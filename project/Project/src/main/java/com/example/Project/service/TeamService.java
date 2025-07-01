package com.example.Project.service;

import com.example.Project.entity.Employee;
import com.example.Project.entity.Notification;
import com.example.Project.entity.Team;
import com.example.Project.repository.EmployeeRepository;
import com.example.Project.repository.NotificationRepository;
import com.example.Project.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MailerService mailerService;

    @Autowired
    private EmployeeRepository employeeRepository;

    // Method to create a team
    public void createTeam(Team team, String jobTitle, Long managerId) {
        // Ensure the team name is not empty
        if (team.getName() == null || team.getName().isEmpty()) {
            throw new RuntimeException("Team name is required");
        }

        // If the creator is a Manager, include them as the manager but don't add them to the members list
        if (jobTitle.equals("MANAGER")) {
            Employee manager = employeeRepository.findById(managerId)
                    .orElseThrow(() -> new RuntimeException("Manager not found"));

            // Set the approvedByHr field to false as the Manager cannot approve their own team
            team.setApprovedByHr(false);

            // Set the manager for the team (manager will not be added to the members list)
            team.setManager(manager);
        } else {
            throw new RuntimeException("Only Managers are allowed to create a team");
        }

        // Set createdAt to the current time
        team.setCreatedAt(LocalDateTime.now());

        // Save the team in the repository
        teamRepository.save(team);
    }

    public void addMemberToTeam(Long teamId, Long employeeId, Long managerId) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        // Check if the logged-in user is the Manager of the team
        if (team.getManager() == null || !team.getManager().getId().equals(managerId)) {
            throw new RuntimeException("Only the Manager of the team can add members");
        }

        // Check if the employee exists
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Check if the employee is already a member of the team
        if (teamRepository.findEmployeeExists(teamId, employeeId)) {
            throw new RuntimeException("Employee is already a member of the team");
        }

        // Add the employee to the team members
        team.addMember(employee);

        // Save the updated team in the repository
        teamRepository.addTeamMembers(team);
    }

    public void approveTeam(Long teamId, Long hrId) {
        // Fetch the team from the repository
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        Employee hrEmployee = employeeRepository.findById(hrId)
                .orElseThrow(() -> new RuntimeException("HR not found"));

        // Update the team's approval status
        team.setApprovedByHr(true);
        team.setHr(hrEmployee);

        // Save the updated team
        teamRepository.setApproval(team);

        // Send notifications and emails to all team members
        sendApprovalNotificationAndEmails(team);
    }

    // Send notifications and emails to all team members
    private void sendApprovalNotificationAndEmails(Team team) {
        List<Employee> teamMembers = team.getMembers();
        System.out.println(teamMembers.size() + " team members found.");

        for (Employee member : teamMembers) {
            // Create and save a notification for each employee
            Notification notification = new Notification();
            notification.setMessage("Manager added you to team: " + team.getName() + ". It has been approved by HR.");
            notification.setReceiver(member);
            notification.setSender(team.getHr());
            notification.setCreatedAt(LocalDateTime.now());
            notification.setStatus(Notification.Status.UNREAD);

            notificationRepository.saveNotification(notification);  // Save the notification

            // Send email to the employee
            String subject = "Team Approval Notification";
            String body = "Dear " + member.getName() + ",\n\n" +
                    "Manager added you to team: " + team.getName() + " It has been approved by HR.. Please check the team details.\n\n" +
                    "Best Regards,\nHR Team";

            //mailerService.sendNotificationEmail(member.getEmail(), subject, body);  // Send email
        }
    }



}
