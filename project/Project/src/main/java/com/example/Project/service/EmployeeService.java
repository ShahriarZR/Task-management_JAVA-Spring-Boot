package com.example.Project.service;

import com.example.Project.entity.Employee;
import com.example.Project.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public String saveEmployee(Employee employee) {
        if (employeeRepository.findByEmail(employee.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }
        String hashedPassword = passwordEncoder.encode(employee.getPassword());

        employeeRepository.save(employee);

        return "Account created successfully";
    }
}

