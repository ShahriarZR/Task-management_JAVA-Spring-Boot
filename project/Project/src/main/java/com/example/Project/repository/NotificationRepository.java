package com.example.Project.repository;

import com.example.Project.entity.Notification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class NotificationRepository {

    private final JdbcTemplate jdbcTemplate;

    public NotificationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Method to save a new notification
    public int saveNotification(Notification notification) {
        String sql = "INSERT INTO notification (message, sender_id, receiver_id, created_at, status) "
                + "VALUES (?, ?, ?, ?, ?)";

        return jdbcTemplate.update(sql,
                notification.getMessage(),
                notification.getSender().getId(),
                notification.getReceiver().getId(),
                notification.getCreatedAt(),
                notification.getStatus().name());
    }

    // Method to get all notifications for a specific employee (receiver) - No sender info
    public List<Map<String, Object>> getNotificationsByReceiver(Long employeeId) {
        String sql = "SELECT n.id AS notification_id, n.message, n.created_at, n.status, e.job_title "
                + "FROM notification n "
                + "JOIN employee e ON e.id = n.sender_id "
                + "WHERE n.receiver_id = ? ORDER BY n.created_at DESC";

        return jdbcTemplate.queryForList(sql, new Object[]{employeeId});
    }



    // Method to mark a notification as read
    public int markAsRead(Long notificationId) {
        String sql = "UPDATE notification SET status = 'READ' WHERE id = ?";
        return jdbcTemplate.update(sql, notificationId);
    }
}
