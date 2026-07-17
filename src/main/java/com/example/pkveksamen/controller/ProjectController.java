package com.example.pkveksamen.controller;

import com.example.pkveksamen.model.*;
import com.example.pkveksamen.service.ProjectService;
import com.example.pkveksamen.service.EmployeeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("project")
public class ProjectController {
    private final ProjectService projectService;
    private final EmployeeService employeeService;

    public ProjectController(ProjectService projectService, EmployeeService employeeService) {
        this.projectService = projectService;
        this.employeeService = employeeService;
    }

    @GetMapping("/employees/{employeeId}/{projectId}")
    public String showProjectMembers(@PathVariable int employeeId,
                                     @PathVariable long projectId,
                                     Model model) {
        Project project = projectService.getProjectById(projectId);
        List<Employee> projectMembers = projectService.getProjectMembers(projectId);
        List<Employee> availableEmployees = projectService.getAvailableEmployeesToAdd(projectId);

        model.addAttribute("project", project);
        model.addAttribute("projectMembers", projectMembers);
        model.addAttribute("availableEmployees", availableEmployees);
        model.addAttribute("currentEmployeeId", employeeId);
        model.addAttribute("currentProjectId", projectId);

        Employee employee = employeeService.getEmployeeById(employeeId);
        if (employee != null) {
            model.addAttribute("username", employee.getUsername());
            model.addAttribute("employeeRole", employee.getRole());
        }

        return "view-project-members";
    }

    @PostMapping("/employees/{employeeId}/{projectId}/add")
    public String addEmployeeToProject(@PathVariable int employeeId,
                                       @PathVariable long projectId,
                                       @RequestParam("selectedEmployeeId") int selectedEmployeeId) {
        projectService.addEmployeeToProject(selectedEmployeeId, projectId);
        return "redirect:/project/employees/" + employeeId + "/" + projectId;
    }

    @PostMapping("/employees/{employeeId}/{projectId}/remove")
    public String removeEmployeeFromProject(@PathVariable int employeeId,
                                            @PathVariable long projectId,
                                            @RequestParam("employeeIdToRemove") int employeeIdToRemove) {
        projectService.removeEmployeeFromProject(employeeIdToRemove, projectId);
        return "redirect:/project/employees/" + employeeId + "/" + projectId;
    }


    @GetMapping("/all-employees")
    public String showAllEmployees(@RequestParam("employeeId") int employeeId, Model model) {
        List<Employee> employeeList = employeeService.getAllEmployees();
        model.addAttribute("employees", employeeList);
        model.addAttribute("currentEmployeeId", employeeId);

        Employee employee = employeeService.getEmployeeById(employeeId);
        if (employee != null) {
            model.addAttribute("username", employee.getUsername());
            model.addAttribute("employeeRole", employee.getRole());
        }

        return "view-all-employees";
    }

    @GetMapping("/list/{employeeId}")
    public String showProjectsByEmployeeId(@PathVariable int employeeId, HttpSession session, Model model) {

        Integer loggedInEmployeeId = (Integer) session.getAttribute("employeeId");

        if (loggedInEmployeeId == null) {
            return "redirect:/login";
        }

        if (!loggedInEmployeeId.equals(employeeId)) {
            return "redirect:/project/list/" + loggedInEmployeeId;
        }

        List<Project> projectList = projectService.showProjectsByEmployeeId(loggedInEmployeeId);

        model.addAttribute("projectList", projectList);
        model.addAttribute("currentEmployeeId", loggedInEmployeeId);

        Employee employee = employeeService.getEmployeeById(loggedInEmployeeId);

        if (employee != null) {
            model.addAttribute("username", employee.getUsername());

            model.addAttribute("employeeRole", employee.getRole());
        }

        return "project";
    }

    @GetMapping("/subproject/list/{projectID}")
    public String showSubprojectByProjectId(@RequestParam("employeeId") int employeeId,
                                            @PathVariable long projectID,
                                            Model model) {
        List<SubProject> subProjectList = projectService.showSubProjectsByProjectId(projectID);
        model.addAttribute("subProjectList", subProjectList);
        model.addAttribute("currentProjectId", projectID);
        model.addAttribute("currentEmployeeId", employeeId);

        // Add employee details for the header
        Employee employee = employeeService.getEmployeeById(employeeId);
        if (employee != null) {
            model.addAttribute("username", employee.getUsername());
            model.addAttribute("employeeRole", employee.getRole());
        }

        return "subproject";
    }


    @GetMapping("/createproject/{employeeId}")
    public String showCreateProjectForm(@PathVariable int employeeId, Model model) {
        model.addAttribute("project", new Project());
        model.addAttribute("currentEmployeeId", employeeId);
//        List<Employee> teamMembers = projectService.getAllTeamMembers();
//        model.addAttribute("teamMembers", teamMembers);
        return "createproject";
    }

    @GetMapping("/createsubproject/{employeeId}/{projectId}")
    public String showCreateSubProjectForm(@PathVariable int employeeId,
                                           @PathVariable long projectId,
                                           @ModelAttribute SubProject subProject,
                                           Model model) {

        // Simpel range-check
        if (subProject.getSubProjectStartDate() != null) {
            int year = subProject.getSubProjectStartDate().getYear();
            if (year < 2000 || year > 2100) {
                // her kunne du fx sætte en fejlbesked i model og vise formen igen
                model.addAttribute("error", "Start date year must be between 2000 and 2100");
                // husk at lægge de samme model-attributter på som i GET-metoden
                return "createsubproject";
            }
        }

        if (subProject.getSubProjectDeadline() != null) {
            int year = subProject.getSubProjectDeadline().getYear();
            if (year < 2000 || year > 2100) {
                model.addAttribute("error", "Deadline year must be between 2000 and 2100");
                return "createsubproject";
            }
        }

        model.addAttribute("subProject", new SubProject());
        model.addAttribute("currentEmployeeId", employeeId);
        model.addAttribute("currentProjectId", projectId);
        return "createsubproject";
    }

    // TODO: Vurder om denne kan slettes (kan nok godt)
    @PostMapping("/create/{employeeId}")
    public String createProject(@PathVariable int employeeId,
                                @ModelAttribute Project project,
                                Model model) {
        project.recalculateDuration();

        // Simpel range-check
        if (project.getProjectStartDate() != null) {
            int year = project.getProjectStartDate().getYear();
            if (year < 2000 || year > 2100) {
                // her kunne du fx sætte en fejlbesked i model og vise formen igen
                model.addAttribute("error", "Start date year must be between 2000 and 2100");
                // husk at lægge de samme model-attributter på som i GET-metoden
                return "createproject";
            }
        }

        if (project.getProjectDeadline() != null) {
            int year = project.getProjectDeadline().getYear();
            if (year < 2000 || year > 2100) {
                model.addAttribute("error", "Deadline year must be between 2000 and 2100");
                return "createproject";
            }
        }

        projectService.createProject(
                project.getProjectName(),
                project.getProjectDescription(),
                project.getProjectStartDate(),
                project.getProjectDeadline(),
                project.getProjectCustomer(),
                employeeId
        );

        return "redirect:/project/list/" + employeeId;
    }

    @PostMapping("/saveproject/{employeeId}")
    public String saveProject(@PathVariable int employeeId,
                              @ModelAttribute Project project) {
        project.recalculateDuration();
        projectService.saveProject(project, employeeId);
//        projectService.assignEmployeeToProject(selectedEmployeeId, project.getProjectID());
        return "redirect:/project/list/" + employeeId;
    }


    @PostMapping("/savesubproject/{employeeId}/{projectId}")
    public String saveSubProject(@PathVariable int employeeId,
                                 @PathVariable long projectId,
                                 @ModelAttribute SubProject subProject,
                                 Model model) {
        subProject.recalculateDuration();

        Project project = projectService.getProjectById(projectId);
        if (project.getProjectStartDate() != null && subProject.getSubProjectStartDate() != null &&
                subProject.getSubProjectStartDate().isBefore(project.getProjectStartDate())) {
            subProject.recalculateDuration();
            model.addAttribute("error", "Subproject start date must be within project period");
            model.addAttribute("subProject", subProject);
            model.addAttribute("currentEmployeeId", employeeId);
            model.addAttribute("currentProjectId", projectId);
            return "createsubproject";
        }
        if (project.getProjectDeadline() != null && subProject.getSubProjectDeadline() != null &&
                subProject.getSubProjectDeadline().isAfter(project.getProjectDeadline())) {
            subProject.recalculateDuration();
            model.addAttribute("error", "Subproject deadline must be within project period");
            model.addAttribute("subProject", subProject);
            model.addAttribute("currentEmployeeId", employeeId);
            model.addAttribute("currentProjectId", projectId);
            return "createsubproject";
        }
        if (subProject.getSubProjectStartDate() != null && subProject.getSubProjectDeadline() != null &&
                subProject.getSubProjectDeadline().isBefore(subProject.getSubProjectStartDate())) {
            model.addAttribute("error", "Subproject deadline cannot be before start date");
            model.addAttribute("subProject", subProject);
            model.addAttribute("currentEmployeeId", employeeId);
            model.addAttribute("currentProjectId", projectId);
            return "createsubproject";
        }

        projectService.saveSubProject(subProject, projectId);
        return "redirect:/project/subproject/list/" + projectId + "?employeeId=" + employeeId;
    }

    @PostMapping("/delete/{employeeId}/{id}")
    public String deleteProject(@PathVariable int employeeId, @PathVariable long id) {
        projectService.deleteProject(id);
        return "redirect:/project/list/" + employeeId;
    }

    // TODO DELETE TIL SUBPROJECT - kig på den Aden har lavet den
    // TODO: KIG OGSÅ PÅ LINJE 123 EFTER PROJECTID
    @PostMapping("/subproject/delete/{employeeId}/{projectId}/{subProjectId}")
    public String deleteSubProject(@PathVariable int employeeId,
                                   @PathVariable long projectId,
                                   @PathVariable long subProjectId) {
        projectService.deleteSubProject(subProjectId);
        return "redirect:/project/subproject/list/" + projectId + "?employeeId=" + employeeId;
    }


    @GetMapping("/edit/{employeeId}/{projectId}")
    public String showEditForm(@PathVariable int employeeId,
                               @PathVariable long projectId,
                               Model model) {
        Project project = projectService.getProjectById(projectId);
        model.addAttribute("project", project);
        model.addAttribute("currentEmployeeId", employeeId);

        Employee employee = employeeService.getEmployeeById(employeeId);
        if (employee != null) {
            model.addAttribute("username", employee.getUsername());
            model.addAttribute("employeeRole", employee.getRole());
        }
        return "edit-project";
    }

    @GetMapping("/subproject/edit/{employeeId}/{projectId}/{subProjectId}")
    public String showSubProjectEditForm(@PathVariable int employeeId,
                                         @PathVariable long projectId,
                                         @PathVariable long subProjectId, // lowercase i URL
                                         Model model) {
        SubProject subProject = projectService.getSubProjectBySubProjectID(subProjectId);
        model.addAttribute("subProject", subProject);
        model.addAttribute("currentEmployeeId", employeeId);
        model.addAttribute("currentProjectId", projectId);

        Employee employee = employeeService.getEmployeeById(employeeId);
        if (employee != null) {
            model.addAttribute("username", employee.getUsername());
            model.addAttribute("employeeRole", employee.getRole());
        }

        return "edit-subproject";
    }

    @PostMapping("/edit/{employeeId}/{projectId}")
    public String editProject(@PathVariable int employeeId,
                              @PathVariable long projectId,
                              @ModelAttribute Project project) {
        project.setProjectID(projectId);
        project.recalculateDuration();
        projectService.editProject(project);
        return "redirect:/project/list/" + employeeId;
    }

    @PostMapping("/subproject/edit/{employeeId}/{projectId}/{subProjectId}")
    public String editSubProject(@PathVariable int employeeId,
                                 @PathVariable long projectId,
                                 @PathVariable long subProjectId, // lowercase i URL
                                 @ModelAttribute SubProject subProject,
                                 Model model) {
        subProject.setSubProjectID(subProjectId); // Bruger setter-metoden fra model klassen
        subProject.recalculateDuration();

        Project project = projectService.getProjectById(projectId);
        if (project.getProjectStartDate() != null && subProject.getSubProjectStartDate() != null &&
                subProject.getSubProjectStartDate().isBefore(project.getProjectStartDate())) {
            model.addAttribute("error", "Subproject start date must be within project period");
            model.addAttribute("subProject", subProject);
            model.addAttribute("currentEmployeeId", employeeId);
            model.addAttribute("currentProjectId", projectId);
            Employee employee = employeeService.getEmployeeById(employeeId);
            if (employee != null) {
                model.addAttribute("username", employee.getUsername());
                model.addAttribute("employeeRole", employee.getRole());
            }
            return "edit-subproject";
        }
        if (project.getProjectDeadline() != null && subProject.getSubProjectDeadline() != null &&
                subProject.getSubProjectDeadline().isAfter(project.getProjectDeadline())) {
            model.addAttribute("error", "Subproject deadline must be within project period");
            model.addAttribute("subProject", subProject);
            model.addAttribute("currentEmployeeId", employeeId);
            model.addAttribute("currentProjectId", projectId);
            Employee employee = employeeService.getEmployeeById(employeeId);
            if (employee != null) {
                model.addAttribute("username", employee.getUsername());
                model.addAttribute("employeeRole", employee.getRole());
            }
            return "edit-subproject";
        }
        if (subProject.getSubProjectStartDate() != null && subProject.getSubProjectDeadline() != null &&
                subProject.getSubProjectDeadline().isBefore(subProject.getSubProjectStartDate())) {
            model.addAttribute("error", "Subproject deadline cannot be before start date");
            model.addAttribute("subProject", subProject);
            model.addAttribute("currentEmployeeId", employeeId);
            model.addAttribute("currentProjectId", projectId);
            Employee employee = employeeService.getEmployeeById(employeeId);
            if (employee != null) {
                model.addAttribute("username", employee.getUsername());
                model.addAttribute("employeeRole", employee.getRole());
            }
            return "edit-subproject";
        }

        projectService.editSubProject(subProject);
        return "redirect:/project/subproject/list/" + projectId + "?employeeId=" + employeeId;
    }
}