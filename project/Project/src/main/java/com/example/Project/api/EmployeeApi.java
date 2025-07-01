package com.example.Project.api;

import com.example.Project.entity.Employee;
import com.example.Project.entity.Notification;
import com.example.Project.service.EmployeeService;
import com.example.Project.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import com.example.Project.util.JwtUtil;

@RestController
@RequestMapping("/api/employee")
public class EmployeeApi {

    @Autowired
    private EmployeeService employeeService;
    private final NotificationService notificationService;

    @Autowired
    private JwtUtil jwtUtil;

    public EmployeeApi(EmployeeService employeeService, JwtUtil jwtUtil, NotificationService notificationService) {
        this.employeeService = employeeService;
        this.jwtUtil = jwtUtil;
        this.notificationService = notificationService;
    }

    @PostMapping("/registration")
    public ResponseEntity<String> registerEmployee(@RequestBody Employee employee) {
        try {
            String response = employeeService.saveEmployee(employee);
            return new ResponseEntity<>(response, HttpStatus.OK); // Success
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to register employee: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); // Error
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String otp = request.get("otp");
            String response = employeeService.verifyEmail(email, otp);
            if (response.contains("not found")) {
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND); // Employee not found
            } else if (response.contains("expired") || response.contains("incorrect")) {
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST); // Invalid OTP
            }
            return new ResponseEntity<>(response, HttpStatus.OK); // Success
        } catch (Exception e) {
            return new ResponseEntity<>("Failed to verify email: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR); // Error
        }
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> getNotifications(@RequestHeader("Authorization") String authHeader) {
        try {
            // Extract the token from the Authorization header
            String token = authHeader.substring(7);  // "Bearer <token>"
            Long employeeId = jwtUtil.extractEmployeeId(token);  // Extract employee ID from the token

            // Fetch notifications for the employee and update status to 'READ'
            List<Notification> notifications = notificationService.getAndMarkNotificationsAsRead(employeeId);

            if (notifications.isEmpty()) {
                // If no notifications, return 204 No Content (empty array is returned)
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            // Return notifications with 200 OK status
            return new ResponseEntity<>(notifications, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            // Invalid JWT token or employee ID extraction failure
            return new ResponseEntity<>(List.of(), HttpStatus.BAD_REQUEST); // Return 400
        } catch (Exception e) {
            // Catch all other exceptions
            return new ResponseEntity<>(List.of(), HttpStatus.INTERNAL_SERVER_ERROR); // Return 500
        }
    }

    @PatchMapping("/updateInfo")
    public ResponseEntity<String> updateEmpData(@RequestBody Map<String, Object> data, @RequestHeader("Authorization") String authHeader) {
        // Extract employee id from token (similarly as in the TypeScript code)
        String token = authHeader.substring(7);  // "Bearer <token>"
        Long employeeId = jwtUtil.extractEmployeeId(token);  // Extract employeeId from the token

        String response = employeeService.updateEmpData(employeeId, data);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/details")
    public ResponseEntity<Map<String, Object>> getEmployeeDetails(@RequestHeader("Authorization") String authHeader) {
        // Extract employee id from the token
        String token = authHeader.substring(7);  // "Bearer <token>"
        Long employeeId = jwtUtil.extractEmployeeId(token);  // Extract employeeId from the token

        // Fetch the employee details from the service
        Map<String, Object> employeeDetails = employeeService.getEmployeeDetails(employeeId);

        if (employeeDetails != null) {
            return new ResponseEntity<>(employeeDetails, HttpStatus.OK);  // Employee found
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);  // Employee not found
        }
    }

    @DeleteMapping("/deleteEmployee")
    public ResponseEntity<String> deleteEmployee(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);  // "Bearer <token>"
        Long employeeId = jwtUtil.extractEmployeeId(token);
        try {
            String response = employeeService.deleteEmployee(employeeId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST); // Return error message
        }
    }

    @PostMapping("/changePassword")
    public ResponseEntity<String> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> requestBody) {

        // Extract employee ID from the token
        String token = authHeader.substring(7);  // "Bearer <token>"
        Long employeeId = jwtUtil.extractEmployeeId(token);  // Extract employeeId from the token

        // Extract old and new password from the request body
        String oldPassword = requestBody.get("password");
        String newPassword = requestBody.get("newPassword");

        // Call the service to change the password
        String response = employeeService.changePassword(employeeId, oldPassword, newPassword);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
