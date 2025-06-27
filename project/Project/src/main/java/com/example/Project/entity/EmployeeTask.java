package com.example.Project.entity;

import com.example.Project.enums.Priority;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_task")
public class EmployeeTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "assignedTaskId", nullable = false)
    private Task assignedTask;

    @ManyToOne(optional = false, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private LocalDateTime assignedAt;

    @Column(nullable = true)
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.LOW;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Task getAssignedTask() {
        return assignedTask;
    }

    public void setAssignedTask(Task assignedTask) {
        this.assignedTask = assignedTask;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }
}
