package com.example.Project.service;

import com.example.Project.entity.Task;
import com.example.Project.entity.Task.Status;
import com.example.Project.repository.AdminRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AdminService {

    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public String createTask(Task task) {
        LocalDateTime now = LocalDateTime.now();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        if (task.getStatus() == null) {
            task.setStatus(Status.PENDING);
        }

        int rows = adminRepository.saveTask(task);
        if (rows > 0) {
            return "Task created successfully";
        } else {
            return "Task with the same title and due date already exists";
        }
    }

    public String assignEmployeeToTask(Long employeeId, Long taskId) {
        // Check if employee exists
        boolean employeeExists = adminRepository.employeeExists(employeeId);
        if (!employeeExists) {
            return "Employee with ID " + employeeId + " does not exist";
        }

        // Check if task exists
        boolean taskExists = adminRepository.taskExistsById(taskId);
        if (!taskExists) {
            return "Task with ID " + taskId + " does not exist";
        }

        // Assign employee to task
        int rows = adminRepository.assignEmployeeToTask(employeeId, taskId);
        if (rows > 0) {
            return "Employee assigned to task successfully";
        } else {
            return "Failed to assign employee to task";
        }
    }
}
