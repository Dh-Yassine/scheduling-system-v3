package com.isimm.Projet_Lazher.model;

import jakarta.persistence.*;

@Entity
@Table(name = "professor")
@PrimaryKeyJoinColumn(name = "id")
public class Professor extends User {
    private String name;
    private int totalHours;
    private String department;
    
    public Professor() {
        super();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getTotalHours() {
        return totalHours;
    }
    
    public void setTotalHours(int totalHours) {
        this.totalHours = totalHours;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
}
