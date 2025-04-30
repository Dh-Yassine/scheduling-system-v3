package com.isimm.Projet_Lazher.service;

import com.isimm.Projet_Lazher.model.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ExcelService {

    @Autowired private ProfessorService professorService;
    @Autowired private RoomService roomService;
    @Autowired private CourseService courseService;

    // Define constants
    private static final String[] DAYS = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"};
    private static final String[] TIME_SLOTS = {
            "08:30 - 10:00", "10:15 - 11:45", "12:00 - 13:30", 
            "13:00 - 14:30", "14:45 - 16:15", "16:30 - 18:00"
    };
    
    // Each day has 6 columns (one for each time slot)
    private static final int COLUMNS_PER_DAY = 6;
    
    // Starting column indices for each day
    private static final int[] DAY_START_COLUMNS = {0, 6, 12, 18, 24, 30};
    
    // Row indices for different types of information
    private static final int SECTION_ROW = 2;    // Row with section info
    private static final int PROFESSOR_ROW = 3;  // Row with professor names
    private static final int COURSE_ROW = 4;     // Row with course descriptions
    private static final int ROOM_ROW = 5;       // Row with room names

    public void processExcelFile(MultipartFile file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0); // Or use getSheet("GroupeTD") if sheet name is known
            if (sheet == null) {
                throw new RuntimeException("Excel sheet not found!");
            }

            Map<String, Room> rooms = initializeRooms();
            Map<String, Professor> professors = new HashMap<>();
            List<Course> courses = new ArrayList<>();

            // Process each day
            for (int dayIndex = 0; dayIndex < DAYS.length; dayIndex++) {
                String currentDay = DAYS[dayIndex];
                int startColumn = DAY_START_COLUMNS[dayIndex];
                
                // Process each time slot for the current day
                for (int slotIndex = 0; slotIndex < TIME_SLOTS.length; slotIndex++) {
                    int currentColumn = startColumn + slotIndex;
                    String currentTimeSlot = TIME_SLOTS[slotIndex];
                    
                    // Extract course information
                    Course course = extractCourseInfo(
                        sheet, 
                        currentDay,
                        currentTimeSlot, 
                        currentColumn, 
                        rooms, 
                        professors
                    );
                    
                    if (course != null) {
                        courses.add(course);
                    }
                }
            }
            
            // Save all extracted courses
            saveCourses(courses);
            
            System.out.println("Successfully processed " + courses.size() + " courses");
        } catch (Exception e) {
            System.err.println("Error processing file: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error processing Excel file: " + e.getMessage(), e);
        }
    }

    private Course extractCourseInfo(
            Sheet sheet, 
            String day,
            String timeSlot, 
            int column, 
            Map<String, Room> rooms, 
            Map<String, Professor> professors) {
        
        // Extract section, professor, course description, and room
        String section = getCellValue(sheet, SECTION_ROW, column);
        String professorName = getCellValue(sheet, PROFESSOR_ROW, column);
        String courseDesc = getCellValue(sheet, COURSE_ROW, column);
        String roomName = getCellValue(sheet, ROOM_ROW, column);
        
        // Skip if we're missing essential information
        if (isEmpty(section) || isEmpty(professorName) || isEmpty(courseDesc)) {
            return null;  
        }
        
        // Parse time slot - convert to LocalDateTime
        String[] timeParts = timeSlot.split(" - ");
        LocalDateTime startTime = parseTime(timeParts[0]);
        LocalDateTime endTime = parseTime(timeParts[1]);
        
        // Validate and possibly swap professor name and course description
        if (shouldSwapProfessorAndCourse(professorName, courseDesc)) {
            String temp = professorName;
            professorName = courseDesc;
            courseDesc = temp;
        }
        
        // Get or create room
        Room room = null;
        if (!isEmpty(roomName)) {
            room = getOrCreateRoom(cleanRoomName(roomName), rooms);
        }
        
        // Get or create professor
        Professor professor = getOrCreateProfessor(professorName, professors);
        
        // Create course
        Course course = new Course();
        course.setStartTime(startTime);
        course.setEndTime(endTime);
        course.setSection(section);
        course.setProfessor(professor);
        course.setDescription(courseDesc);
        course.setRoom(room);
        
        return course;
    }

    private LocalDateTime parseTime(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime localTime = LocalTime.parse(time, formatter);
        return LocalDateTime.now().with(localTime); // Use current date with the parsed time
    }

    private Professor getOrCreateProfessor(String professorName, Map<String, Professor> professors) {
        if (!professors.containsKey(professorName)) {
            Professor newProfessor = new Professor(professorName);
            newProfessor = professorService.saveProfessor(newProfessor);
            professors.put(professorName, newProfessor);
        }
        return professors.get(professorName);
    }

    private Room getOrCreateRoom(String roomName, Map<String, Room> rooms) {
        // First try exact match
        if (rooms.containsKey(roomName)) {
            return rooms.get(roomName);
        }
        
        // Next try a fuzzy match (room that contains this name)
        for (Map.Entry<String, Room> entry : rooms.entrySet()) {
            if (entry.getKey().contains(roomName) || roomName.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        // If not found, create a new room
        Room newRoom = new Room(roomName, 30, "Classroom", true);
        newRoom = roomService.saveRoom(newRoom);
        rooms.put(roomName, newRoom);
        return newRoom;
    }

    private boolean shouldSwapProfessorAndCourse(String professorName, String courseDesc) {
        // Check if professor name looks like a course description (starts with "CR-")
        // and course description looks like a professor name
        return professorName != null && courseDesc != null && 
               professorName.startsWith("CR-") && !courseDesc.startsWith("CR-");
    }
    
    private String cleanRoomName(String roomName) {
        if (roomName != null && roomName.contains("|")) {
            return roomName.split("\\|")[0].trim();
        }
        return roomName;
    }

    private Map<String, Room> initializeRooms() {
        Map<String, Room> rooms = new HashMap<>();
        List<Room> roomList = roomService.getAllRooms();
        for (Room room : roomList) {
            rooms.put(room.getName(), room);
        }
        
        // Add default room IDs if they don't exist in the database
        String[] defaultRooms = {"A-KANOUN", "A-B", "C-01", "C-11", "A-01", "A-02"};
        for (String roomId : defaultRooms) {
            if (!rooms.containsKey(roomId)) {
                Room newRoom = new Room(roomId, 30, "Classroom", true);
                newRoom = roomService.saveRoom(newRoom);
                rooms.put(roomId, newRoom);
            }
        }
        
        return rooms;
    }

    private String getCellValue(Sheet sheet, int rowIndex, int columnIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            return null;
        }
        
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return null;
        }
        
        String value = null;
        switch (cell.getCellType()) {
            case STRING:
                value = cell.getStringCellValue();
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    value = cell.getLocalDateTimeCellValue().toString();
                } else {
                    value = String.valueOf(cell.getNumericCellValue());
                }
                break;
            case FORMULA:
                try {
                    value = cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        value = String.valueOf(cell.getNumericCellValue());
                    } catch (Exception ex) {
                        value = cell.getCellFormula();
                    }
                }
                break;
            default:
                value = null;
        }
        
        return value != null ? value.trim() : null;
    }
    
    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
    
    private void saveCourses(List<Course> courses) {
        for (Course course : courses) {
            courseService.saveCourse(course);
        }
    }
}