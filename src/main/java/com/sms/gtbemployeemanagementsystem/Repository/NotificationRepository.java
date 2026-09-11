package com.sms.gtbemployeemanagementsystem.Repository;

import com.sms.gtbemployeemanagementsystem.Entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByEmployee_IdOrderByCreatedAtDesc(Long employeeId);
    List<Notification> findByEmployee_IdAndIsReadFalseOrderByCreatedAtDesc(Long employeeId);
}