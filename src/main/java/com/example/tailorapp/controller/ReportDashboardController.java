package com.example.tailorapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reports")
public class ReportDashboardController {

    @GetMapping("/dashboard")
    public String showReportsDashboard() {
        return "reports/dashboard";
    }

    @GetMapping("/payment-report")
    public String showPaymentReportSelection() {
        return "reports/payment-report-selection";
    }

    @GetMapping("/profit-analysis")
    public String showProfitAnalysisSelection() {
        return "reports/profit-analysis-selection";
    }
}