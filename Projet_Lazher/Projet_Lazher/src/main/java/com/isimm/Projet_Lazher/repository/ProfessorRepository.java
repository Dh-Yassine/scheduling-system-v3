package com.isimm.Projet_Lazher.repository;

import com.isimm.Projet_Lazher.model.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, Long> {
    List<Professor> findBydepartment(String departement);
    Optional<Professor> findByName(String name);
} 