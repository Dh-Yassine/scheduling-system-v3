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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ExcelService {
    
    private static final Logger log = LoggerFactory.getLogger(ExcelService.class);

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
            log.info("Excel file opened successfully: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());
            
            Sheet sheet = workbook.getSheetAt(0);
            log.info("Sheet name: {}, Physical rows: {}, Last row: {}", 
                    sheet.getSheetName(), sheet.getPhysicalNumberOfRows(), sheet.getLastRowNum());
            
            // Debug: Print the first few rows to see the structure
            log.info("Excel structure preview:");
            for (int r = 0; r < Math.min(20, sheet.getLastRowNum()); r++) {
                Row row = sheet.getRow(r);
                if (row != null) {
                    StringBuilder rowContent = new StringBuilder();
                    rowContent.append(String.format("Row %d: ", r));
                    for (int c = 0; c < 10; c++) { // Just first 10 columns
                        String cellValue = getCellValue(sheet, r, c);
                        if (!isEmpty(cellValue)) {
                            rowContent.append(String.format("[Col %d: %s] ", c, cellValue));
                        }
                    }
                    log.info(rowContent.toString());
                }
            }
            
            Map<String, Room> rooms = initializeRooms();
            Map<String, Professor> professors = new HashMap<>();
            List<Course> courses = new ArrayList<>();

            log.info("Starting detailed Excel processing");
            log.info("Room rows to process: {}", Arrays.toString(ROOM_ROWS));
            log.info("Room columns to process: {}", Arrays.toString(ROOM_COLUMNS));
            
            // Process each room row block
            for (int roomRowIndex : ROOM_ROWS) {
                log.info("Processing room row: {}", roomRowIndex);
                
                // Process each day
                for (int dayIndex = 0; dayIndex < DAYS.length; dayIndex++) {
                    // Get room from the room column
                    String roomName = getCellValue(sheet, roomRowIndex, ROOM_COLUMNS[dayIndex]);
                    log.info("Room at day {}, row {}, col {}: '{}'", 
                            DAYS[dayIndex], roomRowIndex, ROOM_COLUMNS[dayIndex], roomName);
                    
                    if (isEmpty(roomName)) {
                        log.info("Empty room name, skipping this day/room combination");
                        continue;
                    }
                    
                    Room room = getOrCreateRoom(roomName, rooms);
                    log.info("Processing room: {} (ID: {})", room.getName(), room.getId());
                    
                    // Process each time slot for this day
                    int startCol = TIME_SLOT_OFFSETS[dayIndex];
                    for (int slotIndex = 0; slotIndex < 6; slotIndex++) {
                        int currentCol = startCol + slotIndex;
                        log.info("Processing time slot: {} at column {}", TIME_SLOTS[slotIndex], currentCol);
                        
                        // Get data from the three-row block
                        String section = getCellValue(sheet, roomRowIndex, currentCol);      // Same row as room
                        String professorName = getCellValue(sheet, roomRowIndex + 1, currentCol);  // One row below
                        String courseDesc = getCellValue(sheet, roomRowIndex + 2, currentCol);     // Two rows below
                        
                        log.info("Cell data - Section: '{}', Professor: '{}', Course: '{}'", 
                                section, professorName, courseDesc);
                        
                        if (!isEmpty(professorName) && !isEmpty(courseDesc)) {
                            log.info("Found valid course data");
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
                                log.info("Created course: {} with professor: {} in room: {} for {} at {}",
                                    courseDesc, professorName, roomName, DAYS[dayIndex], TIME_SLOTS[slotIndex]);
                                courses.add(course);
                            } else {
                                log.warn("Failed to create course with data - Section: {}, Professor: {}, Course: {}", 
                                    section, professorName, courseDesc);
                            }
                        } else {
                            log.info("Incomplete course data, skipping");
                        }
                    }
                }
            }
            
            // Save all courses
            log.info("Attempting to save {} courses", courses.size());
            saveCourses(courses);
            log.info("Successfully processed {} courses", courses.size());
            
        } catch (Exception e) {
            log.error("Error processing Excel file: {}", e.getMessage());
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
            log.error("Error creating course: {} with professor: {} - {}", 
                courseDesc, professorName, e.getMessage());
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
            log.info("Created new professor: {}", professorName);
            return newProf;
        } catch (Exception e) {
            log.error("Error processing professor: {} - {}", professorName, e.getMessage());
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
            log.info("Created new room: {}", roomName);
            return newRoom;
        } catch (Exception e) {
            log.error("Error processing room: {} - {}", roomName, e.getMessage());
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
                log.error("Error saving course: {} - {}", course.getDescription(), e.getMessage());
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
            if (r == null) {
                log.debug("Row {} is null", row);
                return "";
            }
            
            Cell cell = r.getCell(col);
            if (cell == null) {
                log.debug("Cell at row {}, col {} is null", row, col);
                return "";
            }
            
            String value;
            try {
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
                    case BOOLEAN:
                        value = String.valueOf(cell.getBooleanCellValue());
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
                return value.trim();
            } catch (Exception e) {
                // Fallback to STRING type if there's an error
                log.debug("Error getting cell value, trying as STRING: {}", e.getMessage());
                cell.setCellType(CellType.STRING);
                return cell.getStringCellValue().trim();
            }
        } catch (Exception e) {
            log.error("Error reading cell at row {}, col {} - {}", row, col, e.getMessage());
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
