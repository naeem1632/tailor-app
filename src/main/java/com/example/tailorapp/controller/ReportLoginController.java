package com.example.tailorapp.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/reports")
public class ReportLoginController {

    @Value("${reports.password:admin123}")
    private String reportsPassword;

    @GetMapping("/login")
    public String showLoginPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid password. Please try again.");
        }
        return "reports/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam("password") String password, HttpSession session) {
        if (reportsPassword.equals(password)) {
            session.setAttribute("reportAuthenticated", true);

            // Redirect to originally requested URL or default to reports dashboard
            String requestedUrl = (String) session.getAttribute("requestedReportUrl");
            session.removeAttribute("requestedReportUrl");

            if (requestedUrl != null && !requestedUrl.isEmpty()) {
                return "redirect:" + requestedUrl;
            }
            return "redirect:/reports/dashboard";
        } else {
            return "redirect:/reports/login?error=true";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("reportAuthenticated");
        return "redirect:/";
    }
}