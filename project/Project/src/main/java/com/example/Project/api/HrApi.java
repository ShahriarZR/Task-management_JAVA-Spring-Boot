package com.example.Project.api;

import com.example.Project.service.TeamService;
import com.example.Project.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hr")
public class HrApi {
    @Autowired
    private final JwtUtil jwtUtil;
    private final TeamService teamService;

    public HrApi(JwtUtil jwtUtil, TeamService teamService) {
        this.jwtUtil = jwtUtil;
        this.teamService = teamService;
    }

    @PostMapping("/approveTeam")
    public ResponseEntity<String> approveTeam(@RequestHeader("Authorization") String authHeader, @RequestParam Long teamId) {
        try {
            // Extract the token from the Authorization header
            String token = authHeader.substring(7);  // "Bearer <token>"

            // Extract HR ID from the token
            String jobTitle = jwtUtil.extractJobTitle(token);
            Long hrId = jwtUtil.extractEmployeeId(token);  // Extract HR ID from the token

            if (!jobTitle.equals("HR")) {
                return new ResponseEntity<>("Only HR can approve a team", HttpStatus.FORBIDDEN);
            }

            // Approve the team
            teamService.approveTeam(teamId, hrId);

            return new ResponseEntity<>("Team approved successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error approving team: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
