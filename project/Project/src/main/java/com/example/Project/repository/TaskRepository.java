package com.example.Project.repository;

import com.example.Project.entity.Employee;
import com.example.Project.entity.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class TaskRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Task> findById(Long taskId) {
        String sql = "SELECT * FROM task WHERE id = ?";
        try {
            Task task = jdbcTemplate.queryForObject(sql, new Object[]{taskId}, new TaskRowMapper());
            return Optional.ofNullable(task);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty(); // Return empty if task is not found
        }
    }

    // Save task (to update status)
    public int saveStatus(Task task) {
        String sql = "UPDATE task SET status = ?, updated_at = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                task.getStatus().name(),
                LocalDateTime.now(),
                task.getId());
    }

    public int saveAttachments(Task task) {
        String sql = "UPDATE task SET attachment = ?, updated_at = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                task.getAttachment(),  // Save the file path or URL in the attachment column
                LocalDateTime.now(),
                task.getId());
    }

    // Fetch tasks assigned to the employee by employeeId
    public List<Task> findTasksByEmployeeId(Long employeeId) {
        String sql = "SELECT id, title, description, project_type, status, created_at, updated_at, due_date, attachment, employee_id " +
                "FROM task WHERE employee_id = ?";

        return jdbcTemplate.query(sql, new Object[]{employeeId}, new BeanPropertyRowMapper<>(Task.class));
    }


    public class TaskRowMapper implements RowMapper<Task> {

        @Override
        public Task mapRow(ResultSet rs, int rowNum) throws SQLException {
            Task task = new Task();
            task.setId(rs.getLong("id"));
            task.setTitle(rs.getString("title"));
            task.setDescription(rs.getString("description"));
            task.setProjectType(rs.getString("project_type"));
            task.setStatus(Task.Status.valueOf(rs.getString("status")));
            task.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            task.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            task.setDueDate(rs.getTimestamp("due_date").toLocalDateTime());

            // Fetch the Employee ID associated with the task (assuming it's stored in employee_id)
            Long employeeId = rs.getLong("employee_id");

            // If an employee is assigned to the task, fetch their details
            if (employeeId != 0) {
                Employee employee = new Employee();
                employee.setId(employeeId);
                // Optionally, you can fetch more employee details if required (e.g., name, email, etc.)
                task.setEmployee(employee);  // Set the employee reference in the task
            }

            task.setAttachment(rs.getString("attachment"));  // Map the attachment field
            return task;
        }
    }

    public List<Task> findTasksByTitle(Long employeeId, String keyword) {
        String sql = "SELECT * FROM task WHERE employee_id = ? AND LOWER(title) LIKE LOWER(?)";

        return jdbcTemplate.query(sql, new Object[]{employeeId, "%" + keyword + "%"}, new TaskRowMapper());
    }

    public List<Task> findTasksByStatus(Long employeeId, Task.Status status) {
        String sql = "SELECT * FROM task WHERE employee_id = ? AND status = ?";

        return jdbcTemplate.query(sql, new Object[]{employeeId, status.name()}, new TaskRowMapper());
    }

}
