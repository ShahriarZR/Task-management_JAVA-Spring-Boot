package com.example.Project.service;

import com.example.Project.entity.Employee;
import com.example.Project.repository.EmployeeRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public String saveEmployee(Employee employee) {
        // Encode the password before saving
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        int rows = employeeRepository.saveEmployee(employee);
        if (rows > 0) {
            return "Employee registered successfully";
        } else {
            return "Failed to register employee";
        }
    }

    public Employee login(String email, String password) {
        Employee employee = employeeRepository.findByEmail(email);
        if (employee == null) {
            throw new RuntimeException("User not found");
        }
        if (passwordEncoder.matches(password, employee.getPassword())) {
            return employee;
        } else {
            throw new RuntimeException("Invalid password");
        }
    }
}

