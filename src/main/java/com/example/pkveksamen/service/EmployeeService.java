package com.example.pkveksamen.service;

import com.example.pkveksamen.model.Employee;
import com.example.pkveksamen.repository.EmployeeRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public boolean createEmployee(String username, String password, String email, String role, String alphaRoleDisplayName) {
        try {
            String hashedPassword = passwordEncoder.encode(password);

            employeeRepository.createEmployee(username, hashedPassword, email, role, alphaRoleDisplayName);

            return true;

        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }

    public Integer validateLogin(String username, String password) {
        Employee employee = employeeRepository.findEmployeeByUsername(username);

        if (employee == null) {
            return null;
        }

        boolean passwordMatches = passwordEncoder.matches(password, employee.getPassword());

        return passwordMatches ? employee.getEmployeeId() : null;
    }

    public Employee getEmployeeById(int employeeId) {
        return employeeRepository.findEmployeeById(employeeId);
    }

    public List<Employee> getAllTeamMembers() {
        return employeeRepository.getAllTeamMembers();
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.getAllEmployees();
    }
}
