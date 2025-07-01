package com.example.Project.service;

import com.example.Project.entity.Employee;
import com.example.Project.entity.Notification;
import com.example.Project.entity.Task;
import com.example.Project.entity.Task.Status;
import com.example.Project.enums.JobTitle;
import com.example.Project.repository.AdminRepository;
import com.example.Project.repository.EmployeeRepository;
import com.example.Project.repository.NotificationRepository;
import com.example.Project.service.MailerService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final EmployeeRepository employeeRepository;
    private final AdminRepository adminRepository;
    private final MailerService mailerService;
    private final NotificationRepository notificationRepository;

    @Autowired
    public AdminService(AdminRepository adminRepository, EmployeeRepository employeeRepository, MailerService mailerService, NotificationRepository notificationRepository) {
        this.adminRepository = adminRepository;
        this.employeeRepository = employeeRepository;
        this.mailerService = mailerService;
        this.notificationRepository = notificationRepository;

    }

    public static class TaskNotFoundException extends RuntimeException {
        public TaskNotFoundException(String message) {
            super(message);
        }
    }

    public String createTask(Task task) {
        LocalDateTime now = LocalDateTime.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        if (task.getStatus() == null) {
            task.setStatus(Status.PENDING);
        }

        try {
            int result = adminRepository.saveTask(task);
            if (result > 0) {
                return "Task created successfully";
            } else {
                throw new IllegalArgumentException("Task already exists");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to create task", e);
        }
    }

    public List<Task> getAllTasks() {
        return adminRepository.getAllTasks();
    }

    public String assignEmployeeToTask(Long employeeId, Long taskId, Long senderId) {
        // Check if employee exists
        boolean employeeExists = adminRepository.employeeExists(employeeId);
        if (!employeeExists) {
            throw new IllegalArgumentException("Employee with ID " + employeeId + " does not exist");
        }

        // Check if task exists
        boolean taskExists = adminRepository.taskExistsById(taskId);
        if (!taskExists) {
            throw new IllegalArgumentException("Task with ID " + taskId + " does not exist");
        }

        // Check if task is already assigned to the same employee
        boolean alreadyAssigned = adminRepository.isTaskAssignedToEmployee(employeeId, taskId);
        if (alreadyAssigned) {
            return "Task is already assigned to this employee";
        }

        // Proceed to assign the employee to the task
        int rowsAffected = adminRepository.assignEmployeeToTask(employeeId, taskId);
        if (rowsAffected > 1) { // Two updates are performed, so rowsAffected should be greater than 1

            // Create a new notification
            Employee sender = new Employee();
            sender.setId(senderId); // Set the sender (who is assigning the task)

            Employee receiver = new Employee();
            receiver.setId(employeeId); // Set the receiver (the employee being assigned the task)

            Notification notification = new Notification();
            notification.setMessage("You have been assigned a new task with ID: " + taskId + ". Please check your task list for details.");
            notification.setSender(sender);
            notification.setReceiver(receiver);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setStatus(Notification.Status.UNREAD); // Set status to UNREAD by default

            // Save the notification in the database
            notificationRepository.saveNotification(notification);

            // Send a notification email to the employee
            String employeeEmail = adminRepository.getEmployeeEmailById(employeeId);
            if (employeeEmail != null && !employeeEmail.isEmpty()) {
                String subject = "New Task Assigned";
                String body = "You have been assigned a new task with ID: " + taskId + ". Please check your task list for details.";
                mailerService.sendNotificationEmail(employeeEmail, subject, body);
            }

            return "Employee assigned to task successfully and notification sent.";
        } else {
            return "Failed to assign employee to task";
        }
    }

    public String updateTask(Long taskId, Task task, Long senderId) {
        // Check if task exists
        Task existingTask = adminRepository.getTaskById(taskId);
        if (existingTask == null) {
            throw new TaskNotFoundException("Task with ID " + taskId + " does not exist");
        }

        // Check if employee exists (if updated)
        if (task.getEmployee() != null) {
            boolean employeeExists = adminRepository.employeeExists(task.getEmployee().getId());
            if (!employeeExists) {
                throw new IllegalArgumentException("Employee with ID " + task.getEmployee().getId() + " does not exist");
            }
        }

        //System.out.println(task.getEmployee());

        // Update only non-null fields
        if (task.getTitle() != null) {
            existingTask.setTitle(task.getTitle());
        }
        if (task.getDescription() != null) {
            existingTask.setDescription(task.getDescription());
        }
        if (task.getProjectType() != null) {
            existingTask.setProjectType(task.getProjectType());
        }
        if (task.getStatus() != null) {
            existingTask.setStatus(task.getStatus());
        }
        if (task.getDueDate() != null) {
            existingTask.setDueDate(task.getDueDate());
        }
        if (task.getEmployee() != null) {
            existingTask.setEmployee(task.getEmployee());
        }
        if (task.getAttachment() != null) {
            existingTask.setAttachment(task.getAttachment());
        }

        existingTask.setUpdatedAt(LocalDateTime.now());

        int rows = adminRepository.updateTask(existingTask);
        if (rows > 0) {
            // If task is assigned to an employee, store a notification
            if (existingTask.getEmployee() != null) {
                Employee sender = new Employee();
                sender.setId(senderId);  // Setting the sender's ID from the decoded token

                Employee receiver = existingTask.getEmployee(); // The employee assigned to the task (receiver)

                // Create a new notification
                Notification notification = new Notification();
                notification.setMessage("Your task with ID: " + taskId + " has been updated.");
                notification.setSender(sender);  // Sender is the employee who made the change
                notification.setReceiver(receiver);  // Receiver is the employee assigned to the task
                notification.setCreatedAt(LocalDateTime.now());
                notification.setStatus(Notification.Status.UNREAD);  // Set status to UNREAD

                // Save the notification
                notificationRepository.saveNotification(notification);

                // Send a notification email to the assigned employee
                String employeeEmail = adminRepository.getEmployeeEmailById(receiver.getId());
                if (employeeEmail != null && !employeeEmail.isEmpty()) {
                    // Log the email to verify it's being retrieved correctly
                    System.out.println("Sending email to: " + employeeEmail);
                    String subject = "Task Updated";
                    String body = "Your task with ID: " + taskId + " has been updated. Please check the details.";
                    mailerService.sendNotificationEmail(employeeEmail, subject, body);
                }

            }

            return "Task updated successfully";
        } else {
            throw new RuntimeException("Failed to update task");
        }
    }


    public List<Map<String, Object>> getAllEmployees() {
        return adminRepository.getAllEmployees();
    }

    public String deleteTask(Long taskId, Long senderId) {
        // Call the repository to delete the task and get the response message
        String responseMessage = adminRepository.deleteTaskById(taskId);

        // If the task is assigned to an employee, save the notification
        Task task = adminRepository.getTaskById(taskId);
        if (task != null && task.getEmployee() != null) {
            // If task is assigned to an employee, create and save a notification
            Employee sender = new Employee();
            sender.setId(senderId);  // Setting the sender's ID (the employee performing the action)

            Employee receiver = task.getEmployee(); // The employee assigned to the task (receiver)

            // Create a new notification
            Notification notification = new Notification();
            notification.setMessage("Your task with ID: " + taskId + " has been deleted.");
            notification.setSender(sender);  // Sender is the logged-in employee (who is performing the action)
            notification.setReceiver(receiver);  // Receiver is the employee assigned to the task
            notification.setCreatedAt(LocalDateTime.now());
            notification.setStatus(Notification.Status.UNREAD);  // Set status to UNREAD by default

            // Save the notification in the database
            notificationRepository.saveNotification(notification);

            // Send notification email to the assigned employee
            String employeeEmail = adminRepository.getEmployeeEmailById(receiver.getId());
            if (employeeEmail != null && !employeeEmail.isEmpty()) {
                String subject = "Task Deleted";
                String body = "Your task with ID: " + taskId + " has been deleted. Please check your task list.";
                mailerService.sendNotificationEmail(employeeEmail, subject, body);
            }
        }

        return responseMessage;
    }


    public String deleteEmployee(Long employeeId) {
        // Check if employee exists
        boolean employeeExists = adminRepository.employeeExists(employeeId);
        if (!employeeExists) {
            throw new IllegalArgumentException("Employee with ID " + employeeId + " does not exist");
        }

        // Proceed to delete the employee
        int rowsAffected = adminRepository.deleteEmployeeById(employeeId);
        if (rowsAffected > 0) {
            return "Employee with ID " + employeeId + " deleted successfully";
        } else {
            return "Failed to delete employee with ID " + employeeId;
        }
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


}
