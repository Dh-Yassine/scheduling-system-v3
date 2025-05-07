package com.isimm.Projet_Lazher.dto;

import com.isimm.Projet_Lazher.model.Course;
import com.isimm.Projet_Lazher.model.Professor;
import com.isimm.Projet_Lazher.model.Room;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

/**
 * DTO for transferring course data to the calendar view
 */
public class CourseCalendarDTO {
    private Long id;
    private String description;
    private String section;
    private String professorName;
    private String roomName;
    private String startTime;
    private String endTime;
    private String day;

    public CourseCalendarDTO(Course course) {
        this.id = course.getId();
        this.description = course.getDescription();
        this.section = course.getSection();
        
        // Handle professor
        Professor professor = course.getProfessor();
        this.professorName = professor != null ? professor.getName() : "Unknown";
        
        // Handle room
        Room room = course.getRoom();
        this.roomName = room != null ? room.getName() : "Unknown";
        
        // Extract day from description (format: "Day - Description")
        if (course.getDescription() != null && course.getDescription().contains(" - ")) {
            String[] parts = course.getDescription().split(" - ", 2);
            this.day = parts[0].trim();
        } else {
            this.day = "Monday"; // Default day
        }
        
        // Get the appropriate date for the day of week
        LocalDate date = getDateForDay(this.day);
        
        // Format times as ISO strings
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        
        // Handle start time
        if (course.getStartTime() != null) {
            // Use the time from the course but the date from the day of week
            LocalTime time = course.getStartTime().toLocalTime();
            LocalDateTime dateTime = LocalDateTime.of(date, time);
            this.startTime = dateTime.format(formatter);
        } else {
            // Default to 8:00 AM on the appropriate day
            this.startTime = LocalDateTime.of(date, LocalTime.of(8, 0))
                .format(formatter);
        }
        
        // Handle end time
        if (course.getEndTime() != null) {
            // Use the time from the course but the date from the day of week
            LocalTime time = course.getEndTime().toLocalTime();
            LocalDateTime dateTime = LocalDateTime.of(date, time);
            this.endTime = dateTime.format(formatter);
        } else if (course.getStartTime() != null) {
            // Default to start time + 1.5 hours
            LocalTime time = course.getStartTime().toLocalTime().plusHours(1).plusMinutes(30);
            LocalDateTime dateTime = LocalDateTime.of(date, time);
            this.endTime = dateTime.format(formatter);
        } else {
            // Default to 9:30 AM on the appropriate day
            this.endTime = LocalDateTime.of(date, LocalTime.of(9, 30))
                .format(formatter);
        }
    }
    
    /**
     * Get the date for the given day of week
     */
    private LocalDate getDateForDay(String day) {
        // Always use the current week for all days
        LocalDate now = LocalDate.now();
        DayOfWeek targetDay;
        
        // Debug the day being processed
        System.out.println("Processing day: " + day);
        
        switch (day.toLowerCase()) {
            case "lundi":
            case "monday":
                targetDay = DayOfWeek.MONDAY;
                break;
            case "mardi":
            case "tuesday":
                targetDay = DayOfWeek.TUESDAY;
                break;
            case "mercredi":
            case "wednesday":
                targetDay = DayOfWeek.WEDNESDAY;
                break;
            case "jeudi":
            case "thursday":
                targetDay = DayOfWeek.THURSDAY;
                break;
            case "vendredi":
            case "friday":
                targetDay = DayOfWeek.FRIDAY;
                break;
            case "samedi":
            case "saturday":
                targetDay = DayOfWeek.SATURDAY;
                break;
            case "dimanche":
            case "sunday":
                targetDay = DayOfWeek.SUNDAY;
                break;
            default:
                System.out.println("Unrecognized day: " + day + ", defaulting to Monday");
                targetDay = DayOfWeek.MONDAY; // Default to Monday
        }
        
        // Get the date for the target day in the current week
        // Use previousOrSame to ensure we get the current week, not next week
        LocalDate targetDate = now.with(TemporalAdjusters.previousOrSame(targetDay));
        
        System.out.println("Date calculated for " + day + ": " + targetDate);
        return targetDate;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getProfessorName() {
        return professorName;
    }

    public void setProfessorName(String professorName) {
        this.professorName = professorName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    
    public String getDay() {
        return day;
    }
    
    public void setDay(String day) {
        this.day = day;
    }
}
