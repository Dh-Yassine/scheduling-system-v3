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

    // Room columns (A, H, O, V, AC, AJ)
    private static final int[] ROOM_COLUMNS = {0, 7, 14, 21, 28, 35};
    
    // Room rows (3, 6, 9, 12, 33, 36)
    private static final int[] ROOM_ROWS = {2, 5, 8, 11, 32, 35};
    
    // Column offsets for each day's time slots
    private static final int[] TIME_SLOT_OFFSETS = {
        1,  // B-G for Monday
        8,  // I-N for Tuesday
        15, // P-U for Wednesday
        22, // W-AB for Thursday
        29, // AD-AI for Friday
        36  // AL-AQ for Saturday
    };
    
    // Time slots for each day
    private static final String[] TIME_SLOTS = {
            "08:30 - 10:00", 
            "10:15 - 11:45", 
            "12:00 - 13:30",
            "13:45 - 15:15", 
            "15:30 - 17:00", 
            "17:15 - 18:45"
    };
    
    // Days of the week
    private static final String[] DAYS = {
            "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"
    };

    public void processExcelFile(MultipartFile file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            Map<String, Room> rooms = initializeRooms();
            Map<String, Professor> professors = new HashMap<>();
            List<Course> courses = new ArrayList<>();

            // Process each room row block
            for (int roomRowIndex : ROOM_ROWS) {
                // Process each day
                for (int dayIndex = 0; dayIndex < DAYS.length; dayIndex++) {
                    // Get room from the room column
                    String roomName = getCellValue(sheet, roomRowIndex, ROOM_COLUMNS[dayIndex]);
                    if (isEmpty(roomName)) continue;
                    
                    Room room = getOrCreateRoom(roomName, rooms);
                    
                    // Process each time slot for this day
                    int startCol = TIME_SLOT_OFFSETS[dayIndex];
                    for (int slotIndex = 0; slotIndex < 6; slotIndex++) {
                        int currentCol = startCol + slotIndex;
                        
                        // Get data from the three-row block
                        String section = getCellValue(sheet, roomRowIndex, currentCol);      // Same row as room
                        String professorName = getCellValue(sheet, roomRowIndex + 1, currentCol);  // One row below
                        String courseDesc = getCellValue(sheet, roomRowIndex + 2, currentCol);     // Two rows below
                        
                        if (!isEmpty(professorName) && !isEmpty(courseDesc)) {
                            Course course = createCourse(
                                courseDesc,
                                professorName,
                                section,
                                DAYS[dayIndex],
                                TIME_SLOTS[slotIndex],
                                room,
                                professors
                            );
                            if (course != null) {
                                System.out.println("Created course: " + courseDesc + " with professor: " + professorName + 
                                    " in room: " + roomName + " for " + DAYS[dayIndex] + " at " + TIME_SLOTS[slotIndex]);
                                courses.add(course);
                            }
                        }
                    }
                }
            }
            
            // Save all courses
            saveCourses(courses);
            System.out.println("Successfully processed " + courses.size() + " courses");
            
        } catch (Exception e) {
            System.err.println("Error processing Excel file: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error processing Excel file: " + e.getMessage(), e);
        }
    }

    private Course createCourse(
            String courseDesc,
            String professorName,
            String section,
            String day,
            String timeSlot,
            Room room,
            Map<String, Professor> professors) {
            
        if (isEmpty(courseDesc) || isEmpty(professorName)) return null;
        
        try {
            // Parse time
            String[] times = timeSlot.split(" - ");
            LocalDateTime startTime = parseTime(times[0]);
            LocalDateTime endTime = parseTime(times[1]);
            
            // Get or create professor
            Professor professor = getOrCreateProfessor(professorName, professors);
            
            // Create course
            Course course = new Course();
            course.setStartTime(startTime);
            course.setEndTime(endTime);
            course.setDescription(courseDesc);
            course.setProfessor(professor);
            course.setRoom(room);
            course.setSection(section != null ? section : "Default");
            
            return course;
        } catch (Exception e) {
            System.err.println("Error creating course: " + courseDesc + " with professor: " + professorName + 
                " - " + e.getMessage());
            return null;
        }
    }

    private Professor getOrCreateProfessor(String professorName, Map<String, Professor> professors) {
        if (isEmpty(professorName)) return null;
        
        try {
            if (professors.containsKey(professorName)) {
                return professors.get(professorName);
            }
            
            // Try to find existing professor by name or email
            String email = "ens" + professorName.toLowerCase().replaceAll("\\s+", ".") + "@isimm.u-monastir.tn";
            Optional<Professor> existingProf = professorService.getProfessorByEmail(email);
            if (!existingProf.isPresent()) {
                existingProf = professorService.findByName(professorName);
            }
            
            if (existingProf.isPresent()) {
                professors.put(professorName, existingProf.get());
                return existingProf.get();
            }
            
            // Create new professor
            Professor newProf = new Professor();
            newProf.setName(professorName);
            newProf.setEmail(email);
            newProf.setDepartment("Default");
            newProf.setTotalHours(0);
            newProf.setRole("PROFESSOR");
            
            newProf = professorService.saveProfessor(newProf);
            professors.put(professorName, newProf);
            System.out.println("Created new professor: " + professorName);
            return newProf;
        } catch (Exception e) {
            System.err.println("Error processing professor: " + professorName + " - " + e.getMessage());
            return null;
        }
    }

    private Room getOrCreateRoom(String roomName, Map<String, Room> rooms) {
        if (isEmpty(roomName)) return null;
        
        try {
            roomName = cleanRoomName(roomName);
            
            if (rooms.containsKey(roomName)) {
                return rooms.get(roomName);
            }
            
            Room newRoom = new Room();
            newRoom.setName(roomName);
            newRoom.setCapacity(30);
            newRoom.setType("Classroom");
            newRoom.setIsAvailable(true);
            
            newRoom = roomService.saveRoom(newRoom);
            rooms.put(roomName, newRoom);
            System.out.println("Created new room: " + roomName);
            return newRoom;
        } catch (Exception e) {
            System.err.println("Error processing room: " + roomName + " - " + e.getMessage());
            return null;
        }
    }

    private Map<String, Room> initializeRooms() {
        Map<String, Room> rooms = new HashMap<>();
        List<Room> existingRooms = roomService.getAllRooms();
        for (Room room : existingRooms) {
            rooms.put(room.getName(), room);
        }
        return rooms;
    }

    private void saveCourses(List<Course> courses) {
        for (Course course : courses) {
            try {
                courseService.saveCourse(course);
            } catch (Exception e) {
                System.err.println("Error saving course: " + course.getDescription() + " - " + e.getMessage());
            }
        }
    }

    private LocalDateTime parseTime(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime localTime = LocalTime.parse(time.trim(), formatter);
        return LocalDateTime.now().with(localTime);
    }

    private String getCellValue(Sheet sheet, int row, int col) {
        try {
            Row r = sheet.getRow(row);
            if (r == null) return "";
            
            Cell cell = r.getCell(col);
            if (cell == null) return "";
            
            cell.setCellType(CellType.STRING);
            return cell.getStringCellValue().trim();
        } catch (Exception e) {
            System.err.println("Error reading cell at row " + row + ", col " + col + " - " + e.getMessage());
            return "";
        }
    }
    
    private String cleanRoomName(String roomName) {
        if (roomName == null) return "";
        return roomName.split("\\|")[0].trim();
    }
    
    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}