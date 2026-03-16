package com.example.tailorapp.controller;

import com.example.tailorapp.model.Expense;
import com.example.tailorapp.service.ExpenseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
        return "expenses/index";
    }

    @PostMapping("/save")
    public String saveExpenses(@ModelAttribute Expense expense, RedirectAttributes redirectAttributes) {
        expenseService.save(expense);
        redirectAttributes.addFlashAttribute("successMessage", "Expenses saved successfully.");
        return "redirect:/expenses";
    }
}