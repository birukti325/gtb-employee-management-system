package com.sms.gtbemployeemanagementsystem.Service;

import com.sms.gtbemployeemanagementsystem.Entity.Employee;
import com.sms.gtbemployeemanagementsystem.Entity.Notification;
import com.sms.gtbemployeemanagementsystem.Repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public void notify(Employee employee, String type, String message) {
        Notification n = new Notification();
        n.setEmployee(employee);
        n.setType(type);
        n.setMessage(message);
        notificationRepository.save(n);
    }

    public List<Notification> getAll(Long employeeId) {
        return notificationRepository.findByEmployee_IdOrderByCreatedAtDesc(employeeId);
    }

    public long getUnreadCount(Long employeeId) {
        return notificationRepository.findByEmployee_IdAndIsReadFalseOrderByCreatedAtDesc(employeeId).size();
    }

    public void markAllRead(Long employeeId) {
        List<Notification> unread = notificationRepository.findByEmployee_IdAndIsReadFalseOrderByCreatedAtDesc(employeeId);
        for (Notification n : unread) {
            n.setRead(true);
        }
        notificationRepository.saveAll(unread);
    }
}