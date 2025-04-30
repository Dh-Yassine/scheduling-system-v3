package com.isimm.Projet_Lazher.service;

import com.isimm.Projet_Lazher.model.Schedule;
import com.isimm.Projet_Lazher.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.time.LocalDateTime;

@Service
public class ScheduleService {
    
    private final ScheduleRepository scheduleRepository;
    private final NotificationService notificationService;
    
    @Autowired
    public ScheduleService(ScheduleRepository scheduleRepository, NotificationService notificationService) {
        this.scheduleRepository = scheduleRepository;
        this.notificationService = notificationService;
    }

    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    public Schedule saveSchedule(Schedule schedule) {
        validateScheduleConflicts(schedule);
        Schedule savedSchedule = scheduleRepository.save(schedule);
        notificationService.notifyScheduleChange(savedSchedule);
        return savedSchedule;
    }

    public List<Schedule> getSchedulesByProfessor(Long professorId) {
        return scheduleRepository.findByProfessorId(professorId);
    }

    private void validateScheduleConflicts(Schedule schedule) {
        // Check for room conflicts
        List<Schedule> roomConflicts = scheduleRepository.findConflictingRoomSchedules(
            schedule.getRoom().getId(),
            schedule.getStartTime(),
            schedule.getEndTime()
        );
        
        if (!roomConflicts.isEmpty()) {
            throw new RuntimeException("Room scheduling conflict detected");
        }

        // Check for professor conflicts
        List<Schedule> professorConflicts = scheduleRepository.findConflictingProfessorSchedules(
            schedule.getProfessor().getId(),
            schedule.getStartTime(),
            schedule.getEndTime()
        );
        
        if (!professorConflicts.isEmpty()) {
            throw new RuntimeException("Professor scheduling conflict detected");
        }
    }
}
