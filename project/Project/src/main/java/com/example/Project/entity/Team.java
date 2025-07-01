package com.example.Project.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "team")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // The manager of the team (Many teams can have one manager)
    @ManyToOne
    @JoinColumn(name = "manager_id", nullable = false)
    private Employee manager;

    // The HR who created the team (Many teams can be created by one HR)
    @ManyToOne
    @JoinColumn(name = "hr_id", nullable = true)
    private Employee hr;

    // List of employees assigned to the team (Many-to-many relationship)
    @ManyToMany
    @JoinTable(
            name = "team_members",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "employee_id")
    )
    private List<Employee> members = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Boolean approvedByHr = false;  // Default value is false

    // Constructors, Getters, and Setters

    public Team() {
        // Default constructor
    }

    public Team(String name, Employee manager, Employee hr) {
        this.name = name;
        this.manager = manager;
        this.hr = hr;
        this.createdAt = LocalDateTime.now(); // Set createdAt to the current time when the team is created
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Employee getManager() {
        return manager;
    }

    public void setManager(Employee manager) {
        this.manager = manager;
    }

    public Employee getHr() {
        return hr;
    }

    public void setHr(Employee hr) {
        this.hr = hr;
    }

    public List<Employee> getMembers() {
        return members;
    }

    public void setMembers(List<Employee> members) {
        this.members = members;
    }

    public void addMember(Employee employee) {
        if (this.members == null) {
            this.members = new ArrayList<>();
        }
        this.members.add(employee);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getApprovedByHr() {
        return approvedByHr;
    }

    public void setApprovedByHr(Boolean approvedByHr) {
        this.approvedByHr = approvedByHr;
    }
}
