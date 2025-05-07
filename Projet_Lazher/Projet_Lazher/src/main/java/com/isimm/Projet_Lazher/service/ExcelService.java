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
    
    // Room rows (3, 6, 9, 12, 33, 36) - zero-indexed for Java (2, 5, 8, 11, 32, 35)
    private static final int[] ROOM_ROWS = {2, 5, 8, 11, 32, 35};
    
    // Offsets from room row
    private static final int SECTION_OFFSET = 0;    // Same row as room
    private static final int PROFESSOR_OFFSET = 1;  // 1 row below room
    private static final int COURSE_OFFSET = 2;     // 2 rows below room
    
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
            System.out.println("Processing sheet: " + sheet.getSheetName());
            
            Map<String, Room> rooms = initializeRooms();
            Map<String, Professor> professors = new HashMap<>();
            List<Course> courses = new ArrayList<>();

            // Process each day
            for (int dayIndex = 0; dayIndex < DAYS.length; dayIndex++) {
                System.out.println("\nProcessing day: " + DAYS[dayIndex]);
                
                // Get room column for this day
                int roomColumn = ROOM_COLUMNS[dayIndex];
                
                // Process each room row
                for (int roomRowIndex : ROOM_ROWS) {
                    System.out.println("\nProcessing room row: " + (roomRowIndex + 1));
                    
                    // Get room name
                    String roomName = getCellValue(sheet, roomRowIndex, roomColumn);
                    System.out.println("Room name: '" + roomName + "'");
                    
                    if (isEmpty(roomName)) {
                        System.out.println("Skipping empty room");
                        continue;
                    }
                    
                    Room room = getOrCreateRoom(roomName, rooms);
                    System.out.println("Room: " + room.getName());
                    
                    // Process each time slot
                    for (int slot = 0; slot < 6; slot++) {
                        // Calculate column index for this time slot
                        int colIndex = roomColumn + slot + 1; // +1 to skip room column
                        System.out.println("\nProcessing time slot: " + TIME_SLOTS[slot] + 
                            " at column: " + (colIndex + 1));
                        
                        // Get data from the three-row block
                        // Room row is roomRowIndex
                        // Professor row is roomRowIndex + 1
                        // Course row is roomRowIndex + 2
                        String section = getCellValue(sheet, roomRowIndex, colIndex);
                        String professorName = getCellValue(sheet, roomRowIndex + 1, colIndex);
                        String courseDesc = getCellValue(sheet, roomRowIndex + 2, colIndex);
                        
                        System.out.println("Section: '" + section + "'");
                        System.out.println("Professor: '" + professorName + "'");
                        System.out.println("Course: '" + courseDesc + "'");
                        
                        if (!isEmpty(professorName) && !isEmpty(courseDesc)) {
                            Course course = createCourse(
                                courseDesc,
                                professorName,
                                section,
                                DAYS[dayIndex],
                                TIME_SLOTS[slot],
                                room,
                                professors
                            );
                            if (course != null) {
                                System.out.println("Successfully created course: " + course.getDescription());
                                courses.add(course);
                            }
                        }
                    }
                }
            }
            
            // Save all courses
            saveCourses(courses);
            System.out.println("\nSuccessfully processed " + courses.size() + " courses");
            
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
        
        try {
            // Parse time
            String[] times = timeSlot.split(" - ");
            LocalTime startLocalTime = LocalTime.parse(times[0], DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime endLocalTime = LocalTime.parse(times[1], DateTimeFormatter.ofPattern("HH:mm"));
            
            // Convert LocalTime to LocalDateTime
            LocalDateTime startTime = LocalDateTime.now().withHour(startLocalTime.getHour()).withMinute(startLocalTime.getMinute()).withSecond(0).withNano(0);
            LocalDateTime endTime = LocalDateTime.now().withHour(endLocalTime.getHour()).withMinute(endLocalTime.getMinute()).withSecond(0).withNano(0);
            
            // Get or create professor
            Professor professor = getOrCreateProfessor(professorName, professors);
            if (professor == null) {
                System.err.println("Failed to create professor: " + professorName);
                return null;
            }
            
            // Create course with day incorporated in description
            Course course = new Course();
            course.setStartTime(startTime);
            course.setEndTime(endTime);
            course.setDescription(day + " - " + courseDesc);  // Include day in description
            course.setProfessor(professor);
            course.setRoom(room);
            course.setSection(section != null && !section.isEmpty() ? section : "Default");
            
            return course;
        } catch (Exception e) {
            System.err.println("Error creating course: " + courseDesc + " with professor: " + professorName + 
                " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private Professor getOrCreateProfessor(String professorName, Map<String, Professor> professors) {
        if (isEmpty(professorName)) {
            System.err.println("Skipping professor - empty name");
            return null;
        }
        
        try {
            // Check if we already have this professor in our cache
            if (professors.containsKey(professorName)) {
                Professor existingProf = professors.get(professorName);
                if (existingProf.getId() != null) {
                    return existingProf;
                }
            }
            
            // Generate email based on professor name
            String email = "ens" + professorName.toLowerCase().replaceAll("\\s+", ".") + "@isimm.u-monastir.tn";
            
            // Try to find existing professor by email first
            Optional<Professor> existingProf = professorService.getProfessorByEmail(email);
            
            // If not found by email, try by name
            if (!existingProf.isPresent()) {
                existingProf = professorService.findByName(professorName);
            }
            
            // If found by either method, use that professor
            if (existingProf.isPresent()) {
                Professor prof = existingProf.get();
                professors.put(professorName, prof);
                System.out.println("Found existing professor: " + professorName + " with ID: " + prof.getId());
                return prof;
            }
            
            // Search for any professor with similar email pattern (in case of duplicates)
            String emailPattern = email.replace("@", "%@");
            List<Professor> similarProfessors = professorService.findByEmailLike(emailPattern);
            
            if (!similarProfessors.isEmpty()) {
                // Use the first one found
                Professor prof = similarProfessors.get(0);
                professors.put(professorName, prof);
                System.out.println("Found similar professor: " + professorName + " with ID: " + prof.getId());
                return prof;
            }
            
            // Create new professor as a last resort
            try {
                Professor newProf = new Professor();
                newProf.setName(professorName);
                newProf.setEmail(email);
                newProf.setDepartment("Default");
                newProf.setTotalHours(0);
                newProf.setRole("PROFESSOR");
                
                // Save the professor
                Professor savedProf = professorService.saveProfessor(newProf);
                if (savedProf == null || savedProf.getId() == null) {
                    System.err.println("Failed to save professor: " + professorName);
                    return null;
                }
                
                professors.put(professorName, savedProf);
                System.out.println("Created new professor: " + professorName + " with ID: " + savedProf.getId());
                return savedProf;
            } catch (Exception e) {
                if (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("email")) {
                    System.err.println("Duplicate email for professor: " + professorName + " - trying to find existing one");
                    
                    // Try one more time to find by email
                    existingProf = professorService.getProfessorByEmail(email);
                    if (existingProf.isPresent()) {
                        Professor prof = existingProf.get();
                        professors.put(professorName, prof);
                        System.out.println("Found existing professor after duplicate error: " + professorName);
                        return prof;
                    }
                    
                    // If still not found, create with a modified email
                    String modifiedEmail = "ens" + professorName.toLowerCase().replaceAll("\\s+", ".") 
                            + "." + System.currentTimeMillis() + "@isimm.u-monastir.tn";
                    
                    Professor newProf = new Professor();
                    newProf.setName(professorName);
                    newProf.setEmail(modifiedEmail);
                    newProf.setDepartment("Default");
                    newProf.setTotalHours(0);
                    newProf.setRole("PROFESSOR");
                    
                    Professor savedProf = professorService.saveProfessor(newProf);
                    professors.put(professorName, savedProf);
                    System.out.println("Created professor with modified email: " + professorName);
                    return savedProf;
                } else {
                    throw e; // Re-throw if it's not a duplicate email issue
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing professor: " + professorName + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private Room getOrCreateRoom(String roomName, Map<String, Room> rooms) {
        if (isEmpty(roomName)) {
            System.err.println("Skipping room - empty name");
            return null;
        }
        
        try {
            roomName = cleanRoomName(roomName);
            
            if (rooms.containsKey(roomName)) {
                Room existingRoom = rooms.get(roomName);
                if (existingRoom.getId() != null) {
                    return existingRoom;
                }
            }
            
            // Try to find existing room
            Room existingRoom = roomService.findByName(roomName);
            if (existingRoom != null && existingRoom.getId() != null) {
                rooms.put(roomName, existingRoom);
                return existingRoom;
            }
            
            // Create new room
            Room newRoom = new Room();
            newRoom.setName(roomName);
            newRoom.setCapacity(30);
            newRoom.setType("Classroom");
            newRoom.setIsAvailable(true);
            
            newRoom = roomService.saveRoom(newRoom);
            if (newRoom.getId() == null) {
                System.err.println("Failed to get ID for room: " + roomName);
                return null;
            }
            
            rooms.put(roomName, newRoom);
            System.out.println("Created new room: " + roomName + " with ID: " + newRoom.getId());
            return newRoom;
        } catch (Exception e) {
            System.err.println("Error processing room: " + roomName + " - " + e.getMessage());
            e.printStackTrace();
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
                Course savedCourse = courseService.saveCourse(course);
                if (savedCourse != null) {
                    System.out.println("Saved course: " + course.getDescription());
                } else {
                    System.err.println("Failed to save course: " + course.getDescription());
                }
            } catch (Exception e) {
                System.err.println("Error saving course: " + course.getDescription() + " - " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private String getCellValue(Sheet sheet, int row, int col) {
        try {
            Row r = sheet.getRow(row);
            if (r == null) return "";
            
            Cell cell = r.getCell(col);
            if (cell == null) return "";
            
            String value;
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
                    value = "";
            }
            
            return value != null ? value.trim() : "";
        } catch (Exception e) {
            System.err.println("Error reading cell at row " + row + ", col " + col + " - " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }
    
    private String cleanRoomName(String roomName) {
        if (roomName == null) return "";
        
        // Remove any part after pipe character if present
        if (roomName.contains("|")) {
            return roomName.split("\\|")[0].trim();
        }
        
        return roomName.trim();
    }
    
    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}