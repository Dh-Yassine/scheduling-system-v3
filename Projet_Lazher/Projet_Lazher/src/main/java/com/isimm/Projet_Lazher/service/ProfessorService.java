package com.isimm.Projet_Lazher.service;

import com.isimm.Projet_Lazher.model.Professor;
import com.isimm.Projet_Lazher.repository.ProfessorRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProfessorService {

    private static final Logger logger = LoggerFactory.getLogger(ProfessorService.class);

    private final ProfessorRepository professorRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public ProfessorService(ProfessorRepository professorRepository) {
        this.professorRepository = professorRepository;
    }

    public List<Professor> getAllProfessors() {
        return professorRepository.findAll();
    }

    public Professor getProfessorById(Long id) {
        return professorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Professor not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Professor findByName(String name) {
        return professorRepository.findByName(name.trim()).orElse(null);
    }

    @Transactional
    public Professor saveProfessor(Professor professor) {
        try {
            // Check if professor already exists
            Professor existingProfessor = findByName(professor.getName());
            if (existingProfessor != null) {
                logger.info("Professor already exists: {}", professor.getName());
                return existingProfessor;
            }

            // Set default values
            professor.setEmail(professor.getName().toLowerCase().replace(" ", ".") + "@isimm.u-monastir.tn");
            professor.setRole("PROFESSOR");
            professor.setDepartment(professor.getDepartment() != null ? professor.getDepartment() : "Default");
            professor.setTotalHours(professor.getTotalHours() != null ? professor.getTotalHours() : 0);

            Professor savedProfessor = professorRepository.save(professor);
            entityManager.flush();
            entityManager.refresh(savedProfessor);

            logger.info("Saved professor: {} with ID: {}", savedProfessor.getName(), savedProfessor.getId());
            return savedProfessor;
        } catch (Exception e) {
            logger.error("Error saving professor {}: {}", professor.getName(), e.getMessage());
            throw new RuntimeException("Failed to save professor", e);
        }
    }

    public void deleteProfessor(Long id) {
        professorRepository.deleteById(id);
    }
}
