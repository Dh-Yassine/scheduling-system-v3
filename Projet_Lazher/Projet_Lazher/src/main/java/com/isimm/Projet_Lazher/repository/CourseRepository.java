package com.isimm.Projet_Lazher.repository;

import com.isimm.Projet_Lazher.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByProfessorId(Long professorId);
    List<Course> findByRoomId(Long roomId);
    List<Course> findBySection(String section);
    Optional<Course> findByDescription(String description);
}
