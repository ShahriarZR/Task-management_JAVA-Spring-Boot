package com.example.Project.repository;

import com.example.Project.entity.Employee;
import com.example.Project.entity.Notification;
import com.example.Project.enums.JobTitle;
import com.example.Project.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Repository
public class EmployeeRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public EmployeeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean employeeExists(Long employeeId) {
        String sql = "SELECT COUNT(*) FROM employee WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, employeeId);
        return count != null && count > 0;
    }

    // Method to approve an employee and update the jobTitle
    public int approveEmployeeById(Long employeeId, JobTitle jobTitle) {
        String sql = "UPDATE employee SET approved_by_admin = true, job_title = ? WHERE id = ?";
        return jdbcTemplate.update(sql, jobTitle.name(), employeeId);  // Update job title and approvedByAdmin
    }

    // Method to get the email of the employee by employeeId
    public String getEmployeeEmailById(Long employeeId) {
        String sql = "SELECT email FROM employee WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{employeeId}, String.class);
    }

    public int saveEmployee(Employee employee) {
        String sql = "INSERT INTO employee (name, email, job_title, phone, address, role, password, otp, last_otp_resend, otp_expiry, is_email_verified, is_otp_verified, approved_by_admin) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        return jdbcTemplate.update(sql,
                employee.getName(),
                employee.getEmail(),
                employee.getJobTitle() != null ? employee.getJobTitle().name() : null,
                employee.getPhone(),
                employee.getAddress(),
                employee.getRole() != null ? employee.getRole().name() : null,
                employee.getPassword(),
                employee.getOtp(),
                employee.getLastOtpResend(),
                employee.getOtpExpiry(),
                employee.isEmailVerified(),
                employee.isOtpVerified(),
                employee.isApprovedByAdmin()  // Insert the approvedByAdmin value
        );
    }


    public Employee findByEmail(String email) {
        String sql = "SELECT id, name, email, job_title, phone, address, role, password, otp, last_otp_resend, otp_expiry, is_email_verified, is_otp_verified, approved_by_admin FROM employee WHERE email = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{email}, (rs, rowNum) -> {
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
            employee.setRole(Role.valueOf(rs.getString("role")));
            employee.setPassword(rs.getString("password"));
            employee.setOtp(rs.getString("otp"));

            // Handle possible null value for lastOtpResend and otpExpiry
            Timestamp lastOtpResendTimestamp = rs.getTimestamp("last_otp_resend");
            employee.setLastOtpResend(lastOtpResendTimestamp != null ? lastOtpResendTimestamp.toLocalDateTime() : null);

            Timestamp otpExpiryTimestamp = rs.getTimestamp("otp_expiry");
            employee.setOtpExpiry(otpExpiryTimestamp != null ? otpExpiryTimestamp.toLocalDateTime() : null);

            employee.setEmailVerified(rs.getBoolean("is_email_verified"));
            employee.setOtpVerified(rs.getBoolean("is_otp_verified"));
            employee.setApprovedByAdmin(rs.getBoolean("approved_by_admin"));
            return employee;
        });
    }



    public int updateEmployeeVerification(Employee employee) {
        String sql = "UPDATE employee SET is_email_verified = ?, last_otp_resend = ?, otp = ?, otp_expiry = ?, approved_by_admin = ? WHERE email = ?";
        return jdbcTemplate.update(sql,
                employee.isEmailVerified(),
                employee.getLastOtpResend(),
                employee.getOtp(),
                employee.getOtpExpiry(),
                employee.isApprovedByAdmin(),  // Update approved_by_admin here
                employee.getEmail());
    }

    public List<Map<String, Object>> getUnapprovedEmployees() {
        String sql = "SELECT id, name, email, phone, address, job_title, is_email_verified, approved_by_admin " +
                "FROM employee WHERE approved_by_admin = false"; // Query for employees who are not approved

        return jdbcTemplate.queryForList(sql);
    }

    public int sendNotification(Notification notification) {
        String sql = "INSERT INTO notification (message, sender_id, receiver_id, created_at, status) "
                + "VALUES (?, ?, ?, ?, ?)";

        return jdbcTemplate.update(sql,
                notification.getMessage(),
                notification.getSender().getId(),
                notification.getReceiver().getId(),
                notification.getCreatedAt(),
                notification.getStatus().name());
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
            employee.setOtp(rs.getString("otp"));
            employee.setOtpExpiry(rs.getTimestamp("otp_expiry") != null ? rs.getTimestamp("otp_expiry").toLocalDateTime() : null);
            employee.setLastOtpResend(rs.getTimestamp("last_otp_resend") != null ? rs.getTimestamp("last_otp_resend").toLocalDateTime() : null);
            employee.setEmailVerified(rs.getBoolean("is_email_verified"));
            employee.setOtpVerified(rs.getBoolean("is_otp_verified"));
            String roleStr = rs.getString("role");
            if (roleStr != null) {
                employee.setRole(com.example.Project.enums.Role.valueOf(roleStr));
            }
            return employee;
        }
    }
}
