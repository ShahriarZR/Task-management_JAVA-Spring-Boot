package com.example.Project.api;

import com.example.Project.entity.Employee;
import com.example.Project.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/employee")
public class EmployeeApi {

    @Autowired
    private EmployeeService employeeService;

    public EmployeeApi(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("/registration")
    public ResponseEntity<String> registerEmployee(@RequestBody Employee employee) {
        String response = employeeService.saveEmployee(employee);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");
        Map<String, String> response = employeeService.login(email, password);
        if (response.containsKey("error")) {
            return ResponseEntity.status(401).body(response.get("error"));
        } else {
            return ResponseEntity.ok(response.get("message"));
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        String response = employeeService.verifyEmail(email, otp);
        return ResponseEntity.ok(response);
    }
}
