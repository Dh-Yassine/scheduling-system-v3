package com.isimm.Projet_Lazher.service;

import com.isimm.Projet_Lazher.model.Student;
import com.isimm.Projet_Lazher.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class StudentService {
    
    @Autowired
    private StudentRepository studentRepository;
    
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }
    
    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
    }
    
    public List<Student> getStudentsBySection(String section) {
        return studentRepository.findBySection(section);
    }
    
    public List<Student> getStudentsByNiveau(String niveau) {
        return studentRepository.findByNiveau(niveau);
    }
    
    public Student saveStudent(Student student) {
        return studentRepository.save(student);
    }
    
    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }
    
    public List<Student> getStudentsBySectionAndNiveau(String section, String niveau) {
        return studentRepository.findBySectionAndNiveau(section, niveau);
    }
} 