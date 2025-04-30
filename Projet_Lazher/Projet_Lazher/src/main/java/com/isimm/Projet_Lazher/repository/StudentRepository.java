package com.isimm.Projet_Lazher.repository;

import com.isimm.Projet_Lazher.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findBySection(String section);
    List<Student> findByNiveau(String niveau);
    List<Student> findBySectionAndNiveau(String section, String niveau);
} 