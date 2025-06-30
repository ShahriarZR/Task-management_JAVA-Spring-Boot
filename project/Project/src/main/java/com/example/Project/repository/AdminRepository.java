package com.example.Project.repository;

import com.example.Project.entity.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

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

    public String getEmployeeEmailById(Long employeeId) {
        String sql = "SELECT email FROM employee WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{employeeId}, String.class);
    }

    public boolean taskExistsById(Long taskId) {
        String sql = "SELECT COUNT(*) FROM task WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, taskId);
        return count != null && count > 0;
    }

    public int assignEmployeeToTask(Long employeeId, Long taskId) {
        String updateTaskSql = "UPDATE task SET employee_id = ? WHERE id = ?";
        int updateCount = jdbcTemplate.update(updateTaskSql, employeeId, taskId);

        String insertEmployeeTaskSql = "INSERT INTO employee_task (employee_id, assigned_task_id, created_at, assigned_at, priority) VALUES (?, ?, ?, ?, ?)";
        int insertCount = jdbcTemplate.update(insertEmployeeTaskSql, employeeId, taskId, LocalDateTime.now(), LocalDateTime.now(), "LOW");

        return updateCount + insertCount;
    }

    public boolean isTaskAssignedToEmployee(Long employeeId, Long taskId) {
        String sql = "SELECT COUNT(*) FROM task WHERE id = ? AND employee_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, taskId, employeeId);
        return count != null && count > 0;
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

    public Task getTaskById(Long taskId) {
        String sql = "SELECT id, title, description, project_type, status, created_at, updated_at, due_date, employee_id, attachment FROM task WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{taskId}, (rs, rowNum) -> {
            Task task = new Task();
            task.setId(rs.getLong("id"));
            task.setTitle(rs.getString("title"));
            task.setDescription(rs.getString("description"));
            task.setProjectType(rs.getString("project_type"));
            task.setStatus(Task.Status.valueOf(rs.getString("status")));
            task.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            task.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            task.setDueDate(rs.getTimestamp("due_date") != null ? rs.getTimestamp("due_date").toLocalDateTime() : null);
            Long employeeId = rs.getLong("employee_id");
            if (employeeId != null && employeeId != 0) {
                // Assuming Employee object can be set with just id for now
                com.example.Project.entity.Employee employee = new com.example.Project.entity.Employee();
                employee.setId(employeeId);
                task.setEmployee(employee);
            }
            task.setAttachment(rs.getString("attachment"));
            return task;
        });
    }

    public int updateTask(Task task) {
        String sql = "UPDATE task SET title = ?, description = ?, project_type = ?, status = ?, updated_at = ?, due_date = ?, employee_id = ?, attachment = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                task.getTitle(),
                task.getDescription(),
                task.getProjectType(),
                task.getStatus().name(),
                task.getUpdatedAt(),
                task.getDueDate(),
                task.getEmployee() != null ? task.getEmployee().getId() : null,
                task.getAttachment(),
                task.getId());
    }

    public java.util.List<Task> getAllTasks() {
        String sql = "SELECT id, title, description, project_type, status, created_at, updated_at, due_date, employee_id, attachment FROM task";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Task task = new Task();
            task.setId(rs.getLong("id"));
            task.setTitle(rs.getString("title"));
            task.setDescription(rs.getString("description"));
            task.setProjectType(rs.getString("project_type"));
            task.setStatus(Task.Status.valueOf(rs.getString("status")));
            task.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            task.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            task.setDueDate(rs.getTimestamp("due_date") != null ? rs.getTimestamp("due_date").toLocalDateTime() : null);
            Long employeeId = rs.getLong("employee_id");
            if (employeeId != null && employeeId != 0) {
                // Assuming Employee object can be set with just id for now
                com.example.Project.entity.Employee employee = new com.example.Project.entity.Employee();
                employee.setId(employeeId);
                task.setEmployee(employee);
            }
            task.setAttachment(rs.getString("attachment"));
            return task;
        });
    }
}
