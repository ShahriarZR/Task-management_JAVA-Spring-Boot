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
}
