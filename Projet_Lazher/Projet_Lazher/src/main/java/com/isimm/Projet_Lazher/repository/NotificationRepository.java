package com.isimm.Projet_Lazher.repository;

import com.isimm.Projet_Lazher.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
