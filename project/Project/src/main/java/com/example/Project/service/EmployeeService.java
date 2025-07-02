package com.example.Project.service;

import com.example.Project.entity.Employee;
import com.example.Project.entity.EmployeeTask;
import com.example.Project.entity.Notification;
import com.example.Project.entity.Task;
import com.example.Project.entity.Team;
import com.example.Project.enums.JobTitle;
import com.example.Project.enums.Role;
import com.example.Project.repository.EmployeeRepository;

import com.example.Project.repository.EmployeeTaskRepository;
import com.example.Project.repository.TaskRepository;
import com.example.Project.util.JwtUtil;
import com.example.Project.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final MailerService mailerService;
    private final JwtUtil jwtUtil;
    private final TaskRepository taskRepository;
    private final EmployeeTaskRepository employeeTaskRepository;

    @Autowired
    private TeamService teamService;

    @Value("${file.upload-dir}")  // Inject the value from application.properties
    private String uploadDir;  // Path to store files

    public EmployeeService(EmployeeRepository employeeRepository, MailerService mailerService, JwtUtil jwtUtil, TaskRepository taskRepository, EmployeeTaskRepository employeeTaskRepository) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.mailerService = mailerService;
        this.jwtUtil = jwtUtil;
        this.taskRepository = taskRepository;
        this.employeeTaskRepository = employeeTaskRepository;
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
            String token = jwtUtil.generateToken(
                    employee.getId(),
                    employee.getName(),
                    employee.getEmail(),
                    employee.getRole().name(),
                    employee.getJobTitle().name() // Include job title in the token
            );

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

        String role = existingEmployee.get().getJobTitle().name();

        // Check if the employee to be deleted is a Manager or HR
        if (role.equals("MANAGER") || role.equals("HR")) {
            Employee loggedInEmployee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("Logged-in employee not found"));

            // Ensure that the logged-in Manager or HR is not attempting to delete their own record
            if (loggedInEmployee.getId().equals(employeeId)) {
                throw new RuntimeException("A manager or HR cannot delete themselves directly.");
            }
        }

        // Check if employee exists in any team(s) and remove from those teams first
        List<Team> teams = teamService.getTeamsByEmployeeId(employeeId);
        for (Team team : teams) {
            teamService.removeEmployeeFromTeam(team.getId(), employeeId);
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

    public String handleForgotPassword(String email) {
        // Step 1: Fetch the employee by email
        Employee employee = employeeRepository.findByEmail(email);

        if (employee == null) {
            throw new RuntimeException("Employee with email " + email + " does not exist.");
        }

        // Step 2: Generate a unique OTP
        String otp = generateOtp();

        // Step 3: Set the OTP in the employee record (with an expiry time)
        LocalDateTime otpExpiry = LocalDateTime.now().plusMinutes(10); // OTP valid for 10 minutes
        employee.setOtp(otp);
        employee.setOtpExpiry(otpExpiry);

        // Save OTP and expiry in the database
        employeeRepository.saveEmployee(employee);

        // Step 4: Send OTP to the employee's email
        String subject = "Password Reset OTP";
        String body = "Your OTP to reset your password is: " + otp + "\nIt will expire in 10 minutes.";

        mailerService.sendNotificationEmail(employee.getEmail(), subject, body);

        return "OTP sent to your email address.";
    }

    public String verifyOtpAndResetPassword(String email, String otp, String newPassword) {
        // Step 1: Fetch the employee by email
        Employee employee = employeeRepository.findByEmail(email);

        if (employee == null) {
            throw new RuntimeException("Employee with email " + email + " does not exist.");
        }

        // Step 2: Check if the OTP matches and is not expired
        if (employee.getOtp() == null || !employee.getOtp().equals(otp)) {
            throw new RuntimeException("OTP is incorrect.");
        }

        if (employee.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired.");
        }

        // Step 3: Encrypt the new password
        String encryptedNewPassword = passwordEncoder.encode(newPassword);

        // Step 4: Update the employee's password in the database
        employee.setPassword(encryptedNewPassword);

        // Clear the OTP after the password reset (for security)
        employee.setOtp(null);
        employee.setOtpExpiry(null);

        // Save the updated employee
        employeeRepository.saveEmployee(employee);

        return "Password has been successfully reset.";
    }

    public List<Task> getAssignedTasks(Long employeeId) {
        // Fetch the tasks assigned to the employee from the repository
        return taskRepository.findTasksByEmployeeId(employeeId);
    }

    public String updateTaskStatus(Long employeeId, Long taskId, String status) {
        // Step 1: Check if the employee is assigned to the task
        EmployeeTask employeeTask = employeeTaskRepository.findByEmployeeIdAndTaskId(employeeId, taskId);

        if (employeeTask == null) {
            throw new RuntimeException("Employee is not assigned to this task.");
        }

        // Step 2: Update the task's status
        Task task = taskRepository.findById(taskId).orElse(null);

        if (task == null) {
            throw new RuntimeException("Task not found.");
        }

        // Update status in the Task entity
        task.setStatus(Task.Status.valueOf(status));
        taskRepository.saveStatus(task);  // Save updated task status

        // Step 3: If the status is "IN_PROGRESS" and startedAt is null, set startedAt to now
        if ("IN_PROGRESS".equals(status) && employeeTask.getStartedAt() == null) {
            employeeTask.setStartedAt(LocalDateTime.now());  // Set current time as startedAt
            employeeTaskRepository.startedAt(employeeTask);  // Save updated startedAt in EmployeeTask
        }
        if ("COMPLETED".equals(status) && employeeTask.getCompletedAt() == null) {
            employeeTask.setCompletedAt(LocalDateTime.now());  // Set current time as startedAt
            employeeTaskRepository.completedAt(employeeTask);  // Save updated startedAt in EmployeeTask
        }

        return "Task status updated successfully.";
    }

    public List<Task> searchTasksByTitle(Long employeeId, String keyword) {
        // Call the repository to search for tasks by title
        return taskRepository.findTasksByTitle(employeeId, "%" + keyword + "%");  // Using LIKE query
    }

    public List<Task> filterTasksByStatus(Long employeeId, String status) {
        // Validate the status input
        try {
            Task.Status taskStatus = Task.Status.valueOf(status.toUpperCase());  // Ensure status is valid

            // Call the repository to filter tasks by status
            return taskRepository.findTasksByStatus(employeeId, taskStatus);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status provided.");
        }
    }

    public String uploadAttachment(Long employeeId, Long taskId, MultipartFile file) {
        // Step 1: Validate the task and employee
        Task task = taskRepository.findById(taskId).orElse(null);

        if (task == null) {
            throw new RuntimeException("Task not found.");
        }

        Employee taskEmployee = task.getEmployee();
        if (taskEmployee == null || !employeeId.equals(taskEmployee.getId())) {
            throw new RuntimeException("Employee is not assigned to this task.");
        }

        // Step 2: Save the file to the server or cloud storage
        String savedFileName = saveFile(file);  // Generate the saved file name
        String originalFileName = file.getOriginalFilename();  // Use original file name

        // Step 3: Update the task's attachment with the original file name (overwriting the existing attachment reference)
        task.setAttachment(originalFileName);  // Store the original file name in the 'attachment' column

        // Save the task with the updated attachment field
        taskRepository.saveAttachments(task);

        return "Attachment uploaded successfully.";
    }


    // Method to save the file locally
    private String saveFile(MultipartFile file) {
        try {
            // Generate a unique file name to avoid overwriting existing files
            String fileName = UUID.randomUUID().toString() + "-" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);  // Make sure `uploadDir` exists

            // Save the file to the specified directory
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return fileName;  // Return the saved file name (this is only for storage on the server)
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }

}