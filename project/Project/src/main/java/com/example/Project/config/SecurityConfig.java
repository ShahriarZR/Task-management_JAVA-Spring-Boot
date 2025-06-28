package com.example.Project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable() // Disable CSRF for simplicity, adjust as needed
            .authorizeHttpRequests()
                .requestMatchers("/api/employee/registration").permitAll() // Allow unauthenticated access to registration
                .anyRequest().authenticated() // Require authentication for other requests
            .and()
            .httpBasic(); // Use basic authentication for simplicity

        return http.build();
    }
}
