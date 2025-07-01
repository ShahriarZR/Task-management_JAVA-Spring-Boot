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
        String response = employeeService.saveEmployee(employee);
        
        return ResponseEntity.ok(response);
    }


    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        String response = employeeService.verifyEmail(email, otp);
        return ResponseEntity.ok(response);
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
            e.printStackTrace();  // Log the error for debugging
            return new ResponseEntity<>(List.of(), HttpStatus.BAD_REQUEST); // Return 400
        } catch (Exception e) {
            // Catch all other exceptions
            e.printStackTrace();  // Log the error for debugging
            return new ResponseEntity<>(List.of(), HttpStatus.INTERNAL_SERVER_ERROR); // Return 500
        }
    }


}
