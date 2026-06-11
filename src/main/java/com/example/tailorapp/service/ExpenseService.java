package com.example.tailorapp.service;

import com.example.tailorapp.model.ExpenseHistory;
import com.example.tailorapp.repository.ExpenseHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ExpenseService {

    private final ExpenseHistoryRepository historyRepo;

    public ExpenseService(ExpenseHistoryRepository historyRepo) {
        this.historyRepo = historyRepo;
    }

    /**
     * Returns current active expense settings (most recent by effective date)
     */
    public ExpenseHistory getExpenses() {
        return historyRepo.findAll().stream()
                .max(Comparator.comparing(ExpenseHistory::getEffectiveDate))
                .orElseGet(() -> {
                    ExpenseHistory defaultExpenses = new ExpenseHistory();
                    defaultExpenses.setEffectiveDate(LocalDate.now());
                    defaultExpenses.setDressExpense(0L);
                    defaultExpenses.setWaistcoatExpense(0L);
                    defaultExpenses.setShirtExpense(0L);
                    defaultExpenses.setMatelExpense(0L);
                    defaultExpenses.setTichExpense(0L);
                    defaultExpenses.setKantaExpense(0L);
                    defaultExpenses.setJaliExpense(0L);
                    defaultExpenses.setKrhaiExpense(0L);
                    defaultExpenses.setNotes("Initial default expenses");
                    return historyRepo.save(defaultExpenses);
                });
    }

    /**
     * Get all expense records ordered by effective date (newest first)
     */
    public List<ExpenseHistory> getAllExpenses() {
        List<ExpenseHistory> list = historyRepo.findAll();
        list.sort(Comparator.comparing(ExpenseHistory::getEffectiveDate).reversed());
        return list;
    }

    /**
     * Get current expense settings (most recent by effective date)
     */
    public Optional<ExpenseHistory> getCurrentExpenses() {
        return historyRepo.findAll().stream()
                .max(Comparator.comparing(ExpenseHistory::getEffectiveDate));
    }

    /**
     * Find expense record by ID
     */
    public Optional<ExpenseHistory> findById(Long id) {
        return historyRepo.findById(id);
    }

    /**
     * Save expense record
     */
    public ExpenseHistory save(ExpenseHistory expenses) {
        if (expenses.getEffectiveDate() == null) {
            expenses.setEffectiveDate(LocalDate.now());
        }
        return historyRepo.save(expenses);
    }

    /**
     * Delete expense record by ID
     */
    public void deleteById(Long id) {
        historyRepo.deleteById(id);
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
        ExpenseHistory currentExpenses = getExpenses();
        ExpenseHistory fallback = new ExpenseHistory();
        fallback.setEffectiveDate(defaultDate); // Use Feb 1, 2026 as default
        fallback.setDressExpense(currentExpenses.getDressExpense());
        fallback.setWaistcoatExpense(currentExpenses.getWaistcoatExpense());
        fallback.setShirtExpense(currentExpenses.getShirtExpense());
        fallback.setMatelExpense(currentExpenses.getMatelExpense());
        fallback.setTichExpense(currentExpenses.getTichExpense());
        fallback.setKantaExpense(currentExpenses.getKantaExpense());
        fallback.setJaliExpense(currentExpenses.getJaliExpense());
        fallback.setKrhaiExpense(currentExpenses.getKrhaiExpense());
        return fallback;
    }
}