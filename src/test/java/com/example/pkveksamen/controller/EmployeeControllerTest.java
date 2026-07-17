package com.example.pkveksamen.controller;

import com.example.pkveksamen.model.AlphaRole;
import com.example.pkveksamen.model.Employee;
import com.example.pkveksamen.model.EmployeeRole;
import com.example.pkveksamen.service.EmployeeService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @Mock
    private Model model;

    @Mock
    private HttpSession session;

    @InjectMocks
    private EmployeeController employeeController;

    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testEmployee = new Employee();
        testEmployee.setEmployeeId(1);
        testEmployee.setUsername("testuser");
        testEmployee.setPassword("password123");
        testEmployee.setEmail("test@test.dk");
        testEmployee.setRole(EmployeeRole.PROJECT_MANAGER);
        testEmployee.setSkill(AlphaRole.Developer);
    }


    @Test
    void createEmployeePost_Success_ShouldRedirectToLogin() {

        when(employeeService.createEmployee(
                testEmployee.getUsername(),
                testEmployee.getPassword(),
                testEmployee.getEmail(),
                testEmployee.getRole().getDisplayName(),
                testEmployee.getSkill().getDisplayName()
        )).thenReturn(true);

        String viewName = employeeController.createEmployeePost(testEmployee, model);

        assertEquals("redirect:/login", viewName);
        verify(employeeService).createEmployee(
                testEmployee.getUsername(),
                testEmployee.getPassword(),
                testEmployee.getEmail(),
                testEmployee.getRole().getDisplayName(),
                testEmployee.getSkill().getDisplayName()
        );
    }

    @Test
    void createEmployeePost_NoSkillSelected_ShouldReturnErrorMessage() {

        testEmployee.setSkill(null);

        String viewName = employeeController.createEmployeePost(testEmployee, model);

        assertEquals("create-employee", viewName);
        verify(model).addAttribute("error", "Vælg venligst en Alpha Role");
        verify(model).addAttribute("employee", testEmployee);
        verify(model).addAttribute("roles", EmployeeRole.values());
        verify(model).addAttribute("skills", AlphaRole.values());
        verify(employeeService, never()).createEmployee(anyString(), anyString(), anyString(), anyString(), anyString());
    }


//    @Test
//    void validateLogin_Success_ShouldRedirectToProjectList() {
//        when(employeeService.validateLogin("testuser", "password123")).thenReturn(1);
//
//        String viewName = employeeController.validateLogin("testuser", "password123");
//
//        assertEquals("redirect:/project/list/1", viewName);
//        verify(employeeService).validateLogin("testuser", "password123");
//        verify(model, never()).addAttribute(anyString(), anyString());
//    }



//    @Test
//    void validateLogin_IdIsZero_ShouldReturnLoginViewWithError() {
//        when(employeeService.validateLogin("testuser", "wrongpass")).thenReturn(0);
//
//        String viewName = employeeController.validateLogin("testuser", "wrongpass", model);
//
//        assertEquals("login", viewName);
//        verify(model).addAttribute("error", "Brugernavn eller adgangskoden er forkert. Prøv igen!");
//    }

    @Test
    void logout_ShouldInvalidateSessionAndRedirectToLogin() {
        String viewName = employeeController.logout(session);

        assertEquals("redirect:/login", viewName);
        verify(session).invalidate();
    }
}