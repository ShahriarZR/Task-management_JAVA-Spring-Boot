package com.example.Project.api;

import com.example.Project.entity.Task;
import com.example.Project.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminApi {

    private final AdminService adminService;

    public AdminApi(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/createTask")
    public ResponseEntity<String> createTask(@RequestBody Task task) {
        String response = adminService.createTask(task);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assignEmployeeToTask")
    public ResponseEntity<String> assignEmployeeToTask(@RequestParam Long employeeId, @RequestParam Long taskId) {
        String response = adminService.assignEmployeeToTask(employeeId, taskId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/updateTask")
    public ResponseEntity<String> updateTask(@RequestParam Long taskId, @RequestBody Task task) {
        String response = adminService.updateTask(taskId, task);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tasks")
    public ResponseEntity<java.util.List<Task>> getAllTasks() {
        java.util.List<Task> tasks = adminService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }
}
