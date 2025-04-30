package com.isimm.Projet_Lazher.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "Professor")
public class Professor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generate ID
    private Long id;

    private String name;
    private String email;
    private String department;
    private Integer totalHours;
    private String role;
 
    @OneToMany(mappedBy = "professor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Course> courses;
  
    // Constructors
    public Professor() {}

    public Professor(String name) {
        this.name = name;
        this.email = name.toLowerCase().replace(" ", ".") + "@isimm.u-monastir.tn";
        this.department = "Default";
        this.totalHours = 0;
        this.role = "PROFESSOR";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Integer getTotalHours() { return totalHours; }
    public void setTotalHours(Integer totalHours) { this.totalHours = totalHours; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public List<Course> getCourses() { return courses; }
    public void setCourses(List<Course> courses) { this.courses = courses; }
}
