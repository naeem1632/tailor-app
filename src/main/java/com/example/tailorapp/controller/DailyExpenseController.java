package com.example.tailorapp.controller;

import com.example.tailorapp.model.DailyExpense;
import com.example.tailorapp.service.DailyExpenseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/daily-expenses")
@PreAuthorize("hasRole('ADMIN')")
public class DailyExpenseController {

    @Autowired
    private DailyExpenseService dailyExpenseService;

    /**
     * List all daily expenses with optional category filter
     */
    @GetMapping
    public String index(@RequestParam(required = false) String category,
                       Model model) {

        List<DailyExpense> expenses;

        if (category != null && !category.isEmpty()) {
            expenses = dailyExpenseService.findByCategory(category);
            model.addAttribute("selectedCategory", category);
        } else {
            expenses = dailyExpenseService.findAll();
        }

        model.addAttribute("expenses", expenses);
        return "daily-expenses/index";
    }

    /**
     * Show add expense form
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        DailyExpense expense = new DailyExpense();
        // Set defaults
        expense.setExpenseDate(LocalDate.now());
        expense.setExpenseTime(java.time.LocalTime.now());
        expense.setPaymentMethod("CASH");

        model.addAttribute("expense", expense);
        return "daily-expenses/add";
    }

    /**
     * Process add expense
     */
    @PostMapping("/add")
    public String add(@Valid @ModelAttribute("expense") DailyExpense expense,
                     BindingResult result,
                     RedirectAttributes ra,
                     Model model) {

        if (result.hasErrors()) {
            return "daily-expenses/add";
        }

        try {
            dailyExpenseService.save(expense);
            ra.addFlashAttribute("success", "Expense recorded successfully! Amount: Rs. " + expense.getAmount());
            return "redirect:/daily-expenses";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "daily-expenses/add";
        }
    }

    /**
     * View expense details
     */
    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model, RedirectAttributes ra) {
        DailyExpense expense = dailyExpenseService.findById(id).orElse(null);

        if (expense == null) {
            ra.addFlashAttribute("error", "Expense not found!");
            return "redirect:/daily-expenses";
        }

        model.addAttribute("expense", expense);
        return "daily-expenses/view";
    }

    /**
     * Delete expense
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            dailyExpenseService.delete(id);
            ra.addFlashAttribute("success", "Expense deleted successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error deleting expense: " + e.getMessage());
        }
        return "redirect:/daily-expenses";
    }

    /**
     * Show expense report with date range filter
     */
    @GetMapping("/report")
    public String showReport(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                            @RequestParam(required = false) String category,
                            Model model) {

        // Default to current month if no dates provided
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        DailyExpenseService.ExpenseReportSummary report = dailyExpenseService.getExpenseReport(startDate, endDate, category);

        model.addAttribute("report", report);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("selectedCategory", category);

        return "daily-expenses/report";
    }
}
