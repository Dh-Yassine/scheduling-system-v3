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
        
        // Debug information
        logger.info("Found {} courses in the database", courses.size());
        
        // Convert to DTOs
        List<CourseCalendarDTO> dtos = courses.stream()
                .map(CourseCalendarDTO::fromCourse)
                .collect(Collectors.toList());
        
        logger.info("Converted to {} DTOs", dtos.size());
        
        return dtos;
    }

    @GetMapping("/api/courses/{id}")
    @ResponseBody
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
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
    public List<CourseCalendarDTO> getCoursesByProfessorId(@PathVariable Long professorId) {
        logger.info("API request: Fetching courses for professor ID: {}", professorId);
        
        // Get courses for the professor
        List<Course> courses = courseService.getCoursesByProfessorId(professorId);
        logger.info("Found {} courses for professor ID: {}", courses.size(), professorId);
        
        // Log course details for debugging
        if (courses.isEmpty()) {
            logger.warn("No courses found for professor ID: {}", professorId);
        } else {
            for (Course course : courses) {
                logger.info("Course: {}, Start: {}, End: {}, Section: {}, Professor: {}", 
                    course.getDescription(),
                    course.getStartTime(),
                    course.getEndTime(),
                    course.getSection(),
                    course.getProfessor() != null ? course.getProfessor().getName() : "Unknown");
            }
        }
        
        // Convert to DTOs
        List<CourseCalendarDTO> dtos = courses.stream()
                .map(CourseCalendarDTO::fromCourse)
                .collect(Collectors.toList());
        
        logger.info("Converted to {} DTOs", dtos.size());
        
        return dtos;
    }
    
    /**
     * Get courses by section
     */
    @GetMapping("/api/courses/section/{section}")
    @ResponseBody
    public List<CourseCalendarDTO> getCoursesBySection(@PathVariable String section) {
        logger.info("Fetching courses for section: {}", section);
        List<Course> courses = courseService.getCoursesBySection(section);
        logger.info("Found {} courses for section: {}", courses.size(), section);
        
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