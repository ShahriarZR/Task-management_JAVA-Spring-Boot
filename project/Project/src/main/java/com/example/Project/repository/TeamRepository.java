package com.example.Project.repository;

import com.example.Project.entity.Employee;
import com.example.Project.entity.Team;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TeamRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TeamRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Save a new team or update an existing team
    public void save(Team team) {
        String sql = "INSERT INTO team (name, manager_id, created_at, approved_by_hr) VALUES (?, ?, ?, ?)";

        // Insert the team details
        jdbcTemplate.update(sql,
                team.getName(),
                team.getManager().getId(),
                team.getCreatedAt(),
                team.getApprovedByHr());
    }

    public void setApproval(Team team) {
        String sql = "UPDATE team SET approved_by_hr = ?, hr_id = ? WHERE id = ?";

        jdbcTemplate.update(sql,
                team.getApprovedByHr(),
                team.getHr() != null ? team.getHr().getId() : null,  // Set the HR ID if available
                team.getId());  // Update the team with approval status and HR ID
    }



    // Method to add members to the team (team_members join table)
    public void addTeamMembers(Team team) {
        String sql = "INSERT INTO team_members (team_id, employee_id) VALUES (?, ?)";

        for (Employee member : team.getMembers()) {
            jdbcTemplate.update(sql, team.getId(), member.getId());
        }
    }

    // Find a team by ID
    public Optional<Team> findById(Long teamId) {
        String sql = "SELECT t.id, t.name, t.manager_id, e.id as manager_id, e.name as manager_name " +
                "FROM team t LEFT JOIN employee e ON t.manager_id = e.id WHERE t.id = ?";

        try {
            // Fetch the team along with its manager
            Team team = jdbcTemplate.queryForObject(sql, new Object[]{teamId}, (rs, rowNum) -> {
                Team t = new Team();
                t.setId(rs.getLong("id"));
                t.setName(rs.getString("name"));

                // Fetch the manager of the team
                Long managerId = rs.getLong("manager_id");
                if (managerId != null) {
                    Employee manager = new Employee();
                    manager.setId(managerId);
                    manager.setName(rs.getString("manager_name"));
                    t.setManager(manager);  // Set the manager for the team
                }

                return t;
            });

            // Fetch the members of the team
            String membersSql = "SELECT e.id, e.name, e.email FROM employee e " +
                    "JOIN team_members tm ON e.id = tm.employee_id WHERE tm.team_id = ?";

            List<Employee> members = jdbcTemplate.query(membersSql, new Object[]{teamId}, (rs, rowNum) -> {
                Employee member = new Employee();
                member.setId(rs.getLong("id"));
                member.setName(rs.getString("name"));
                member.setEmail(rs.getString("email"));
                return member;
            });

            team.setMembers(members);

            return Optional.ofNullable(team);
        } catch (Exception e) {
            return Optional.empty();  // Return empty if team not found or any other error
        }
    }


    public boolean findEmployeeExists(Long teamId, Long employeeId) {
        String sql = "SELECT COUNT(*) FROM team_members WHERE team_id = ? AND employee_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{teamId, employeeId}, Integer.class);
        return count != null && count > 0;  // If count is greater than 0, the employee is already a member
    }


    // Find all teams for a given HR (who created the team)
    public List<Team> findByHrId(Long hrId) {
        String sql = "SELECT * FROM team WHERE hr_id = ?";
        return jdbcTemplate.query(sql, new Object[]{hrId}, new BeanPropertyRowMapper<>(Team.class));
    }

    // Find all teams for a given Manager (assigned to the team)
    public List<Team> findByManagerId(Long managerId) {
        String sql = "SELECT * FROM team WHERE manager_id = ?";

        List<Team> teams = jdbcTemplate.query(sql, new Object[]{managerId}, new BeanPropertyRowMapper<>(Team.class));

        // For each team, fetch the team members
        for (Team team : teams) {
            String memberSql = "SELECT e.id, e.name FROM employee e " +
                    "JOIN team_members tm ON e.id = tm.employee_id " +
                    "WHERE tm.team_id = ?";
            List<Employee> members = jdbcTemplate.query(memberSql, new Object[]{team.getId()}, (rs, rowNum) -> {
                Employee employee = new Employee();
                employee.setId(rs.getLong("id"));
                employee.setName(rs.getString("name"));
                return employee;
            });

            team.setMembers(members);  // Set members for each team
        }

        return teams;
    }

    // Remove all team members from the team_members join table
    public void removeTeamMembers(Long teamId) {
        String sql = "DELETE FROM team_members WHERE team_id = ?";
        jdbcTemplate.update(sql, teamId);
    }

    // Delete the team from the team table
    public void delete(Long teamId) {
        String sql = "DELETE FROM team WHERE id = ?";
        jdbcTemplate.update(sql, teamId);
    }

    // New method: Find all teams that an employee belongs to
    public List<Team> findTeamsByEmployeeId(Long employeeId) {
        String sql = "SELECT t.* FROM team t " +
                "JOIN team_members tm ON t.id = tm.team_id " +
                "WHERE tm.employee_id = ?";

        List<Team> teams = jdbcTemplate.query(sql, new Object[]{employeeId}, new BeanPropertyRowMapper<>(Team.class));

        // For each team, fetch the team members
        for (Team team : teams) {
            String memberSql = "SELECT e.id, e.name FROM employee e " +
                    "JOIN team_members tm ON e.id = tm.employee_id " +
                    "WHERE tm.team_id = ?";
            List<Employee> members = jdbcTemplate.query(memberSql, new Object[]{team.getId()}, (rs, rowNum) -> {
                Employee employee = new Employee();
                employee.setId(rs.getLong("id"));
                employee.setName(rs.getString("name"));
                return employee;
            });

            team.setMembers(members);
        }

        return teams;
    }

    // New method: Remove an employee from a specific team
    public void removeEmployeeFromTeam(Long teamId, Long employeeId) {
        String sql = "DELETE FROM team_members WHERE team_id = ? AND employee_id = ?";
        jdbcTemplate.update(sql, teamId, employeeId);
    }

}
