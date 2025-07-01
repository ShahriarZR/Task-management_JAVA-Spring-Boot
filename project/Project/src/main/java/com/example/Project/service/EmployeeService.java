package com.example.Project.service;

import com.example.Project.entity.Employee;
import com.example.Project.entity.Notification;
import com.example.Project.enums.JobTitle;
import com.example.Project.enums.Role;
import com.example.Project.repository.EmployeeRepository;

import com.example.Project.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
                String body = "Dear employee, \n\nYour application has been approved. Your Designation is: " + jobTitle.name();
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
        try {
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
            employee.setPassword(passwordEncoder.encode(employee.getPassword()));

            int rows = employeeRepository.saveEmployee(employee);
            if (rows > 0) {
                mailerService.sendOtpEmail(employee.getEmail(), otp);
                return "OTP sent to your email. Please verify to complete registration.";
            } else {
                throw new IllegalArgumentException("Employee registration failed due to database error.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error saving employee: " + e.getMessage(), e);
        }
    }

    public String verifyEmail(String email, String otp) {
        try {
            Employee employee = employeeRepository.findByEmail(email);
            if (employee == null) {
                throw new IllegalArgumentException("User not found.");
            }
            if (employee.isEmailVerified()) {
                throw new IllegalStateException("Email already verified.");
            }
            System.out.println(employee.getOtpExpiry());
            if (employee.getOtp() == null || employee.getOtpExpiry() == null || LocalDateTime.now().isAfter(employee.getOtpExpiry())) {
                throw new IllegalArgumentException("OTP expired.");
            }
            if (!employee.getOtp().equals(otp)) {
                throw new IllegalArgumentException("Incorrect OTP.");
            }

            // Set email verified and clear OTP related fields
            employee.setEmailVerified(true);
            employee.setLastOtpResend(null);
            employee.setOtp(null);
            employee.setOtpExpiry(null);
            employee.setApprovedByAdmin(false);

            employeeRepository.updateEmployeeVerification(employee);
            return "Email verified successfully. Waiting for admin approval.";
        } catch (Exception e) {
            throw new RuntimeException("Error verifying email: " + e.getMessage(), e);
        }
    }

    public Map<String, String> login(String email, String password) {
        try {
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
                response.put("error", "Please verify your email before logging in.");
                return response;
            }
            if (!employee.isApprovedByAdmin()) {
                response.put("error", "Wait for Admin to approve your account before logging in.");
                return response;
            }
            String token = jwtUtil.generateToken(employee.getId(), employee.getName(), employee.getEmail(), employee.getRole().name());
            response.put("access_token", token);
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Error logging in: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> getUnapprovedEmployees() {
        return employeeRepository.getUnapprovedEmployees();  // Call the repository method to fetch unapproved employees
    }

    public String sendNotification(Long senderId, Long receiverId, String message) {
        // Get the sender and receiver Employee objects (assuming they are retrieved from the database)
        Employee sender = new Employee();  // Fetch sender from DB based on senderId
        sender.setId(senderId);
        Employee receiver = new Employee();  // Fetch receiver from DB based on receiverId
        receiver.setId(receiverId);

        Notification notification = new Notification();
        notification.setSender(sender);
        notification.setReceiver(receiver);
        notification.setMessage(message);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setStatus(Notification.Status.UNREAD);  // Set status to UNREAD by default

        int rowsAffected = employeeRepository.sendNotification(notification);
        if (rowsAffected > 0) {
            return "Notification sent successfully.";
        } else {
            return "Failed to send notification.";
        }
    }

    public String updateEmpData(Long employeeId, Map<String, Object> data) {
        // Fetch the employee by ID
        Employee existingEmployee = employeeRepository.findById(employeeId).orElse(null);
        if (existingEmployee == null) {
            throw new RuntimeException("Employee with ID " + employeeId + " does not exist.");
        }

        // Update only non-null fields
        if (data.containsKey("name") && data.get("name") != null) {
            existingEmployee.setName((String) data.get("name"));
        }
        if (data.containsKey("email") && data.get("email") != null) {
            existingEmployee.setEmail((String) data.get("email"));
        }
        if (data.containsKey("phone") && data.get("phone") != null) {
            existingEmployee.setPhone((String) data.get("phone"));
        }
        if (data.containsKey("address") && data.get("address") != null) {
            existingEmployee.setAddress((String) data.get("address"));
        }
        if (data.containsKey("jobTitle") && data.get("jobTitle") != null) {
            throw new RuntimeException("You cannot update your job title. Please contact your administrator for assistance.");
        }

        // Save the updated employee data
        employeeRepository.saveEmployee(existingEmployee);

        return "Employee data updated successfully.";
    }

    public String deleteEmployee(Long employeeId) {
        // Check if the employee exists
        Optional<Employee> existingEmployee = employeeRepository.findById(employeeId);
        if (!existingEmployee.isPresent()) {
            throw new RuntimeException("Employee with ID " + employeeId + " does not exist.");
        }

        int updatedTasks = employeeRepository.setEmployeeIdToNullTask(employeeId);
        int updatedEmployeeTasks = employeeRepository.deleteEmployeeTask(employeeId);
        int updatedNotifications = employeeRepository.deleteEmployeeNotification(employeeId);

        // Delete the employee by ID
        int rowsAffected = employeeRepository.deleteEmployeeById(employeeId);
        if (rowsAffected > 0) {
            return "Employee with ID " + employeeId + " has been deleted successfully.";
        } else {
            throw new RuntimeException("Failed to delete employee with ID " + employeeId);
        }
    }

    public Map<String, Object> getEmployeeDetails(Long employeeId) {
        // Fetch the employee by ID
        Optional<Employee> employeeOptional = employeeRepository.findById(employeeId);

        if (employeeOptional.isPresent()) {
            Employee employee = employeeOptional.get();

            // Create a map to store the filtered employee details
            Map<String, Object> employeeDetails = new HashMap<>();

            // Only add the fields we want to include in the response
            employeeDetails.put("id", employee.getId());
            employeeDetails.put("name", employee.getName());
            employeeDetails.put("email", employee.getEmail());
            employeeDetails.put("jobTitle", employee.getJobTitle().name()); // Assuming jobTitle is an enum
            employeeDetails.put("phone", employee.getPhone());
            employeeDetails.put("address", employee.getAddress());

            // Return the filtered map
            return employeeDetails;
        } else {
            return null;  // If employee is not found, return null
        }
    }


    public String changePassword(Long employeeId, String oldPassword, String newPassword) {
        // Fetch the employee by ID
        Employee employee = employeeRepository.findById(employeeId).orElse(null);

        if (employee == null) {
            throw new RuntimeException("Employee with ID " + employeeId + " does not exist.");
        }

        // Check if the old password matches the stored password
        if (!passwordEncoder.matches(oldPassword, employee.getPassword())) {
            throw new RuntimeException("Old password is incorrect.");
        }

        // Encrypt the new password
        String encryptedNewPassword = passwordEncoder.encode(newPassword);

        // Update the employee password
        employee.setPassword(encryptedNewPassword);
        employeeRepository.saveEmployee(employee);

        return "Password updated successfully.";
    }
}