package com.isimm.Projet_Lazher.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer capacity;
    private String type; // CLASSROOM, LAB, etc.
    private Boolean isAvailable = true;
    
    // Default constructor (needed by Hibernate)
    public Room() {
    }

    // Parameterized constructor (for convenience)
    public Room(String name, int capacity, String type, boolean isAvailable) {
        this.name = name;
        this.capacity = capacity;
        this.type = type;
        this.isAvailable = isAvailable;
    }

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<Course> courses;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getCapacity() {
		return capacity;
	}

	public void setCapacity(Integer capacity) {
		this.capacity = capacity;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean getIsAvailable() {
		return isAvailable;
	}

	public void setIsAvailable(Boolean isAvailable) {
		this.isAvailable = isAvailable;
	}

	public List<Course> getCourses() {
		return courses;
	}

	public void setCourses(List<Course> courses) {
		this.courses = courses;
	}

    
}
