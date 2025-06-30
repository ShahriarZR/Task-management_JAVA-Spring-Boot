package com.example.Project.api;

import com.example.Project.entity.Employee;
import com.example.Project.entity.Task;
import com.example.Project.enums.JobTitle;
import com.example.Project.service.AdminService;
import com.example.Project.service.EmployeeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminApi {

    private final AdminService adminService;
    private final EmployeeService employeeService;

    public AdminApi(AdminService adminService, EmployeeService employeeService) {
        this.adminService = adminService;
        this.employeeService = employeeService;
    }

    @PostMapping("/createTask")
    public ResponseEntity<String> createTask(@RequestBody Task task) {
        try {
            String response = adminService.createTask(task);
            if (response.contains("already exists")) {
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // Task already exists
            }
            return new ResponseEntity<>(response, HttpStatus.OK); // Success
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to create task", HttpStatus.INTERNAL_SERVER_ERROR); // Server error
        }
    }


    @PostMapping("/assignEmployeeToTask")
    public ResponseEntity<String> assignEmployeeToTask(@RequestParam Long employeeId, @RequestParam Long taskId) {
        try {
            // Attempt to assign employee to task
            String response = adminService.assignEmployeeToTask(employeeId, taskId);
            return new ResponseEntity<>(response, HttpStatus.OK); // Return success with 200 OK
        } catch (IllegalArgumentException e) {
            // If employee or task doesn't exist, return error with 400 Bad Request
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST); // Return error message with 400
        } catch (Exception e) {
            // Catch any other unexpected errors
            return new ResponseEntity<>("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR); // Return error with 500
        }
    }



    @PutMapping("/updateTask")
    public ResponseEntity<String> updateTask(@RequestParam Long taskId, @RequestBody Task task) {
        try {
            String response = adminService.updateTask(taskId, task);
            return new ResponseEntity<>(response, HttpStatus.OK); // Success
        } catch (AdminService.TaskNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND); // Task not found
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST); // Invalid employee ID
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to update task", HttpStatus.INTERNAL_SERVER_ERROR); // General server error
        }
    }



    @GetMapping("/all-tasks")
    public ResponseEntity<List<Task>> getAllTasks() {
        try {
            List<Task> tasks = adminService.getAllTasks();
            if (tasks.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND); // No tasks found
            }
            return new ResponseEntity<>(tasks, HttpStatus.OK); // Success
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Server error
        }
    }


    @GetMapping("/all-employees")
    public ResponseEntity<List<Map<String, Object>>> getAllEmployees() {
        try {
            List<Map<String, Object>> employees = adminService.getAllEmployees();
            if (employees.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND); // No employees found
            }
            return new ResponseEntity<>(employees, HttpStatus.OK); // Success
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); // Server error
        }
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

    @DeleteMapping("/terminateEmployee/{employeeId}")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long employeeId) {
        try {
            // Attempt to delete the employee
            String response = adminService.deleteEmployee(employeeId);
            if (response.contains("does not exist")) {
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND); // Employee not found
            }
            return new ResponseEntity<>(response, HttpStatus.OK); // Success
        } catch (IllegalArgumentException e) {
            // Catch the case where the employee doesn't exist
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND); // Return 404 for invalid employee ID
        } catch (Exception e) {
            // General server error for unexpected failures
            return new ResponseEntity<>("Failed to delete employee", HttpStatus.INTERNAL_SERVER_ERROR); // Server error
        }
    }

    @PutMapping("/approveEmployee/{employeeId}")
    public ResponseEntity<String> approveEmployee(@PathVariable Long employeeId, @RequestParam JobTitle jobTitle) {
        try {
            String response = employeeService.approveEmployee(employeeId, jobTitle);
            return new ResponseEntity<>(response, HttpStatus.OK); // Success
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND); // Employee not found
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to approve employee", HttpStatus.INTERNAL_SERVER_ERROR); // General error
        }
    }



}
