package com.example.Project.api;

import com.example.Project.entity.Employee;
import com.example.Project.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
