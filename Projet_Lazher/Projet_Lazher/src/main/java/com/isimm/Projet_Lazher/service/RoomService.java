package com.isimm.Projet_Lazher.service;

import com.isimm.Projet_Lazher.model.Room;
import com.isimm.Projet_Lazher.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class RoomService {
    
    @Autowired
    private RoomRepository roomRepository;
    
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }
    
    public Room getRoomById(Long id) {
        return roomRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Room not found with id: " + id));
    }
    public Room findByName(String name) {
        Optional<Room> room = roomRepository.findByName(name);
        return room.orElse(null); // Returns null if not found
    }
    /*public List<Room> getAvailableRooms() {
        return roomRepository.findByDisponibilite(true);
    }*/
    
    public Room saveRoom(Room room) {
        return roomRepository.save(room);
    }
    
    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }
   /* 
    public Room updateRoomAvailability(Long id, boolean disponibilite) {
        Room room = getRoomById(id);
        room.setDisponibilite(disponibilite);
        return roomRepository.save(room);
    }*/
} 