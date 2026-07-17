package com.example.pkveksamen.repository;

import com.example.pkveksamen.model.AlphaRole;
import com.example.pkveksamen.model.Employee;
import com.example.pkveksamen.model.EmployeeRole;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class EmployeeRepository {

    private final JdbcTemplate jdbcTemplate;

    public EmployeeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createEmployee(String username, String password, String email, String role, String alphaRoleDisplayName) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String insertEmployeeSql = "INSERT INTO employee(username, password, email, role) VALUES (?, ?, ?, ?)";
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertEmployeeSql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, email);
            ps.setString(4, role);
            return ps;
        }, keyHolder);
        
        Integer employeeId = keyHolder.getKey().intValue();
        
        String getRoleIdSql = "SELECT role_id FROM role WHERE role_name = ?";
        Integer roleId;
        try {
            roleId = jdbcTemplate.queryForObject(getRoleIdSql, Integer.class, alphaRoleDisplayName);
        } catch (EmptyResultDataAccessException e) {
            KeyHolder roleKeyHolder = new GeneratedKeyHolder();
            String insertRoleSql = "INSERT INTO role(role_name, role_description) VALUES (?, ?)";
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(insertRoleSql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, alphaRoleDisplayName);
                ps.setString(2, alphaRoleDisplayName);
                return ps;
            }, roleKeyHolder);
            roleId = roleKeyHolder.getKey().intValue();
        }
        
        String insertEmployeeRoleSql = "INSERT INTO employee_role(employee_id, role_id) VALUES (?, ?)";
        jdbcTemplate.update(insertEmployeeRoleSql, employeeId, roleId);
    }

    public Employee findEmployeeById(int employeeId) {
        String sql = "SELECT employee_id, username, password, email, role FROM employee WHERE employee_id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Employee employee = new Employee();
                int id = rs.getInt("employee_id");
                employee.setEmployeeId(id);
                employee.setUsername(rs.getString("username"));
                employee.setPassword(rs.getString("password"));
                employee.setEmail(rs.getString("email"));
                employee.setRole(EmployeeRole.fromDisplayName(rs.getString("role")));
                employee.setAlphaRoles(findAlphaRolesByEmployeeId(id));
                return employee;
            }, employeeId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public Employee findEmployeeByUsername(String username) {
        String sql = """
            SELECT employee_id, username, password, email, role
            FROM employee
            WHERE username = ?
            """;

        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Employee employee = new Employee();
                employee.setEmployeeId(rs.getInt("employee_id"));
                employee.setUsername(rs.getString("username"));
                employee.setPassword(rs.getString("password"));
                employee.setEmail(rs.getString("email"));
                employee.setRole(
                        EmployeeRole.fromDisplayName(rs.getString("role"))
                );
                return employee;
            }, username);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public List<AlphaRole> findAlphaRolesByEmployeeId(int employeeId) {
        String sql = "SELECT r.role_name " +
                "FROM role r " +
                "JOIN employee_role er ON r.role_id = er.role_id " +
                "WHERE er.employee_id = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                        AlphaRole.fromDisplayName(rs.getString("role_name")),
                employeeId
        );
    }

    public List<Employee> getAllTeamMembers() {
        String sql = "SELECT employee_id, username, password, email, role " +
                "FROM employee WHERE role = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Employee employee = new Employee();
            int employeeId = rs.getInt("employee_id");
            employee.setEmployeeId(employeeId);
            employee.setUsername(rs.getString("username"));
            employee.setPassword(rs.getString("password"));
            employee.setEmail(rs.getString("email"));
            employee.setRole(EmployeeRole.fromDisplayName(rs.getString("role")));
            employee.setAlphaRoles(findAlphaRolesByEmployeeId(employeeId));
            return employee;
        }, EmployeeRole.TEAM_MEMBER.getDisplayName());
    }

    public List<Employee> getAllEmployees() {
        String sql = "SELECT employee_id, username, email, role FROM employee";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Employee employee = new Employee();
            int employeeId = rs.getInt("employee_id");

            employee.setEmployeeId(employeeId);
            employee.setUsername(rs.getString("username"));
            employee.setEmail(rs.getString("email"));
            employee.setRole(EmployeeRole.fromDisplayName(rs.getString("role")));
            employee.setAlphaRoles(findAlphaRolesByEmployeeId(employeeId));

            return employee;
        });
    }

}
