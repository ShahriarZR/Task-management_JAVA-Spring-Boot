package com.example.Project.service;

import com.example.Project.entity.Notification;
import com.example.Project.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    // Method to get notifications for an employee and mark them as read
    public List<Notification> getAndMarkNotificationsAsRead(Long employeeId) {
        // Step 1: Fetch the notifications for the employee (now as a Map)
        List<Map<String, Object>> notificationsMap = notificationRepository.getNotificationsByReceiver(employeeId);

        // Step 2: Convert Map to Notification objects
        List<Notification> notifications = notificationsMap.stream().map(map -> {
            Notification notification = new Notification();
            notification.setId((Long) map.get("notification_id"));
            notification.setMessage((String) map.get("message"));
            notification.setCreatedAt(((java.sql.Timestamp) map.get("created_at")).toLocalDateTime());
            notification.setStatus(Notification.Status.valueOf((String) map.get("status")));
            return notification;
        }).toList();

        // Step 3: Mark each notification as "READ"
        for (Notification notification : notifications) {
            notificationRepository.markAsRead(notification.getId());  // Mark each notification as read
        }

        return notifications;  // Return the notifications
    }


    // Other methods related to notification (send, etc.)...
}
