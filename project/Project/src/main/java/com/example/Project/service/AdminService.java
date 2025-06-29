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

        // Check if task is already assigned to the same employee
        boolean alreadyAssigned = adminRepository.isTaskAssignedToEmployee(employeeId, taskId);
        if (alreadyAssigned) {
            return "Task is already assigned to this employee";
        }

        // Assign employee to task
        int rows = adminRepository.assignEmployeeToTask(employeeId, taskId);
        if (rows > 0) {
            return "Employee assigned to task successfully";
        } else {
            return "Failed to assign employee to task";
        }
    }

    public String updateTask(Long taskId, Task task) {
        // Check if task exists
        Task existingTask = adminRepository.getTaskById(taskId);
        if (existingTask == null) {
            return "Task with ID " + taskId + " does not exist";
        }

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

        existingTask.setUpdatedAt(java.time.LocalDateTime.now());

        int rows = adminRepository.updateTask(existingTask);
        if (rows > 0) {
            return "Task updated successfully";
        } else {
            return "Failed to update task";
        }
    }
}
