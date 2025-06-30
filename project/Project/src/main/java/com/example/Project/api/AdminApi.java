package com.example.Project.api;

import com.example.Project.entity.Employee;
import com.example.Project.entity.Task;
import com.example.Project.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/all-tasks")
    public ResponseEntity<java.util.List<Task>> getAllTasks() {
        java.util.List<Task> tasks = adminService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/all-employees")
    public ResponseEntity<List<Map<String, Object>>> getAllEmployees() {
        List<Map<String, Object>> employees = adminService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    @DeleteMapping("/deleteTask/{taskId}")
    public ResponseEntity<String> deleteTask(@PathVariable Long taskId) {
        String responseMessage = adminService.deleteTask(taskId);

        // Check the response message and set the appropriate status code
        if (responseMessage.contains("does not exist")) {
            // If task doesn't exist, return 404 Not Found
            return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
        } else if (responseMessage.contains("Failed")) {
            // If task deletion failed, return 500 Internal Server Error
            return new ResponseEntity<>(responseMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            // Success: task deleted successfully
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        }
    }


}
