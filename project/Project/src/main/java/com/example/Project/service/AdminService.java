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
}
