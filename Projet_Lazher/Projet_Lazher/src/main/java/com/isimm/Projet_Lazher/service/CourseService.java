package com.isimm.Projet_Lazher.service;

import com.isimm.Projet_Lazher.model.Course;
import com.isimm.Projet_Lazher.model.Professor;
import com.isimm.Projet_Lazher.repository.CourseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CourseService {
    private static final Logger logger = LoggerFactory.getLogger(CourseService.class);

    @Autowired private CourseRepository courseRepository;
    @Autowired private RoomService roomService;
    @Autowired private ProfessorService professorService;

    @PersistenceContext
    private EntityManager entityManager;

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
    }

    public Course findByDescription(String courseDescription) {
        return courseRepository.findByDescription(courseDescription).orElse(null);
    }

    public List<Course> getCoursesByProfessor(Long professorId) {
        return courseRepository.findByProfessorId(professorId);
    }

    public List<Course> getCoursesByRoom(Long roomId) {
        return courseRepository.findByRoomId(roomId);
    }

    public List<Course> getCoursesBySection(String section) {
        return courseRepository.findBySection(section);
    }

    /**
     * Get courses by professor ID
     */
    public List<Course> getCoursesByProfessorId(Long professorId) {
        // Find the professor first
        Optional<Professor> professorOpt = professorService.findById(professorId);
        if (!professorOpt.isPresent()) {
            logger.warn("Professor with ID {} not found", professorId);
            return new ArrayList<>();
        }
        
        // Get all courses and filter by professor
        List<Course> allCourses = courseRepository.findAll();
        return allCourses.stream()
                .filter(course -> course.getProfessor() != null && 
                        course.getProfessor().getId().equals(professorId))
                .collect(Collectors.toList());
    }
    
    /**
     * Get special occasion courses
     * These are courses with "SPO" or "Special" in their description or section
     */
    public List<Course> getSpecialCourses() {
        List<Course> allCourses = courseRepository.findAll();
        return allCourses.stream()
                .filter(this::isSpecialCourse)
                .collect(Collectors.toList());
    }
    
    /**
     * Check if a course is a special occasion course
     */
    private boolean isSpecialCourse(Course course) {
        // Check if the description contains "SPO" or "Special"
        if (course.getDescription() != null) {
            String desc = course.getDescription().toLowerCase();
            if (desc.contains("spo") || desc.contains("special")) {
                return true;
            }
        }
        
        // Check if the section contains "SPO" or "Special"
        if (course.getSection() != null) {
            String section = course.getSection().toLowerCase();
            if (section.contains("spo") || section.contains("special")) {
                return true;
            }
        }
        
        return false;
    }

    @Transactional
    public Course saveCourse(Course course) {
        try {
            if (course.getProfessor() == null || course.getProfessor().getId() == null) {
                logger.error("Cannot save course: Professor is null or has no ID");
                return null;
            }
            if (course.getRoom() == null || course.getRoom().getId() == null) {
                logger.error("Cannot save course: Room is null or has no ID");
                return null;
            }
            
            // Ensure startTime and endTime are set
            if (course.getStartTime() == null) {
                logger.info("Setting default start time for course: {}", course.getDescription());
                course.setStartTime(LocalDateTime.now().withHour(8).withMinute(0).withSecond(0).withNano(0));
            }
            
            if (course.getEndTime() == null) {
                logger.info("Setting default end time for course: {}", course.getDescription());
                course.setEndTime(course.getStartTime().plusHours(1).plusMinutes(30));
            }
            
            // Save the course
            return courseRepository.save(course);
        } catch (Exception e) {
            logger.error("Error saving course: {}", e.getMessage(), e);
            return null;
        }
    }

    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }
}
