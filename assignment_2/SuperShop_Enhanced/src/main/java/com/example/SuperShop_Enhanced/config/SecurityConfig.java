package com.example.SuperShop_Enhanced.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.SuperShop_Enhanced.security.JwtRequestFilter;

import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {


    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/users").permitAll()

                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/users/{id}").hasAnyRole("ADMIN", "USER")
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/users/{id}").hasAnyRole("ADMIN", "USER")

                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/invoices/all").hasRole("ADMIN")

                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/orders/allOrders").hasRole("ADMIN")

                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/products/add").hasRole("ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/products/bulk").hasRole("ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/products/update").hasRole("ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/products/expiring-soon").hasRole("ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/products/discounted").hasRole("ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/products/total-by-category").hasRole("ADMIN")

                .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/products/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/wishlists/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/orders/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/orders/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/carts/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/invoices/**").hasAnyRole("ADMIN", "USER")
                .requestMatchers("/api/logs/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
