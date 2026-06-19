package com.example.tailorapp.controller;

import com.example.tailorapp.service.EmployeeService;
import com.example.tailorapp.service.PayrollReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/payroll-report")
@PreAuthorize("hasRole('ADMIN')")
public class PayrollReportController {

    @Autowired
    private PayrollReportService payrollReportService;

    @Autowired
    private EmployeeService employeeService;

    /**
     * Show payroll report with date range and employee filter
     */
    @GetMapping
    public String showReport(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                            @RequestParam(required = false) Long employeeId,
                            Model model) {

        // Default to current month if no dates provided
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        PayrollReportService.PayrollReport report = payrollReportService.generatePayrollReport(startDate, endDate, employeeId);

        model.addAttribute("report", report);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("employees", employeeService.findActiveEmployees());
        model.addAttribute("selectedEmployeeId", employeeId);

        return "payroll-report/index";
    }
}
