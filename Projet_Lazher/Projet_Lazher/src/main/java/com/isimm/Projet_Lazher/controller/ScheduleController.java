package com.isimm.Projet_Lazher.controller;

import com.isimm.Projet_Lazher.model.*;
import com.isimm.Projet_Lazher.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.Map;
import java.time.DayOfWeek;

@Controller
@RequestMapping("/schedules")
public class ScheduleController {
    
    private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);
    
    private final ScheduleService scheduleService;
    private final RoomService roomService;
    private final ProfessorService professorService;
    private final CourseService courseService;
    
    @Autowired
    public ScheduleController(ScheduleService scheduleService, 
                            RoomService roomService,
                            ProfessorService professorService,
                            CourseService courseService) {
        this.scheduleService = scheduleService;
        this.roomService = roomService;
        this.professorService = professorService;
        this.courseService = courseService;
    }

    @GetMapping
    public String listSchedules(Model model) {
        try {
            // Create a default user for now (you should replace this with actual user authentication)
            User defaultUser = new User();
            defaultUser.setName("Admin");
            defaultUser.setRole("ADMIN");
            model.addAttribute("user", defaultUser);
            
            List<Schedule> schedules = scheduleService.getAllSchedules();
            logger.info("Found {} schedules", schedules.size());
            
            List<Room> rooms = roomService.getAllRooms();
            List<Professor> professors = professorService.getAllProfessors();
            List<Course> courses = courseService.getAllCourses();
            
            logger.info("Found {} rooms, {} professors, {} courses", 
                       rooms.size(), professors.size(), courses.size());

            model.addAttribute("schedules", schedules);
            model.addAttribute("rooms", rooms);
            model.addAttribute("professors", professors);
            model.addAttribute("courses", courses);
            model.addAttribute("newSchedule", new Schedule());
            
            // Add counts for display
            model.addAttribute("schedulesCount", schedules.size());
            model.addAttribute("roomsCount", rooms.size());
            model.addAttribute("professorsCount", professors.size());
            model.addAttribute("coursesCount", courses.size());
            model.addAttribute("notificationCount", 0); // Add default notification count

            // Group schedules by day for better display
            Map<DayOfWeek, List<Schedule>> schedulesByDay = schedules.stream()
                .collect(Collectors.groupingBy(s -> s.getStartTime().getDayOfWeek()));
            model.addAttribute("schedulesByDay", schedulesByDay);

            return "list";
        } catch (Exception e) {
            logger.error("Error loading schedules: ", e);
            model.addAttribute("error", "Error loading schedules: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping
    public String createSchedule(@ModelAttribute Schedule schedule, Model model) {
        try {
            scheduleService.saveSchedule(schedule);
            logger.info("Successfully created new schedule");
            return "redirect:/schedules?success=true";
        } catch (Exception e) {
            logger.error("Error creating schedule: ", e);
            model.addAttribute("error", "Error creating schedule: " + e.getMessage());
            return "error";
        }
    }
}
