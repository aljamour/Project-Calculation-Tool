package com.example.pkveksamen.controller;

import com.example.pkveksamen.model.AlphaRole;
import com.example.pkveksamen.model.Employee;
import com.example.pkveksamen.model.EmployeeRole;
import com.example.pkveksamen.service.EmployeeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/")
    public String homepage() {
        return "homepage";
    }

    @GetMapping("login")
    public String getLogin() {
        return "login";
    }

    @GetMapping("/create-employee")
    public String createEmployee(Model model) {
        model.addAttribute("employee", new Employee());
        model.addAttribute("roles", EmployeeRole.values());
        model.addAttribute("skills", AlphaRole.values());
        return "create-employee";
    }

    @PostMapping("/create-employee")
    public String createEmployeePost(Employee employee, Model model) {
        if (employee.getSkill() == null) {
            model.addAttribute("error", "Vælg venligst en Alpha Role");
            model.addAttribute("employee", employee);
            model.addAttribute("roles", EmployeeRole.values());
            model.addAttribute("skills", AlphaRole.values());
            return "create-employee";
        }
        
        boolean success = employeeService.createEmployee(
                employee.getUsername(),
                employee.getPassword(),
                employee.getEmail(),
                employee.getRole().getDisplayName(),
                employee.getSkill().getDisplayName()
        );

        if (!success) {
            model.addAttribute("error", "Denne email er allerede i brug");
            model.addAttribute("employee", employee);
            model.addAttribute("roles", EmployeeRole.values());
            model.addAttribute("skills", AlphaRole.values());
            return "create-employee";
        }

        return "redirect:/login";
    }

    @PostMapping("/validate-login")
    public String validateLogin(@RequestParam("username") String username,
                                @RequestParam("password") String password,
                                HttpSession session,
                                Model model) {

        Integer employeeId = employeeService.validateLogin(username, password);

        if (employeeId != null && employeeId > 0) {
            session.setAttribute("employeeId", employeeId);

            return "redirect:/project/list/" + employeeId;
        }

        model.addAttribute("error", "Brugernavn eller adgangskoden er forkert. Prøv igen!");

        return "login";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
