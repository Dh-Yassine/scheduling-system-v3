package com.isimm.Projet_Lazher.repository;

import com.isimm.Projet_Lazher.model.Schedule;
import com.isimm.Projet_Lazher.model.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByProfessorId(Long professorId);
    
    @Query("SELECT s FROM Schedule s WHERE s.room.id = :roomId " +
           "AND ((s.startTime BETWEEN :startTime AND :endTime) " +
           "OR (s.endTime BETWEEN :startTime AND :endTime))")
    List<Schedule> findConflictingRoomSchedules(
        Long roomId, 
        LocalDateTime startTime, 
        LocalDateTime endTime
    );
    
    @Query("SELECT s FROM Schedule s WHERE s.professor.id = :professorId " +
           "AND ((s.startTime BETWEEN :startTime AND :endTime) " +
           "OR (s.endTime BETWEEN :startTime AND :endTime))")
    List<Schedule> findConflictingProfessorSchedules(
        Long professorId, 
        LocalDateTime startTime, 
        LocalDateTime endTime
    );
}
