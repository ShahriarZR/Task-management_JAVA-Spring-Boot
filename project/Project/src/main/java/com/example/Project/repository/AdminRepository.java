package com.example.Project.repository;

import com.example.Project.entity.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AdminRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AdminRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean taskExists(String title, java.time.LocalDateTime dueDate) {
        String sql = "SELECT COUNT(*) FROM task WHERE title = ? AND due_date = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, title, dueDate);
        return count != null && count > 0;
    }

    public boolean employeeExists(Long employeeId) {
        String sql = "SELECT COUNT(*) FROM employee WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, employeeId);
        return count != null && count > 0;
    }

    public boolean taskExistsById(Long taskId) {
        String sql = "SELECT COUNT(*) FROM task WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, taskId);
        return count != null && count > 0;
    }

    public int assignEmployeeToTask(Long employeeId, Long taskId) {
        String sql = "UPDATE task SET employee_id = ? WHERE id = ?";
        return jdbcTemplate.update(sql, employeeId, taskId);
    }

    public int saveTask(Task task) {
        if (taskExists(task.getTitle(), task.getDueDate())) {
            return 0; // Task already exists
        }
        String sql = "INSERT INTO task (title, description, project_type, status, created_at, updated_at, due_date, employee_id, attachment) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                task.getTitle(),
                task.getDescription(),
                task.getProjectType(),
                task.getStatus().name(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getDueDate(),
                task.getEmployee() != null ? task.getEmployee().getId() : null,
                task.getAttachment());
    }
}
