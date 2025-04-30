package com.isimm.Projet_Lazher.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.isimm.Projet_Lazher.service.*;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @Autowired private RoomService roomService;
    @Autowired private CourseService courseService;
    @Autowired private ProfessorService professorService;
    @Autowired private ScheduleService scheduleService;
    
    @GetMapping("/counts")
    public String getCounts() {
        return String.format(
            "Rooms: %d\nCourses: %d\nProfessors: %d\nSchedules: %d",
            roomService.getAllRooms().size(),
            courseService.getAllCourses().size(),
            professorService.getAllProfessors().size(),
            scheduleService.getAllSchedules().size()
        );
    }
} //TO TEST CHNIWA SAYER