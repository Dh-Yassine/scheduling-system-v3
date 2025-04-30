package com.isimm.Projet_Lazher.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@DiscriminatorValue("STUDENT")
public class Student extends User {
	
    private String section;
    private String niveau;

    public Student(String name, String section, String niveau) {
        super(name);
        this.section = section;
        this.niveau = niveau;
        this.setRole("STUDENT");
    }

    // Ensure proper initialization
    @PrePersist
    public void prePersist() {
        if (this.getSection() == null) {
            this.setSection("Default Section");
        }
        if (this.getNiveau() == null) {
            this.setNiveau("Default Level");
        }
    }

    // Getters and Setters
    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }
}

