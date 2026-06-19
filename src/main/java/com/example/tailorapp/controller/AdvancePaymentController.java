package com.example.tailorapp.controller;

import com.example.tailorapp.model.AdvancePayment;
import com.example.tailorapp.model.Employee;
import com.example.tailorapp.service.AdvancePaymentService;
import com.example.tailorapp.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/employee-advances")
@PreAuthorize("hasRole('ADMIN')")
public class AdvancePaymentController {

    @Autowired
    private AdvancePaymentService advancePaymentService;

    @Autowired
    private EmployeeService employeeService;

    /**
     * List all advance payments with optional filters
     */
    @GetMapping
    public String index(@RequestParam(required = false) Long employeeId,
                       @RequestParam(required = false) String status,
                       Model model) {

        List<AdvancePayment> advances;
        Employee selectedEmployee = null;

        if (employeeId != null) {
            selectedEmployee = employeeService.findById(employeeId).orElse(null);
            if (selectedEmployee != null) {
                if (status != null && !status.isEmpty()) {
                    advances = advancePaymentService.findByEmployeeAndStatus(selectedEmployee, status);
                } else {
                    advances = advancePaymentService.findByEmployee(selectedEmployee);
                }
            } else {
                advances = advancePaymentService.findAll();
            }
        } else if (status != null && !status.isEmpty()) {
            advances = advancePaymentService.findByStatus(status);
        } else {
            advances = advancePaymentService.findAll();
        }

        model.addAttribute("advances", advances);
        model.addAttribute("employees", employeeService.findAll());
        model.addAttribute("selectedEmployee", selectedEmployee);
        model.addAttribute("selectedStatus", status);

        return "employee-advances/index";
    }

    /**
     * Show add advance payment form
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        AdvancePayment advancePayment = new AdvancePayment();
        // Set defaults
        advancePayment.setAdvanceDate(java.time.LocalDate.now());
        advancePayment.setAdvanceTime(java.time.LocalTime.now());
        advancePayment.setPaymentMethod("CASH");

        model.addAttribute("advancePayment", advancePayment);
        model.addAttribute("employees", employeeService.findAll());
        return "employee-advances/add";
    }

    /**
     * Process add advance payment
     */
    @PostMapping("/add")
    public String add(@ModelAttribute AdvancePayment advancePayment,
                     @RequestParam(required = false) Long employeeId,
                     RedirectAttributes ra,
                     Model model) {

        // Check if employee is selected
        if (employeeId == null) {
            model.addAttribute("error", "Please select an employee!");
            model.addAttribute("employees", employeeService.findAll());
            model.addAttribute("advancePayment", advancePayment);
            return "employee-advances/add";
        }

        // Set employee from employeeId
        Employee employee = employeeService.findById(employeeId).orElse(null);
        if (employee == null) {
            model.addAttribute("error", "Employee not found!");
            model.addAttribute("employees", employeeService.findAll());
            model.addAttribute("advancePayment", advancePayment);
            return "employee-advances/add";
        }
        advancePayment.setEmployee(employee);

        // Validate required fields
        if (advancePayment.getAmount() == null || advancePayment.getAmount() < 1) {
            model.addAttribute("error", "Amount must be at least 1");
            model.addAttribute("employees", employeeService.findAll());
            model.addAttribute("advancePayment", advancePayment);
            return "employee-advances/add";
        }

        if (advancePayment.getAdvanceDate() == null) {
            model.addAttribute("error", "Advance date is required");
            model.addAttribute("employees", employeeService.findAll());
            model.addAttribute("advancePayment", advancePayment);
            return "employee-advances/add";
        }

        if (advancePayment.getPaymentMethod() == null || advancePayment.getPaymentMethod().isEmpty()) {
            model.addAttribute("error", "Payment method is required");
            model.addAttribute("employees", employeeService.findAll());
            model.addAttribute("advancePayment", advancePayment);
            return "employee-advances/add";
        }

        try {
            advancePaymentService.save(advancePayment);
            ra.addFlashAttribute("success", "Advance payment of Rs. " + advancePayment.getAmount() + " recorded successfully!");
            return "redirect:/employee-advances";
        } catch (Exception e) {
            model.addAttribute("error", "Error saving advance payment: " + e.getMessage());
            model.addAttribute("employees", employeeService.findAll());
            model.addAttribute("advancePayment", advancePayment);
            return "employee-advances/add";
        }
    }

    /**
     * View advance payment details
     */
    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model, RedirectAttributes ra) {
        AdvancePayment advancePayment = advancePaymentService.findById(id).orElse(null);

        if (advancePayment == null) {
            ra.addFlashAttribute("error", "Advance payment not found!");
            return "redirect:/employee-advances";
        }

        model.addAttribute("advancePayment", advancePayment);
        model.addAttribute("deductions", advancePaymentService.findDeductionsByAdvance(advancePayment));

        return "employee-advances/view";
    }

    /**
     * Delete advance payment
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        AdvancePayment advancePayment = advancePaymentService.findById(id).orElse(null);

        if (advancePayment == null) {
            ra.addFlashAttribute("error", "Advance payment not found!");
            return "redirect:/employee-advances";
        }

        // Check if any deductions have been made
        if (advancePayment.getBalanceRemaining() < advancePayment.getAmount()) {
            ra.addFlashAttribute("error", "Cannot delete advance payment with deductions. Balance: Rs. " +
                advancePayment.getBalanceRemaining() + " / " + advancePayment.getAmount());
            return "redirect:/employee-advances";
        }

        advancePaymentService.delete(id);
        ra.addFlashAttribute("success", "Advance payment deleted successfully!");
        return "redirect:/employee-advances";
    }
}
