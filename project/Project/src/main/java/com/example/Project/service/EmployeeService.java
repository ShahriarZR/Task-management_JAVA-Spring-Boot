package com.example.Project.service;

import com.example.Project.entity.Employee;
import com.example.Project.repository.EmployeeRepository;
import com.example.Project.service.MailerService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final MailerService mailerService;

    public EmployeeService(EmployeeRepository employeeRepository, MailerService mailerService) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.mailerService = mailerService;
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

    public Employee login(String email, String password) {
        Employee employee = employeeRepository.findByEmail(email);
        if (employee == null) {
            throw new RuntimeException("User not found");
        }
        if (passwordEncoder.matches(password, employee.getPassword())) {
            return employee;
        } else {
            throw new RuntimeException("Invalid password");
        }
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

        employee.setEmailVerified(true);
        employee.setLastOtpResend(null);
        employee.setOtp(null);
        employee.setOtpExpiry(null);

        employeeRepository.updateEmployeeVerification(employee);

        return "Email verified successfully";
    }
}
