package com.example.tailorapp.controller;

import com.example.tailorapp.model.Employee;
import com.example.tailorapp.service.EmployeeService;
import com.example.tailorapp.service.WorkAssignmentService;
import com.example.tailorapp.service.EmployeePaymentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final WorkAssignmentService workAssignmentService;
    private final EmployeePaymentService employeePaymentService;

    public EmployeeController(EmployeeService employeeService,
                            WorkAssignmentService workAssignmentService,
                            EmployeePaymentService employeePaymentService) {
        this.employeeService = employeeService;
        this.workAssignmentService = workAssignmentService;
        this.employeePaymentService = employeePaymentService;
    }

    /**
     * List all employees
     */
    @GetMapping
    public String listEmployees(@RequestParam(required = false) String status,
                               @RequestParam(required = false) String skill,
                               Model model) {
        List<Employee> employees;

        if (status != null && !status.isEmpty()) {
            employees = employeeService.findByStatus(status);
        } else if (skill != null && !skill.isEmpty()) {
            employees = employeeService.getEmployeesForWorkType(skill);
        } else {
            employees = employeeService.findAll();
        }

        model.addAttribute("employees", employees);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedSkill", skill);
        return "employees/index";
    }

    /**
     * Show form to add new employee
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("employee", new Employee());
        return "employees/add";
    }

    /**
     * Save new employee
     */
    @PostMapping("/add")
    public String addEmployee(@Valid @ModelAttribute Employee employee,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "employees/add";
        }

        employeeService.save(employee);
        redirectAttributes.addFlashAttribute("success", "Employee added successfully!");
        return "redirect:/employees";
    }

    /**
     * Show form to edit employee
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Employee employee = employeeService.findById(id)
                .orElse(null);

        if (employee == null) {
            redirectAttributes.addFlashAttribute("error", "Employee not found!");
            return "redirect:/employees";
        }

        model.addAttribute("employee", employee);
        return "employees/edit";
    }

    /**
     * Update employee
     */
    @PostMapping("/edit/{id}")
    public String updateEmployee(@PathVariable Long id,
                                @Valid @ModelAttribute Employee employee,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "employees/edit";
        }

        employee.setId(id);
        employeeService.save(employee);
        redirectAttributes.addFlashAttribute("success", "Employee updated successfully!");
        return "redirect:/employees";
    }

    /**
     * View employee details
     */
    @GetMapping("/view/{id}")
    public String viewEmployee(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Employee employee = employeeService.findById(id)
                .orElse(null);

        if (employee == null) {
            redirectAttributes.addFlashAttribute("error", "Employee not found!");
            return "redirect:/employees";
        }

        // Get work assignments
        var activeAssignments = workAssignmentService.findActiveAssignments(employee);
        var completedAssignments = workAssignmentService.findByEmployeeAndStatus(employee, "COMPLETED");

        // Get payroll summary
        var payrollSummary = employeePaymentService.getPayrollSummary(employee);

        model.addAttribute("employee", employee);
        model.addAttribute("activeAssignments", activeAssignments);
        model.addAttribute("completedAssignments", completedAssignments);
        model.addAttribute("payrollSummary", payrollSummary);

        return "employees/view";
    }

    /**
     * Delete employee
     */
    @PostMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Check if employee has any assignments
            Employee employee = employeeService.findById(id).orElse(null);
            if (employee != null) {
                var assignments = workAssignmentService.findByEmployee(employee);
                if (!assignments.isEmpty()) {
                    redirectAttributes.addFlashAttribute("error",
                            "Cannot delete employee with existing work assignments. Please set status to INACTIVE instead.");
                    return "redirect:/employees";
                }
            }

            employeeService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Employee deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting employee: " + e.getMessage());
        }
        return "redirect:/employees";
    }

    /**
     * Change employee status
     */
    @PostMapping("/status/{id}")
    public String changeStatus(@PathVariable Long id,
                              @RequestParam String status,
                              RedirectAttributes redirectAttributes) {
        Employee employee = employeeService.findById(id).orElse(null);
        if (employee == null) {
            redirectAttributes.addFlashAttribute("error", "Employee not found!");
            return "redirect:/employees";
        }

        employee.setEmployeeStatus(status);
        employeeService.save(employee);
        redirectAttributes.addFlashAttribute("success", "Employee status updated to " + status);
        return "redirect:/employees/view/" + id;
    }

    /**
     * Search employees
     */
    @GetMapping("/search")
    public String searchEmployees(@RequestParam String query, Model model) {
        List<Employee> employees = employeeService.searchByName(query);
        model.addAttribute("employees", employees);
        model.addAttribute("searchQuery", query);
        return "employees/index";
    }

    /**
     * Get employees by skill (AJAX endpoint)
     */
    @GetMapping("/by-skill/{skill}")
    @ResponseBody
    public List<Employee> getEmployeesBySkill(@PathVariable String skill) {
        return employeeService.getEmployeesForWorkType(skill);
    }
}