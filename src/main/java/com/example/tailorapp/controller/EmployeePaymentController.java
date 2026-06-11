package com.example.tailorapp.controller;

import com.example.tailorapp.model.Employee;
import com.example.tailorapp.model.EmployeePayment;
import com.example.tailorapp.model.EmployeePaymentTransaction;
import com.example.tailorapp.model.WorkAssignment;
import com.example.tailorapp.service.EmployeePaymentService;
import com.example.tailorapp.service.EmployeeService;
import com.example.tailorapp.service.WorkAssignmentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/employee-payments")
public class EmployeePaymentController {

    private final EmployeePaymentService employeePaymentService;
    private final EmployeeService employeeService;
    private final WorkAssignmentService workAssignmentService;

    public EmployeePaymentController(EmployeePaymentService employeePaymentService,
                                    EmployeeService employeeService,
                                    WorkAssignmentService workAssignmentService) {
        this.employeePaymentService = employeePaymentService;
        this.employeeService = employeeService;
        this.workAssignmentService = workAssignmentService;
    }

    /**
     * List all payment records (default view)
     */
    @GetMapping
    public String listPayments(@RequestParam(required = false) Long employeeId,
                              @RequestParam(required = false) String status,
                              Model model) {
        List<EmployeePayment> payments;

        if (employeeId != null && status != null && !status.isEmpty()) {
            Employee employee = employeeService.findById(employeeId).orElse(null);
            if (employee != null) {
                // Filter by both employee and status
                List<EmployeePayment> allForEmployee = employeePaymentService.findByEmployee(employee);
                payments = allForEmployee.stream()
                        .filter(p -> status.equals(p.getPaymentStatus()))
                        .toList();
                model.addAttribute("selectedEmployee", employee);
            } else {
                payments = employeePaymentService.findByPaymentStatus(status);
            }
            model.addAttribute("selectedStatus", status);
        } else if (employeeId != null) {
            Employee employee = employeeService.findById(employeeId).orElse(null);
            if (employee != null) {
                payments = employeePaymentService.findByEmployee(employee);
                model.addAttribute("selectedEmployee", employee);
            } else {
                payments = employeePaymentService.findAll();
            }
        } else if (status != null && !status.isEmpty()) {
            payments = employeePaymentService.findByPaymentStatus(status);
            model.addAttribute("selectedStatus", status);
        } else {
            payments = employeePaymentService.findAll();
        }

        model.addAttribute("payments", payments);
        model.addAttribute("employees", employeeService.findActiveEmployees());

        return "employee-payments/index";
    }

    /**
     * Show payroll calculator page
     */
    @GetMapping("/payroll")
    public String showPayrollCalculator(@RequestParam(required = false) Long employeeId,
                                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                       Model model) {
        List<Employee> employees = employeeService.findActiveEmployees();
        model.addAttribute("employees", employees);

        if (employeeId != null) {
            Employee employee = employeeService.findById(employeeId).orElse(null);
            if (employee != null) {
                var payrollSummary = employeePaymentService.getPayrollSummary(employee);
                model.addAttribute("employee", employee);
                model.addAttribute("payrollSummary", payrollSummary);

                // Get all assignments for the employee with optional date filtering
                List<WorkAssignment> allAssignments;
                if (startDate != null && endDate != null) {
                    allAssignments = workAssignmentService.findByEmployeeAndDateRange(employee, startDate, endDate);
                    model.addAttribute("startDate", startDate);
                    model.addAttribute("endDate", endDate);
                } else {
                    allAssignments = workAssignmentService.findByEmployee(employee);
                }
                model.addAttribute("allAssignments", allAssignments);
            }
        }

        return "employee-payments/payroll";
    }

    /**
     * Show form to create payment
     */
    @GetMapping("/add")
    public String showAddPaymentForm(@RequestParam(required = false) Long employeeId,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        List<Employee> employees = employeeService.findActiveEmployees();
        model.addAttribute("employees", employees);

        if (employeeId != null) {
            Employee employee = employeeService.findById(employeeId).orElse(null);
            if (employee != null) {
                List<WorkAssignment> unpaidWork = workAssignmentService.findUnpaidCompletedWork(employee);
                Long unpaidAmount = workAssignmentService.calculateUnpaidAmount(employee);

                model.addAttribute("selectedEmployee", employee);
                model.addAttribute("unpaidWork", unpaidWork);
                model.addAttribute("unpaidAmount", unpaidAmount);
            }
        }

        model.addAttribute("payment", new EmployeePayment());
        return "employee-payments/add";
    }

    // ===============================================
    // LEGACY ENDPOINTS - Removed (use generatePaymentRecord and addPaymentTransaction instead)
    // ===============================================

    /**
     * View payment details
     */
    @GetMapping("/view/{id}")
    public String viewPayment(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        EmployeePayment payment = employeePaymentService.findById(id).orElse(null);

        if (payment == null) {
            redirectAttributes.addFlashAttribute("error", "Payment not found!");
            return "redirect:/employee-payments";
        }

        model.addAttribute("payment", payment);
        return "employee-payments/view";
    }

    /**
     * Delete payment
     */
    @PostMapping("/delete/{id}")
    public String deletePayment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            EmployeePayment payment = employeePaymentService.findById(id).orElse(null);
            if (payment != null) {
                // Unmark associated work assignments
                for (WorkAssignment assignment : payment.getWorkAssignments()) {
                    assignment.setIsPaid(false);
                    assignment.setPaidDate(null);
                    assignment.setEmployeePayment(null);
                    workAssignmentService.save(assignment);
                }
            }

            employeePaymentService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Payment deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting payment: " + e.getMessage());
        }
        return "redirect:/employee-payments";
    }

    /**
     * Get unpaid work for employee (AJAX endpoint)
     */
    @GetMapping("/unpaid-work/{employeeId}")
    @ResponseBody
    public List<WorkAssignment> getUnpaidWork(@PathVariable Long employeeId) {
        Employee employee = employeeService.findById(employeeId).orElse(null);
        if (employee != null) {
            return workAssignmentService.findUnpaidCompletedWork(employee);
        }
        return List.of();
    }

    /**
     * Calculate unpaid amount (AJAX endpoint)
     */
    @GetMapping("/unpaid-amount/{employeeId}")
    @ResponseBody
    public Long getUnpaidAmount(@PathVariable Long employeeId) {
        Employee employee = employeeService.findById(employeeId).orElse(null);
        if (employee != null) {
            return workAssignmentService.calculateUnpaidAmount(employee);
        }
        return 0L;
    }

    /**
     * Get payroll summary (AJAX endpoint)
     */
    @GetMapping("/payroll-summary/{employeeId}")
    @ResponseBody
    public EmployeePaymentService.PayrollSummary getPayrollSummary(@PathVariable Long employeeId) {
        Employee employee = employeeService.findById(employeeId).orElse(null);
        if (employee != null) {
            return employeePaymentService.getPayrollSummary(employee);
        }
        return null;
    }

    // ===============================================
    // NEW PAYMENT RECORD & TRANSACTION ENDPOINTS
    // ===============================================

    /**
     * Generate payment record from payroll page
     */
    @PostMapping("/generate-record")
    public String generatePaymentRecord(@RequestParam Long employeeId,
                                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate workPeriodStart,
                                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate workPeriodEnd,
                                       @RequestParam(required = false) String notes,
                                       RedirectAttributes redirectAttributes) {
        try {
            Employee employee = employeeService.findById(employeeId).orElse(null);
            if (employee == null) {
                redirectAttributes.addFlashAttribute("error", "Employee not found!");
                return "redirect:/employee-payments/payroll";
            }

            EmployeePayment paymentRecord = employeePaymentService.generatePaymentRecord(
                    employee, workPeriodStart, workPeriodEnd, notes);

            redirectAttributes.addFlashAttribute("success",
                    "Payment record #" + paymentRecord.getId() + " generated for Rs. " +
                    paymentRecord.getTotalAmount() + ". Status: PENDING");
            return "redirect:/employee-payments";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/employee-payments/payroll?employeeId=" + employeeId +
                   "&startDate=" + workPeriodStart + "&endDate=" + workPeriodEnd;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error generating payment record: " + e.getMessage());
            return "redirect:/employee-payments/payroll?employeeId=" + employeeId;
        }
    }

    /**
     * Add payment transaction to a payment record
     */
    @PostMapping("/add-transaction/{paymentRecordId}")
    public String addPaymentTransaction(@PathVariable Long paymentRecordId,
                                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate paymentDate,
                                       @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime paymentTime,
                                       @RequestParam Long amountPaid,
                                       @RequestParam String paymentMethod,
                                       @RequestParam(required = false) String notes,
                                       RedirectAttributes redirectAttributes) {
        try {
            EmployeePaymentTransaction transaction = employeePaymentService.addPaymentTransaction(
                    paymentRecordId, paymentDate, paymentTime, amountPaid, paymentMethod, notes);

            EmployeePayment paymentRecord = employeePaymentService.findById(paymentRecordId).orElse(null);
            if (paymentRecord != null) {
                redirectAttributes.addFlashAttribute("success",
                        "Payment of Rs. " + amountPaid + " recorded successfully! Status: " +
                        paymentRecord.getPaymentStatus());
            }
            return "redirect:/employee-payments/view/" + paymentRecordId;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/employee-payments/view/" + paymentRecordId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error recording payment: " + e.getMessage());
            return "redirect:/employee-payments/view/" + paymentRecordId;
        }
    }

    /**
     * Show list of all payment records (new default view)
     */
    @GetMapping("/records")
    public String listPaymentRecords(@RequestParam(required = false) Long employeeId,
                                     @RequestParam(required = false) String status,
                                     Model model) {
        List<EmployeePayment> paymentRecords;

        if (employeeId != null && status != null && !status.isEmpty()) {
            Employee employee = employeeService.findById(employeeId).orElse(null);
            if (employee != null) {
                // Filter by both employee and status
                List<EmployeePayment> allForEmployee = employeePaymentService.findPaymentRecordsByEmployee(employee);
                paymentRecords = allForEmployee.stream()
                        .filter(p -> status.equals(p.getPaymentStatus()))
                        .toList();
                model.addAttribute("selectedEmployee", employee);
            } else {
                paymentRecords = employeePaymentService.findByPaymentStatus(status);
            }
            model.addAttribute("selectedStatus", status);
        } else if (employeeId != null) {
            Employee employee = employeeService.findById(employeeId).orElse(null);
            if (employee != null) {
                paymentRecords = employeePaymentService.findPaymentRecordsByEmployee(employee);
                model.addAttribute("selectedEmployee", employee);
            } else {
                paymentRecords = employeePaymentService.findAllPaymentRecords();
            }
        } else if (status != null && !status.isEmpty()) {
            paymentRecords = employeePaymentService.findByPaymentStatus(status);
            model.addAttribute("selectedStatus", status);
        } else {
            paymentRecords = employeePaymentService.findAllPaymentRecords();
        }

        model.addAttribute("payments", paymentRecords);
        model.addAttribute("employees", employeeService.findActiveEmployees());

        return "employee-payments/index";
    }
}