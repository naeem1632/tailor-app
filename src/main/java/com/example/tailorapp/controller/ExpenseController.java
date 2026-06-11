package com.example.tailorapp.controller;

import com.example.tailorapp.model.Expense;
import com.example.tailorapp.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public String showExpenses(Model model) {
        model.addAttribute("expense", expenseService.getExpense());
        model.addAttribute("expenseHistory", expenseService.getAllExpenseHistory());
        return "expenses/index";
    }

    @GetMapping("/history")
    public String showExpenseHistory(Model model) {
        model.addAttribute("expenseHistory", expenseService.getAllExpenseHistory());
        return "expenses/history";
    }

    @PostMapping("/save")
    public String saveExpenses(@Valid @ModelAttribute Expense expense,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            model.addAttribute("expense", expense);
            model.addAttribute("errorMessage", "Please correct the errors below.");
            return "expenses/index";
        }

        expenseService.save(expense);
        redirectAttributes.addFlashAttribute("successMessage", "Expenses saved successfully.");
        return "redirect:/expenses";
    }
}