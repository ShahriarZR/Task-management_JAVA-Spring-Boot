package com.example.Project.api;

import com.example.Project.entity.Employee;
import com.example.Project.entity.Team;
import com.example.Project.service.TeamService;
import com.example.Project.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager")
public class ManagerApi {

    @Autowired
    private JwtUtil jwtUtil;  // To extract job title from the JWT token

    @Autowired
    private TeamService teamService;  // Service to handle team creation logic

    @PostMapping("/createTeam")
    public ResponseEntity<String> createTeam(@RequestHeader("Authorization") String authHeader, @RequestBody Team team) {
        try {
            // Extract the token from the Authorization header
            String token = authHeader.substring(7);  // "Bearer <token>"

            // Extract job title and manager ID from the token
            String jobTitle = jwtUtil.extractJobTitle(token);  // Extract job title from the token
            Long managerId = jwtUtil.extractEmployeeId(token);  // Extract manager ID from the token

            // Check if the logged-in user is a Manager
            if (!jobTitle.equals("MANAGER")) {
                return new ResponseEntity<>("Only Managers can create a team", HttpStatus.FORBIDDEN);
            }

            // Set the manager ID in the team object using the manager ID extracted from the token
            Employee manager = new Employee();
            manager.setId(managerId);
            team.setManager(manager);

            // Set the team name from the request body (team name is required)
            if (team.getName() == null || team.getName().isEmpty()) {
                return new ResponseEntity<>("Team name is required", HttpStatus.BAD_REQUEST);
            }

            // Create the team
            teamService.createTeam(team, jobTitle, managerId);

            return new ResponseEntity<>("Team created successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error creating team: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/addMemberToTeam")
    public ResponseEntity<String> addMemberToTeam(@RequestHeader("Authorization") String authHeader,
                                                  @RequestParam Long teamId,
                                                  @RequestParam Long employeeId) {
        try {
            // Extract the token from the Authorization header
            String token = authHeader.substring(7);  // "Bearer <token>"

            // Extract job title and manager ID from the token
            String jobTitle = jwtUtil.extractJobTitle(token);  // Extract job title from the token
            Long managerId = jwtUtil.extractEmployeeId(token);  // Extract manager ID from the token

            // Check if the logged-in user is a Manager
            if (!jobTitle.equals("MANAGER")) {
                return new ResponseEntity<>("Only Managers can add members to a team", HttpStatus.FORBIDDEN);
            }

            // Call the service to add the member to the team
            teamService.addMemberToTeam(teamId, employeeId, managerId);

            return new ResponseEntity<>("Member added successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error adding member: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/myTeams")
    public ResponseEntity<?> getTeamsByManager(@RequestHeader("Authorization") String authHeader) {
        try {
            // Extract manager ID from the JWT token
            String token = authHeader.substring(7);  // "Bearer <token>"
            Long managerId = jwtUtil.extractEmployeeId(token);  // Extract manager ID from the token

            // Fetch the teams managed by this manager
            return ResponseEntity.ok(teamService.getTeamsByManagerId(managerId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching teams: " + e.getMessage());
        }
    }

    @DeleteMapping("/teams/{teamId}")
    public ResponseEntity<String> deleteTeam(@RequestHeader("Authorization") String authHeader, @PathVariable Long teamId) {
        try {
            // Extract the token from the Authorization header
            String token = authHeader.substring(7);  // "Bearer <token>"

            // Extract manager ID from the token
            Long managerId = jwtUtil.extractEmployeeId(token);

            // Call the service method to delete the team
            teamService.deleteTeam(teamId, managerId);

            return ResponseEntity.ok("Team deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting team: " + e.getMessage());
        }
    }


}

