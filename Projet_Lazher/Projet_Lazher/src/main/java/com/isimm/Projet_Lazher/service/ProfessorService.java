package com.isimm.Projet_Lazher.service;

import com.isimm.Projet_Lazher.model.Professor;
import com.isimm.Projet_Lazher.repository.ProfessorRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    public Professor saveProfessor(Professor professor) {
        return professorRepository.save(professor);
    }

    public List<Professor> getAllProfessors() {
        return professorRepository.findAll();
    }

    public Optional<Professor> getProfessorById(Long id) {
        return professorRepository.findById(id);
    }

    public Optional<Professor> getProfessorByEmail(String email) {
        return professorRepository.findByEmail(email);
    }
    
    public Optional<Professor> findByName(String name) {
        return professorRepository.findByName(name);
    }

    public List<Professor> getProfessorsByDepartment(String department) {
        return professorRepository.findByDepartment(department);
    }

    public void deleteProfessor(Long id) {
        professorRepository.deleteById(id);
    }

    public Professor updateProfessor(Professor professor) {
        return professorRepository.save(professor);
    }
}
