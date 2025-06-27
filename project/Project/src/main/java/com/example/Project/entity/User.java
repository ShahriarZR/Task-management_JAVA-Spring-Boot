package com.example.Project.entity;

import com.example.Project.enums.JobTitle;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 50, unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = true)
    private JobTitle jobTitle;

    @Column(length = 50, unique = true, nullable = false)
    private String phone;

    @Column(length = 100, nullable = false)
    private String address;

    @Column(length = 100, nullable = false)
    private String password;

    @Column(length = 6, nullable = true)
    private String otp;

    @Column(nullable = true)
    private LocalDateTime lastOtpResend;

    @Column(nullable = true)
    private LocalDateTime otpExpiry;

    @Column(nullable = false)
    private boolean isEmailVerified = false;

    @Column(nullable = false)
    private boolean isOtpVerified = false;

    // Getters and Setters

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public JobTitle getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(JobTitle jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public LocalDateTime getLastOtpResend() {
        return lastOtpResend;
    }

    public void setLastOtpResend(LocalDateTime lastOtpResend) {
        this.lastOtpResend = lastOtpResend;
    }

    public LocalDateTime getOtpExpiry() {
        return otpExpiry;
    }

    public void setOtpExpiry(LocalDateTime otpExpiry) {
        this.otpExpiry = otpExpiry;
    }

    public boolean isEmailVerified() {
        return isEmailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        isEmailVerified = emailVerified;
    }

    public boolean isOtpVerified() {
        return isOtpVerified;
    }

    public void setOtpVerified(boolean otpVerified) {
        isOtpVerified = otpVerified;
    }
}
