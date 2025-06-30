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

    // Many tasks can be assigned to one task entity
    @ManyToOne
    @JoinColumn(name = "assigned_task_id", nullable = false)
    private Task assignedTask;

    // Many employee tasks can belong to one employee
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime assignedAt;

    @Column
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

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
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
