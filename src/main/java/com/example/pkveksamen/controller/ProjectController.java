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

    private Integer getLoggedInEmployeeId(HttpSession session) {
        return (Integer) session.getAttribute("employeeId");
    }

    private boolean hasAccessToProject(int employeeId, long projectId) {
        return projectService.showProjectsByEmployeeId(employeeId)
                .stream()
                .anyMatch(project -> project.getProjectID() == projectId);
    }

    private boolean isProjectManager(Employee employee) {
        return employee != null
                && employee.getRole() == EmployeeRole.PROJECT_MANAGER;
    }

    private boolean subProjectBelongsToProject(long projectId, long subProjectId) {

        return projectService.showSubProjectsByProjectId(projectId)
                .stream()
                .anyMatch(subProject -> subProject.getSubProjectID() == subProjectId);
    }

    @GetMapping("/employees/{employeeId}/{projectId}")
    public String showProjectMembers(@PathVariable int employeeId,
                                     @PathVariable long projectId,
                                     HttpSession session,
                                     Model model) {

        Integer loggedInEmployeeId = getLoggedInEmployeeId(session);

        if (loggedInEmployeeId == null) {
            return "redirect:/login";
        }

        if (!loggedInEmployeeId.equals(employeeId)) {
            return "redirect:/project/list/" + loggedInEmployeeId;
        }

        Employee currentEmployee = employeeService.getEmployeeById(loggedInEmployeeId);

        if (currentEmployee == null ||
                currentEmployee.getRole() != EmployeeRole.PROJECT_MANAGER) {

            return "redirect:/project/list/" + loggedInEmployeeId;
        }

        Project project = projectService.getProjectById(projectId);

        List<Employee> projectMembers = projectService.getProjectMembers(projectId);

        List<Employee> availableEmployees = projectService.getAvailableEmployeesToAdd(projectId);

        model.addAttribute("project", project);
        model.addAttribute("projectMembers", projectMembers);
        model.addAttribute("availableEmployees", availableEmployees);
        model.addAttribute("currentEmployeeId", loggedInEmployeeId);
        model.addAttribute("currentProjectId", projectId);
        model.addAttribute("username", currentEmployee.getUsername());
        model.addAttribute("employeeRole", currentEmployee.getRole());

        return "view-project-members";
    }

    @PostMapping("/employees/{employeeId}/{projectId}/add")
    public String addEmployeeToProject(
            @PathVariable int employeeId,
            @PathVariable long projectId,
            @RequestParam("selectedEmployeeId") int selectedEmployeeId,
            HttpSession session) {

        Integer loggedInEmployeeId = getLoggedInEmployeeId(session);

        if (loggedInEmployeeId == null) {
            return "redirect:/login";
        }

        Employee currentEmployee = employeeService.getEmployeeById(loggedInEmployeeId);

        if (!loggedInEmployeeId.equals(employeeId) ||
                currentEmployee == null ||
                currentEmployee.getRole() != EmployeeRole.PROJECT_MANAGER) {

            return "redirect:/project/list/" + loggedInEmployeeId;
        }

        projectService.addEmployeeToProject(selectedEmployeeId, projectId);

        return "redirect:/project/employees/"
                + loggedInEmployeeId
                + "/"
                + projectId;
    }

    @PostMapping("/employees/{employeeId}/{projectId}/remove")
    public String removeEmployeeFromProject(
            @PathVariable int employeeId,
            @PathVariable long projectId,
            @RequestParam("employeeIdToRemove") int employeeIdToRemove,
            HttpSession session) {

        Integer loggedInEmployeeId = getLoggedInEmployeeId(session);

        if (loggedInEmployeeId == null) {
            return "redirect:/login";
        }

        Employee currentEmployee = employeeService.getEmployeeById(loggedInEmployeeId);

        if (!loggedInEmployeeId.equals(employeeId) ||
                currentEmployee == null ||
                currentEmployee.getRole() != EmployeeRole.PROJECT_MANAGER) {

            return "redirect:/project/list/" + loggedInEmployeeId;
        }

        projectService.removeEmployeeFromProject(employeeIdToRemove, projectId);

        return "redirect:/project/employees/"
                + loggedInEmployeeId
                + "/"
                + projectId;
    }


    @GetMapping("/all-employees")
    public String showAllEmployees(@RequestParam("employeeId") int employeeId, HttpSession session, Model model) {

        Integer loggedInEmployeeId = getLoggedInEmployeeId(session);

        if (loggedInEmployeeId == null) {
            return "redirect:/login";
        }

        Employee currentEmployee = employeeService.getEmployeeById(loggedInEmployeeId);

        if (!loggedInEmployeeId.equals(employeeId) ||
                currentEmployee == null ||
                currentEmployee.getRole() != EmployeeRole.PROJECT_MANAGER) {

            return "redirect:/project/list/" + loggedInEmployeeId;
        }

        List<Employee> employeeList = employeeService.getAllEmployees();

        model.addAttribute("employees", employeeList);
        model.addAttribute("currentEmployeeId", loggedInEmployeeId);
        model.addAttribute("username", currentEmployee.getUsername());
        model.addAttribute("employeeRole", currentEmployee.getRole());

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
    public String showSubprojectByProjectId(
            @RequestParam("employeeId") int employeeId,
            @PathVariable long projectID,
            HttpSession session,
            Model model) {

        Integer loggedInEmployeeId = getLoggedInEmployeeId(session);

        if (loggedInEmployeeId == null) {
            return "redirect:/login";
        }

        if (!loggedInEmployeeId.equals(employeeId)) {
            return "redirect:/project/list/" + loggedInEmployeeId;
        }

        if (!hasAccessToProject(loggedInEmployeeId, projectID)) {
            return "redirect:/project/list/" + loggedInEmployeeId;
        }

        Employee currentEmployee = employeeService.getEmployeeById(loggedInEmployeeId);

        if (currentEmployee == null) {
            session.invalidate();
            return "redirect:/login";
        }

        List<SubProject> subProjectList = projectService.showSubProjectsByProjectId(projectID);

        model.addAttribute("subProjectList", subProjectList);
        model.addAttribute("currentProjectId", projectID);
        model.addAttribute("currentEmployeeId", loggedInEmployeeId);
        model.addAttribute("username", currentEmployee.getUsername());
        model.addAttribute("employeeRole", currentEmployee.getRole());

        return "subproject";
    }


    @GetMapping("/createproject/{employeeId}")
    public String showCreateProjectForm(
            @PathVariable int employeeId,
            HttpSession session,
            Model model) {

        Integer loggedInEmployeeId = getLoggedInEmployeeId(session);

        if (loggedInEmployeeId == null) {
            return "redirect:/login";
        }

        Employee currentEmployee = employeeService.getEmployeeById(loggedInEmployeeId);

        if (!loggedInEmployeeId.equals(employeeId) ||
                currentEmployee == null ||
                currentEmployee.getRole() != EmployeeRole.PROJECT_MANAGER) {

            return "redirect:/project/list/" + loggedInEmployeeId;
        }

        model.addAttribute("project", new Project());
        model.addAttribute("currentEmployeeId", loggedInEmployeeId);

        return "createproject";
    }

    @GetMapping("/createsubproject/{employeeId}/{projectId}")
    public String showCreateSubProjectForm(
            @PathVariable int employeeId,
            @PathVariable long projectId,
            HttpSession session,
            Model model) {

        Integer loggedInEmployeeId = getLoggedInEmployeeId(session);

        if (loggedInEmployeeId == null) {
            return "redirect:/login";
        }

        Employee currentEmployee = employeeService.getEmployeeById(loggedInEmployeeId);

        if (!loggedInEmployeeId.equals(employeeId) ||
                currentEmployee == null ||
                currentEmployee.getRole() != EmployeeRole.PROJECT_MANAGER ||
                !hasAccessToProject(loggedInEmployeeId, projectId)) {

            return "redirect:/project/list/" + loggedInEmployeeId;
        }

        model.addAttribute("subProject", new SubProject());
        model.addAttribute("currentEmployeeId", loggedInEmployeeId);
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
    public String saveProject(
            @PathVariable int employeeId,
            @ModelAttribute Project project,
            HttpSession session) {

        Integer loggedInEmployeeId = getLoggedInEmployeeId(session);

        if (loggedInEmployeeId == null) {
            return "redirect:/login";
        }

        Employee currentEmployee =
                employeeService.getEmployeeById(loggedInEmployeeId);

        if (!loggedInEmployeeId.equals(employeeId) ||
                currentEmployee == null ||
                currentEmployee.getRole() != EmployeeRole.PROJECT_MANAGER) {

            return "redirect:/project/list/" + loggedInEmployeeId;
        }

        project.recalculateDuration();

        projectService.saveProject(
                project,
                loggedInEmployeeId
        );

        return "redirect:/project/list/" + loggedInEmployeeId;
    }


    @PostMapping("/savesubproject/{employeeId}/{projectId}")
    public String saveSubProject(
            @PathVariable int employeeId,
            @PathVariable long projectId,
            @ModelAttribute SubProject subProject,
            HttpSession session,
            Model model) {

        Integer loggedInEmployeeId = getLoggedInEmployeeId(session);

        if (loggedInEmployeeId == null) {
            return "redirect:/login";
        }

        Employee currentEmployee = employeeService.getEmployeeById(loggedInEmployeeId);

        if (!loggedInEmployeeId.equals(employeeId)
                || currentEmployee == null
                || currentEmployee.getRole() != EmployeeRole.PROJECT_MANAGER
                || !hasAccessToProject(loggedInEmployeeId, projectId)) {

            return "redirect:/project/list/" + loggedInEmployeeId;
        }

        Project project = projectService.getProjectById(projectId);

        if (project == null) {
            return "redirect:/project/list/" + loggedInEmployeeId;
        }

        subProject.recalculateDuration();

        if (project.getProjectStartDate() != null
                && subProject.getSubProjectStartDate() != null
                && subProject.getSubProjectStartDate()
                .isBefore(project.getProjectStartDate())) {

            model.addAttribute("error", "Subproject start date must be within project period");

            model.addAttribute("subProject", subProject);
            model.addAttribute("currentEmployeeId", loggedInEmployeeId);
            model.addAttribute("currentProjectId", projectId);

            return "createsubproject";
        }

        if (project.getProjectDeadline() != null
                && subProject.getSubProjectDeadline() != null
                && subProject.getSubProjectDeadline()
                .isAfter(project.getProjectDeadline())) {

            model.addAttribute("error", "Subproject deadline must be within project period");
            model.addAttribute("subProject", subProject);
            model.addAttribute("currentEmployeeId", loggedInEmployeeId);
            model.addAttribute("currentProjectId", projectId);

            return "createsubproject";
        }

        if (subProject.getSubProjectStartDate() != null
                && subProject.getSubProjectDeadline() != null
                && subProject.getSubProjectDeadline()
                .isBefore(subProject.getSubProjectStartDate())) {

            model.addAttribute("error", "Subproject deadline cannot be before start date");

            model.addAttribute("subProject", subProject);
            model.addAttribute("currentEmployeeId", loggedInEmployeeId);
            model.addAttribute("currentProjectId", projectId);

            return "createsubproject";
        }

        projectService.saveSubProject(subProject, projectId);

        return "redirect:/project/subproject/list/"
                + projectId
                + "?employeeId="
                + loggedInEmployeeId;
    }

    @PostMapping("/delete/{employeeId}/{id}")
    public String deleteProject(@PathVariable int employeeId, @PathVariable long id, HttpSession session) {

        Integer loggedInEmployeeId = getLoggedInEmployeeId(session);

        if (loggedInEmployeeId == null) {
            return "redirect:/login";
        }

        Employee currentEmployee = employeeService.getEmployeeById(loggedInEmployeeId);

        if (!loggedInEmployeeId.equals(employeeId)
                || !isProjectManager(currentEmployee)
                || !hasAccessToProject(loggedInEmployeeId, id)) {

            return "redirect:/project/list/" + loggedInEmployeeId;
        }

        projectService.deleteProject(id);

        return "redirect:/project/list/" + loggedInEmployeeId;
    }

    @PostMapping(
            "/subproject/delete/{employeeId}/{projectId}/{subProjectId}"
    )
    public String deleteSubProject(
            @PathVariable int employeeId,
            @PathVariable long projectId,
            @PathVariable long subProjectId,
            HttpSession session) {

        Integer loggedInEmployeeId = getLoggedInEmployeeId(session);

        if (loggedInEmployeeId == null) {
            return "redirect:/login";
        }

        Employee currentEmployee = employeeService.getEmployeeById(loggedInEmployeeId);

        if (!loggedInEmployeeId.equals(employeeId)
                || !isProjectManager(currentEmployee)
                || !hasAccessToProject(loggedInEmployeeId, projectId)
                || !subProjectBelongsToProject(projectId, subProjectId) ){

            return "redirect:/project/list/" + loggedInEmployeeId;
        }

        projectService.deleteSubProject(subProjectId);

        return "redirect:/project/subproject/list/"
                + projectId
                + "?employeeId="
                + loggedInEmployeeId;
    }


    @GetMapping("/edit/{employeeId}/{projectId}")
    public String showEditForm(
            @PathVariable int employeeId,
            @PathVariable long projectId,
            HttpSession session,
            Model model) {

        Integer loggedInEmployeeId = getLoggedInEmployeeId(session);

        if (loggedInEmployeeId == null) {
            return "redirect:/login";
        }

        Employee currentEmployee = employeeService.getEmployeeById(loggedInEmployeeId);

        if (!loggedInEmployeeId.equals(employeeId)
                || !isProjectManager(currentEmployee)
                || !hasAccessToProject(
                loggedInEmployeeId,
                projectId
        )) {

            return "redirect:/project/list/" + loggedInEmployeeId;
        }

        Project project = projectService.getProjectById(projectId);

        if (project == null) {
            return "redirect:/project/list/" + loggedInEmployeeId;
        }

        model.addAttribute("project", project);
        model.addAttribute("currentEmployeeId", loggedInEmployeeId);
        model.addAttribute("username", currentEmployee.getUsername());
        model.addAttribute("employeeRole", currentEmployee.getRole());

        return "edit-project";
    }

    @GetMapping(
            "/subproject/edit/{employeeId}/{projectId}/{subProjectId}"
    )
    public String showSubProjectEditForm(
            @PathVariable int employeeId,
            @PathVariable long projectId,
            @PathVariable long subProjectId,
            HttpSession session,
            Model model) {

        Integer loggedInEmployeeId = getLoggedInEmployeeId(session);

        if (loggedInEmployeeId == null) {
            return "redirect:/login";
        }

        Employee currentEmployee = employeeService.getEmployeeById(loggedInEmployeeId);

        if (!loggedInEmployeeId.equals(employeeId)
                || !isProjectManager(currentEmployee)
                || !hasAccessToProject(loggedInEmployeeId, projectId)
                || !subProjectBelongsToProject(projectId, subProjectId)) {

            return "redirect:/project/list/" + loggedInEmployeeId;
        }

        SubProject subProject = projectService.getSubProjectBySubProjectID(subProjectId);

        if (subProject == null) {
            return "redirect:/project/subproject/list/"
                    + projectId
                    + "?employeeId="
                    + loggedInEmployeeId;
        }

        model.addAttribute("subProject", subProject);
        model.addAttribute("currentEmployeeId", loggedInEmployeeId);
        model.addAttribute("currentProjectId", projectId);
        model.addAttribute("username", currentEmployee.getUsername());
        model.addAttribute("employeeRole", currentEmployee.getRole());

        return "edit-subproject";
    }

    @PostMapping("/edit/{employeeId}/{projectId}")
    public String editProject(
            @PathVariable int employeeId,
            @PathVariable long projectId,
            @ModelAttribute Project project,
            HttpSession session) {

        Integer loggedInEmployeeId = getLoggedInEmployeeId(session);

        if (loggedInEmployeeId == null) {
            return "redirect:/login";
        }

        Employee currentEmployee = employeeService.getEmployeeById(loggedInEmployeeId);

        if (!loggedInEmployeeId.equals(employeeId)
                || !isProjectManager(currentEmployee)
                || !hasAccessToProject(
                loggedInEmployeeId,
                projectId
        )) {

            return "redirect:/project/list/" + loggedInEmployeeId;
        }

        project.setProjectID(projectId);
        project.recalculateDuration();

        projectService.editProject(project);

        return "redirect:/project/list/" + loggedInEmployeeId;
    }

    @PostMapping("/subproject/edit/{employeeId}/{projectId}/{subProjectId}")
    public String editSubProject(
            @PathVariable int employeeId,
            @PathVariable long projectId,
            @PathVariable long subProjectId,
            @ModelAttribute SubProject subProject,
            HttpSession session,
            Model model) {

        Integer loggedInEmployeeId = getLoggedInEmployeeId(session);

        if (loggedInEmployeeId == null) {
            return "redirect:/login";
        }

        Employee currentEmployee = employeeService.getEmployeeById(loggedInEmployeeId);

        if (!loggedInEmployeeId.equals(employeeId)
                || !isProjectManager(currentEmployee)
                || !hasAccessToProject(loggedInEmployeeId, projectId)
                || !subProjectBelongsToProject(projectId, subProjectId)) {

            return "redirect:/project/list/" + loggedInEmployeeId;
        }

        Project project = projectService.getProjectById(projectId);

        if (project == null) {
            return "redirect:/project/list/" + loggedInEmployeeId;
        }

        subProject.setSubProjectID(subProjectId);
        subProject.recalculateDuration();

        if (project.getProjectStartDate() != null
                && subProject.getSubProjectStartDate() != null
                && subProject.getSubProjectStartDate()
                .isBefore(project.getProjectStartDate())) {

            model.addAttribute("error", "Subproject start date must be within project period");

            model.addAttribute("subProject", subProject);
            model.addAttribute("currentEmployeeId", loggedInEmployeeId);
            model.addAttribute("currentProjectId", projectId);
            model.addAttribute("username", currentEmployee.getUsername());
            model.addAttribute("employeeRole", currentEmployee.getRole());

            return "edit-subproject";
        }

        if (project.getProjectDeadline() != null
                && subProject.getSubProjectDeadline() != null
                && subProject.getSubProjectDeadline()
                .isAfter(project.getProjectDeadline())) {

            model.addAttribute("error", "Subproject deadline must be within project period");

            model.addAttribute("subProject", subProject);
            model.addAttribute("currentEmployeeId", loggedInEmployeeId);
            model.addAttribute("currentProjectId", projectId);
            model.addAttribute("username", currentEmployee.getUsername());
            model.addAttribute("employeeRole", currentEmployee.getRole());

            return "edit-subproject";
        }

        if (subProject.getSubProjectStartDate() != null
                && subProject.getSubProjectDeadline() != null
                && subProject.getSubProjectDeadline()
                .isBefore(subProject.getSubProjectStartDate())) {

            model.addAttribute("error", "Subproject deadline cannot be before start date");

            model.addAttribute("subProject", subProject);
            model.addAttribute("currentEmployeeId", loggedInEmployeeId);
            model.addAttribute("currentProjectId", projectId);
            model.addAttribute("username", currentEmployee.getUsername());
            model.addAttribute("employeeRole", currentEmployee.getRole());

            return "edit-subproject";
        }

        projectService.editSubProject(subProject);

        return "redirect:/project/subproject/list/"
                + projectId
                + "?employeeId="
                + loggedInEmployeeId;
    }
}