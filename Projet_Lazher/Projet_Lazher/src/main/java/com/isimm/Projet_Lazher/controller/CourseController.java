package com.isimm.Projet_Lazher.controller;

import com.isimm.Projet_Lazher.dto.CourseCalendarDTO;
import com.isimm.Projet_Lazher.model.Course;
import com.isimm.Projet_Lazher.service.CourseService;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class CourseController {

    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);

    @Autowired
    private CourseService courseService;

    // REST API endpoints
    @GetMapping("/api/courses")
    @ResponseBody
    public List<CourseCalendarDTO> getCoursesForCalendar() {
        List<Course> courses = courseService.getAllCourses();
        
        System.out.println("Found " + courses.size() + " courses in the database");
        
        // Debug the first few courses
        if (!courses.isEmpty()) {
            int count = Math.min(courses.size(), 3);
            for (int i = 0; i < count; i++) {
                Course course = courses.get(i);
                System.out.println("Course " + (i+1) + ": " + course.getDescription() + 
                                  ", Start: " + course.getStartTime() + 
                                  ", End: " + course.getEndTime());
            }
        }
        
        // Convert courses to DTOs with proper time handling
        List<CourseCalendarDTO> courseDTOs = courses.stream()
            .map(CourseCalendarDTO::new)
            .collect(Collectors.toList());
        
        System.out.println("Converted to " + courseDTOs.size() + " DTOs");
        
        // Debug the first few DTOs
        if (!courseDTOs.isEmpty()) {
            int count = Math.min(courseDTOs.size(), 3);
            for (int i = 0; i < count; i++) {
                CourseCalendarDTO dto = courseDTOs.get(i);
                System.out.println("DTO " + (i+1) + ": " + dto.getDescription() + 
                                  ", Day: " + dto.getDay() + 
                                  ", Start: " + dto.getStartTime() + 
                                  ", End: " + dto.getEndTime());
            }
        }
        
        return courseDTOs;
    }

    @GetMapping("/api/courses/{id}")
    @ResponseBody
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @GetMapping("/api/courses/professor/{professorId}")
    @ResponseBody
    public ResponseEntity<List<Course>> getCoursesByProfessor(@PathVariable Long professorId) {
        return ResponseEntity.ok(courseService.getCoursesByProfessor(professorId));
    }

    @GetMapping("/api/courses/room/{roomId}")
    @ResponseBody
    public ResponseEntity<List<Course>> getCoursesByRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(courseService.getCoursesByRoom(roomId));
    }

    @PostMapping("/api/courses")
    @ResponseBody
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        return ResponseEntity.ok(courseService.saveCourse(course));
    }

    @PutMapping("/api/courses/{id}")
    @ResponseBody
    public ResponseEntity<Course> updateCourse(@PathVariable Long id, @RequestBody Course course) {
        course.setId(id);
        return ResponseEntity.ok(courseService.saveCourse(course));
    }

    @DeleteMapping("/api/courses/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Get courses for a specific professor
     */
    @GetMapping("/api/courses/professor/{professorId}")
    @ResponseBody
    public List<CourseCalendarDTO> getCoursesByProfessor(@PathVariable Long professorId) {
        List<Course> courses = courseService.getCoursesByProfessorId(professorId);
        
        // Debug information
        logger.info("Found {} courses for professor ID {}", courses.size(), professorId);
        
        // Convert to DTOs
        List<CourseCalendarDTO> dtos = courses.stream()
                .map(CourseCalendarDTO::fromCourse)
                .collect(Collectors.toList());
        
        logger.info("Converted to {} DTOs", dtos.size());
        
        return dtos;
    }
    
    /**
     * Get special occasion courses
     */
    @GetMapping("/api/courses/special")
    @ResponseBody
    public List<CourseCalendarDTO> getSpecialCourses() {
        List<Course> courses = courseService.getSpecialCourses();
        
        // Debug information
        logger.info("Found {} special occasion courses", courses.size());
        
        // Convert to DTOs
        List<CourseCalendarDTO> dtos = courses.stream()
                .map(CourseCalendarDTO::fromCourse)
                .collect(Collectors.toList());
        
        logger.info("Converted to {} special occasion DTOs", dtos.size());
        
        return dtos;
    }

    // View endpoints
    @GetMapping("/courses")
    public String listCourses(Model model) {
        List<Course> courses = courseService.getAllCourses();
        model.addAttribute("courses", courses);
        return "list";
    }
}