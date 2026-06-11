package com.example.tailorapp.service;

import com.example.tailorapp.model.Expense;
import com.example.tailorapp.model.ExpenseHistory;
import com.example.tailorapp.repository.ExpenseHistoryRepository;
import com.example.tailorapp.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository repo;
    private final ExpenseHistoryRepository historyRepo;

    public ExpenseService(ExpenseRepository repo, ExpenseHistoryRepository historyRepo) {
        this.repo = repo;
        this.historyRepo = historyRepo;
    }

    /** Returns the single expense settings row, creating a blank one if it doesn't exist yet. */
    public Expense getExpense() {
        return repo.findById(1L).orElseGet(() -> repo.save(new Expense()));
    }

    /**
     * Save current expense settings and create a historical record if values changed
     */
    public Expense save(Expense expense) {
        expense.setId(1L); // always enforce single row

        // Get current expense values before saving
        Expense oldExpense = repo.findById(1L).orElse(null);

        // Check if expense values have changed
        boolean hasChanged = oldExpense == null || hasExpenseChanged(oldExpense, expense);

        // Save the new expense values
        Expense saved = repo.save(expense);

        // If values changed, create a historical record with today's date
        if (hasChanged) {
            ExpenseHistory history = new ExpenseHistory();
            history.setEffectiveDate(LocalDate.now());
            history.setDressExpense(expense.getDressExpense());
            history.setWaistcoatExpense(expense.getWaistcoatExpense());
            history.setShirtExpense(expense.getShirtExpense());
            history.setMatelExpense(expense.getMatelExpense());
            history.setTichExpense(expense.getTichExpense());
            history.setKantaExpense(expense.getKantaExpense());
            history.setJaliExpense(expense.getJaliExpense());
            history.setKrhaiExpense(expense.getKrhaiExpense());
            history.setNotes("Updated expense rates");

            historyRepo.save(history);
        }

        return saved;
    }

    /**
     * Check if expense values have changed
     */
    private boolean hasExpenseChanged(Expense old, Expense newExpense) {
        return !old.getDressExpense().equals(newExpense.getDressExpense()) ||
               !old.getWaistcoatExpense().equals(newExpense.getWaistcoatExpense()) ||
               !old.getShirtExpense().equals(newExpense.getShirtExpense()) ||
               !old.getMatelExpense().equals(newExpense.getMatelExpense()) ||
               !old.getTichExpense().equals(newExpense.getTichExpense()) ||
               !old.getKantaExpense().equals(newExpense.getKantaExpense()) ||
               !old.getJaliExpense().equals(newExpense.getJaliExpense()) ||
               !old.getKrhaiExpense().equals(newExpense.getKrhaiExpense());
    }

    /**
     * Get expense rates that were active on a specific date
     * Returns the most recent expense record with effectiveDate <= orderDate
     */
    public ExpenseHistory getExpenseForDate(LocalDate orderDate) {
        List<ExpenseHistory> expenses = historyRepo.findByEffectiveDateLessThanEqualOrderByEffectiveDateDesc(orderDate);

        if (!expenses.isEmpty()) {
            return expenses.get(0); // Return the most recent one
        }

        // If no historical record found, try to get the default Feb 1, 2026 record
        LocalDate defaultDate = LocalDate.of(2026, 2, 1);
        List<ExpenseHistory> defaultExpenses = historyRepo.findByEffectiveDateLessThanEqualOrderByEffectiveDateDesc(defaultDate);

        if (!defaultExpenses.isEmpty()) {
            return defaultExpenses.get(0);
        }

        // Ultimate fallback: create one from current settings
        Expense currentExpense = getExpense();
        ExpenseHistory fallback = new ExpenseHistory();
        fallback.setEffectiveDate(defaultDate); // Use Feb 1, 2026 as default
        fallback.setDressExpense(currentExpense.getDressExpense());
        fallback.setWaistcoatExpense(currentExpense.getWaistcoatExpense());
        fallback.setShirtExpense(currentExpense.getShirtExpense());
        fallback.setMatelExpense(currentExpense.getMatelExpense());
        fallback.setTichExpense(currentExpense.getTichExpense());
        fallback.setKantaExpense(currentExpense.getKantaExpense());
        fallback.setJaliExpense(currentExpense.getJaliExpense());
        fallback.setKrhaiExpense(currentExpense.getKrhaiExpense());
        return fallback;
    }

    /**
     * Get all expense history records
     */
    public List<ExpenseHistory> getAllExpenseHistory() {
        return historyRepo.findAllByOrderByEffectiveDateDesc();
    }

    /**
     * Save a new expense history record with a specific effective date
     */
    public ExpenseHistory saveExpenseHistory(ExpenseHistory history) {
        return historyRepo.save(history);
    }
}