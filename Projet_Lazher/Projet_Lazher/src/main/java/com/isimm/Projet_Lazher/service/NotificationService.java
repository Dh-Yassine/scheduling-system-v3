package com.isimm.Projet_Lazher.service;

import com.isimm.Projet_Lazher.model.Notification;
import com.isimm.Projet_Lazher.model.Schedule;
import com.isimm.Projet_Lazher.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    @Autowired
    private NotificationRepository notificationRepository;

    public void notifyScheduleChange(Schedule schedule) {
        if (schedule == null) {
            logger.warn("Cannot create notification for null schedule");
            return;
        }

        String message;
        if (schedule.getCourse() != null) {
            message = "Schedule updated for course: " + schedule.getCourse().getDescription();
        } else {
            message = "Schedule updated for time slot: " + schedule.getStartTime();
        }

        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setSentAt(LocalDateTime.now());
        notificationRepository.save(notification);
        
        logger.info("Created notification: {}", message);
    }
}
