package com.isimm.Projet_Lazher.service;

import com.isimm.Projet_Lazher.model.Course;
import com.isimm.Projet_Lazher.repository.CourseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

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
            if (course.getStartTime() == null || course.getEndTime() == null) {
                logger.error("Cannot save course: Start or end time is null");
                return null;
            }

            logger.info("Saving course: {}", course.getDescription());

            Course savedCourse = courseRepository.save(course);
            entityManager.flush();
            entityManager.refresh(savedCourse);

            logger.info("Successfully saved course with ID: {}", savedCourse.getId());
            return savedCourse;
        } catch (Exception e) {
            logger.error("Error saving course: {} - Error: {}", course.getDescription(), e.getMessage());
            throw new RuntimeException("Failed to save course", e);
        }
    }

    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }
}
