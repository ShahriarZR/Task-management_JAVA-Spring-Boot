package com.example.Project.repository;

import com.example.Project.entity.Employee;
import com.example.Project.entity.EmployeeTask;
import com.example.Project.entity.Task;
import com.example.Project.enums.Priority;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class EmployeeTaskRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public EmployeeTaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Find EmployeeTask by employeeId and taskId
    public EmployeeTask findByEmployeeIdAndTaskId(Long employeeId, Long taskId) {
        String sql = "SELECT * FROM employee_task WHERE employee_id = ? AND assigned_task_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{employeeId, taskId}, new EmployeeTaskRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // Save EmployeeTask (to update startedAt)
    public int startedAt(EmployeeTask employeeTask) {
        String sql = "UPDATE employee_task SET started_at = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                employeeTask.getStartedAt(),
                employeeTask.getId());
    }

    public int completedAt(EmployeeTask employeeTask) {
        String sql = "UPDATE employee_task SET completed_at = ? WHERE id = ?";
        return jdbcTemplate.update(sql,
                employeeTask.getCompletedAt(),
                employeeTask.getId());
    }

    public Optional<EmployeeTask> findByTaskIdAndEmployeeId(Long taskId, Long employeeId) {
        String sql = "SELECT * FROM employee_task WHERE assigned_task_id = ? AND employee_id = ?";

        try {
            EmployeeTask employeeTask = jdbcTemplate.queryForObject(sql, new Object[]{taskId, employeeId}, new BeanPropertyRowMapper<>(EmployeeTask.class));
            return Optional.ofNullable(employeeTask);
        } catch (Exception e) {
            return Optional.empty();  // If no matching EmployeeTask found
        }
    }

    public class EmployeeTaskRowMapper implements RowMapper<EmployeeTask> {

        @Override
        public EmployeeTask mapRow(ResultSet rs, int rowNum) throws SQLException {
            EmployeeTask employeeTask = new EmployeeTask();

            // Map fields from the result set to the entity
            employeeTask.setId(rs.getLong("id"));

            // Map the task (assigned task reference)
            Task task = new Task();
            task.setId(rs.getLong("assigned_task_id"));
            employeeTask.setAssignedTask(task);

            // Map the employee
            Employee employee = new Employee();
            employee.setId(rs.getLong("employee_id"));
            employeeTask.setEmployee(employee);

            // Map createdAt, assignedAt, completedAt, and startedAt (time-related fields)
            employeeTask.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            employeeTask.setAssignedAt(rs.getTimestamp("assigned_at") != null ? rs.getTimestamp("assigned_at").toLocalDateTime() : null);
            employeeTask.setCompletedAt(rs.getTimestamp("completed_at") != null ? rs.getTimestamp("completed_at").toLocalDateTime() : null);
            employeeTask.setStartedAt(rs.getTimestamp("started_at") != null ? rs.getTimestamp("started_at").toLocalDateTime() : null);

            // Map the priority (assuming Priority enum is present)
            employeeTask.setPriority(Priority.valueOf(rs.getString("priority")));

            return employeeTask;
        }
    }

}

