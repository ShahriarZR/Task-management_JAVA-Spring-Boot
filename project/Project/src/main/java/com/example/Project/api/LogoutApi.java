package com.example.Project.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api")
public class LogoutApi {

    @PostMapping("/logout")
    @ResponseBody
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        // Get the current authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = "unknown";

        if (authentication != null && authentication.getPrincipal() != null) {
            username = authentication.getPrincipal().toString();
        }

        // Use SecurityContextLogoutHandler to invalidate the session
        LogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(request, response, authentication);

        // Return a message after successful logout
        return "User " + username + " has been successfully logged out.";
    }
}