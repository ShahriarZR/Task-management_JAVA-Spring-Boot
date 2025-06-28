package com.example.Project.repository;

import com.example.Project.entity.Employee;
import com.example.Project.enums.JobTitle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class EmployeeRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public EmployeeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int saveEmployee(Employee employee) {
        String sql = "INSERT INTO employee (name, email, job_title, phone, address, password) VALUES (?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql,
                employee.getName(),
                employee.getEmail(),
                employee.getJobTitle() != null ? employee.getJobTitle().name() : null,
                employee.getPhone(),
                employee.getAddress(),
                employee.getPassword());
    }

    public Employee findByEmail(String email) {
        String sql = "SELECT * FROM employee WHERE email = ?";
        List<Employee> employees = jdbcTemplate.query(sql, new Object[]{email}, new EmployeeRowMapper());
        if (employees.isEmpty()) {
            return null;
        }
        return employees.get(0);
    }

    private static class EmployeeRowMapper implements RowMapper<Employee> {
        @Override
        public Employee mapRow(ResultSet rs, int rowNum) throws SQLException {
            Employee employee = new Employee();
            employee.setId(rs.getLong("id"));
            employee.setName(rs.getString("name"));
            employee.setEmail(rs.getString("email"));
            String jobTitleStr = rs.getString("job_title");
            if (jobTitleStr != null) {
                employee.setJobTitle(JobTitle.valueOf(jobTitleStr));
            }
            employee.setPhone(rs.getString("phone"));
            employee.setAddress(rs.getString("address"));
            employee.setPassword(rs.getString("password"));
            return employee;
        }
    }
}
