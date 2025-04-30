package com.isimm.Projet_Lazher.controller;

import com.isimm.Projet_Lazher.model.Professor;
import com.isimm.Projet_Lazher.model.Course;
import com.isimm.Projet_Lazher.service.ProfessorService;
import com.isimm.Projet_Lazher.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/professors")
@CrossOrigin(origins = "*")
public class ProfessorController {

    @Autowired
    private ProfessorService professorService;
    
    @Autowired
    private CourseService courseService;

    @GetMapping
    public ResponseEntity<List<Professor>> getAllProfessors() {
        return ResponseEntity.ok(professorService.getAllProfessors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Professor> getProfessorById(@PathVariable Long id) {
        return ResponseEntity.ok(professorService.getProfessorById(id));
    }

    @GetMapping("/department/{departement}")
    public ResponseEntity<List<Professor>> getProfessorsByDepartement(@PathVariable String departement) {
        return ResponseEntity.ok(professorService.getProfessorsByDepartement(departement));
    }

    @GetMapping("/{id}/courses")
    public ResponseEntity<List<Course>> getProfessorCourses(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCoursesByProfessor(id));
    }

    @PostMapping
    public ResponseEntity<Professor> createProfessor(@RequestBody Professor professor) {
        return ResponseEntity.ok(professorService.saveProfessor(professor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Professor> updateProfessor(@PathVariable Long id, @RequestBody Professor professor) {
        professor.setId(id);
        return ResponseEntity.ok(professorService.saveProfessor(professor));
    }  

    /*@DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfessor(@PathVariable Long id) {
        professorService.deleteProfessor(id);
        return ResponseEntity.ok().build();
    }*/
} 