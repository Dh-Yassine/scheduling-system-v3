package com.isimm.Projet_Lazher.repository;

import com.isimm.Projet_Lazher.model.Room;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
	Optional<Room> findByName(String name);
} 