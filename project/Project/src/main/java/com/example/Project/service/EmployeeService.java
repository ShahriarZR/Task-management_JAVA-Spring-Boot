package com.example.Project.service;

import com.example.Project.entity.Employee;
import com.example.Project.enums.JobTitle;
import com.example.Project.enums.Role;
import com.example.Project.repository.EmployeeRepository;

import com.example.Project.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final MailerService mailerService;
    private final JwtUtil jwtUtil;

    public EmployeeService(EmployeeRepository employeeRepository, MailerService mailerService, JwtUtil jwtUtil) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.mailerService = mailerService;
        this.jwtUtil = jwtUtil;
    }

    public String approveEmployee(Long employeeId, JobTitle jobTitle) {
        // Check if the employee exists
        boolean employeeExists = employeeRepository.employeeExists(employeeId);
        if (!employeeExists) {
            throw new IllegalArgumentException("Employee with ID " + employeeId + " does not exist");
        }

        // Proceed to approve the employee and update the job title
        int rowsAffected = employeeRepository.approveEmployeeById(employeeId, jobTitle);
        if (rowsAffected > 0) {
            // Get the employee's email to send the approval notification
            String employeeEmail = employeeRepository.getEmployeeEmailById(employeeId);
            if (employeeEmail != null && !employeeEmail.isEmpty()) {
                String subject = "Your Employee Status Has Been Approved";
                String body = "Dear employee, \n\nYour application has been approved. Your job title is now: " + jobTitle.name();
                mailerService.sendNotificationEmail(employeeEmail, subject, body);
            }
            return "Employee with ID " + employeeId + " has been approved successfully with job title: " + jobTitle.name();
        } else {
            return "Failed to approve employee with ID " + employeeId;
        }
    }

    private String generateOtp() {
        String digits = "0123456789";
        StringBuilder otp = new StringBuilder();
        java.security.SecureRandom random = new java.security.SecureRandom();
        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(digits.length());
            otp.append(digits.charAt(index));
        }
        return otp.toString();
    }

    public String saveEmployee(Employee employee) {
        String otp = generateOtp();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = now.plusMinutes(10);

        employee.setOtp(otp);
        employee.setLastOtpResend(now);
        employee.setOtpExpiry(expiry);
        employee.setEmailVerified(false);
        employee.setOtpVerified(false);
        employee.setRole(Role.USER);
        employee.setApprovedByAdmin(false);  // Ensure new employees are not approved by default

        // Encode the password before saving
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));

        int rows = employeeRepository.saveEmployee(employee);
        if (rows > 0) {
            mailerService.sendOtpEmail(employee.getEmail(), otp);
            return "OTP sent to your email. Please verify to complete registration.";
        } else {
            return "Failed to register employee";
        }
    }

    public Map<String, String> login(String email, String password) {
        Employee employee = employeeRepository.findByEmail(email);
        Map<String, String> response = new HashMap<>();
        if (employee == null) {
            response.put("error", "Invalid Username");
            return response;
        }
        if (!passwordEncoder.matches(password, employee.getPassword())) {
            response.put("error", "Wrong Password");
            return response;
        }
        if (!employee.isEmailVerified()) {
            response.put("error", "Please verify your email before logging in");
            return response;
        }
        System.out.println(employee.isApprovedByAdmin());
        if (!employee.isApprovedByAdmin()) {
            response.put("error", "Wait for Admin to approve your account before logging in");
            return response;
        }
        String token = jwtUtil.generateToken(employee.getId(), employee.getName(), employee.getEmail(), employee.getRole().name());
        response.put("access_token", token);
        return response;
    }

    public String verifyEmail(String email, String otp) {
        Employee employee = employeeRepository.findByEmail(email);
        if (employee == null) {
            return "User not found";
        }
        if (employee.isEmailVerified()) {
            return "Email already verified";
        }
        if (employee.getOtp() == null || employee.getOtpExpiry() == null || LocalDateTime.now().isAfter(employee.getOtpExpiry())) {
            return "OTP expired";
        }
        if (!employee.getOtp().equals(otp)) {
            return "Incorrect OTP";
        }

        // Set email verified and clear OTP related fields
        employee.setEmailVerified(true);
        employee.setLastOtpResend(null);
        employee.setOtp(null);
        employee.setOtpExpiry(null);
        employee.setApprovedByAdmin(false);  // Ensure it is set to false during the email verification process

        // Update employee record in the database
        employeeRepository.updateEmployeeVerification(employee);

        // Re-fetch the employee to ensure the updated values
        employee = employeeRepository.findByEmail(email);

        // Now the approvedByAdmin value should be up-to-date
        System.out.println(employee.isApprovedByAdmin());  // This should print the updated value.

        return "Email verified successfully. Waiting for admin approval. You will receive an email when approved.";
    }

}
