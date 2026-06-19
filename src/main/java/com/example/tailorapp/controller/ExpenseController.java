package com.example.tailorapp.controller;

import com.example.tailorapp.model.ExpenseHistory;
import com.example.tailorapp.service.ExpenseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    /**
     * List all expense records with history
     */
    @GetMapping
    public String listExpenses(Model model) {
        List<ExpenseHistory> expensesList = expenseService.getAllExpenses();
        Optional<ExpenseHistory> currentExpenses = expenseService.getCurrentExpenses();

        model.addAttribute("expensesList", expensesList);
        model.addAttribute("currentExpenses", currentExpenses.orElse(null));
        return "expenses/index";
    }

    /**
     * Show form to add new expense record
     */
    @GetMapping("/add")
    public String showAddForm(Model model) {
        Optional<ExpenseHistory> currentExpensesOpt = expenseService.getCurrentExpenses();

        // Create new expenses pre-filled with current expenses for convenience
        ExpenseHistory newExpenses = new ExpenseHistory();

        // Set default effective date to today
        newExpenses.setEffectiveDate(java.time.LocalDate.now());

        if (currentExpensesOpt.isPresent()) {
            ExpenseHistory currentExpenses = currentExpensesOpt.get();
            newExpenses.setDressExpense(currentExpenses.getDressExpense());
            newExpenses.setWaistcoatExpense(currentExpenses.getWaistcoatExpense());
            newExpenses.setShirtExpense(currentExpenses.getShirtExpense());
            newExpenses.setMatelExpense(currentExpenses.getMatelExpense());
            newExpenses.setTichExpense(currentExpenses.getTichExpense());
            newExpenses.setKantaExpense(currentExpenses.getKantaExpense());
            newExpenses.setJaliExpense(currentExpenses.getJaliExpense());
            newExpenses.setKrhaiExpense(currentExpenses.getKrhaiExpense());
        }

        model.addAttribute("expenses", newExpenses);
        model.addAttribute("currentExpenses", currentExpensesOpt.orElse(null));
        return "expenses/add";
    }

    /**
     * Save new expense record
     */
    @PostMapping("/save")
    public String saveExpenses(@ModelAttribute ExpenseHistory expenses, RedirectAttributes redirectAttributes) {
        expenseService.save(expenses);
        redirectAttributes.addFlashAttribute("success", "Expense rates saved successfully.");
        return "redirect:/expenses";
    }

    /**
     * Show view page for specific expense record
     */
    @GetMapping("/view/{id}")
    public String viewExpenses(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<ExpenseHistory> expensesOpt = expenseService.findById(id);
        if (expensesOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Expense record not found.");
            return "redirect:/expenses";
        }
        model.addAttribute("expenses", expensesOpt.get());
        return "expenses/view";
    }

    /**
     * Show edit form
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<ExpenseHistory> expensesOpt = expenseService.findById(id);
        if (expensesOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Expense record not found.");
            return "redirect:/expenses";
        }
        model.addAttribute("expenses", expensesOpt.get());
        return "expenses/edit";
    }

    /**
     * Update existing expense record
     */
    @PostMapping("/update/{id}")
    public String updateExpenses(@PathVariable Long id, @ModelAttribute ExpenseHistory expenses, RedirectAttributes redirectAttributes) {
        expenses.setId(id);
        expenseService.save(expenses);
        redirectAttributes.addFlashAttribute("success", "Expense rates updated successfully.");
        return "redirect:/expenses";
    }

    /**
     * Show expense history page (legacy route - now redirects to main index)
     */
    @GetMapping("/history")
    public String showExpenseHistory() {
        return "redirect:/expenses";
    }
}