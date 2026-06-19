package com.example.tailorapp.service;

import com.example.tailorapp.model.DailyExpense;
import com.example.tailorapp.repository.DailyExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class DailyExpenseService {

    @Autowired
    private DailyExpenseRepository dailyExpenseRepository;

    public List<DailyExpense> findAll() {
        return dailyExpenseRepository.findAllByOrderByExpenseDateDescExpenseTimeDesc();
    }

    public Optional<DailyExpense> findById(Long id) {
        return dailyExpenseRepository.findById(id);
    }

    public List<DailyExpense> findByCategory(String category) {
        return dailyExpenseRepository.findByCategoryOrderByExpenseDateDescExpenseTimeDesc(category);
    }

    public List<DailyExpense> findByDateRange(LocalDate startDate, LocalDate endDate) {
        return dailyExpenseRepository.findByDateRange(startDate, endDate);
    }

    public List<DailyExpense> findByCategoryAndDateRange(String category, LocalDate startDate, LocalDate endDate) {
        return dailyExpenseRepository.findByCategoryAndDateRange(category, startDate, endDate);
    }

    public Long getTotalByDateRange(LocalDate startDate, LocalDate endDate) {
        Long total = dailyExpenseRepository.getTotalByDateRange(startDate, endDate);
        return total != null ? total : 0L;
    }

    public Long getTotalByCategoryAndDateRange(String category, LocalDate startDate, LocalDate endDate) {
        Long total = dailyExpenseRepository.getTotalByCategoryAndDateRange(category, startDate, endDate);
        return total != null ? total : 0L;
    }

    public Map<String, Long> getCategorySummaryByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = dailyExpenseRepository.getCategorySummaryByDateRange(startDate, endDate);
        Map<String, Long> summary = new LinkedHashMap<>();

        for (Object[] row : results) {
            String category = (String) row[0];
            Long amount = (Long) row[1];
            summary.put(category, amount);
        }

        return summary;
    }

    public DailyExpense save(DailyExpense dailyExpense) {
        // If category is OTHER and customCategory is provided, validate
        if ("OTHER".equals(dailyExpense.getCategory())) {
            if (dailyExpense.getCustomCategory() == null || dailyExpense.getCustomCategory().trim().isEmpty()) {
                throw new IllegalArgumentException("Custom category is required when selecting 'Other'");
            }
        }
        return dailyExpenseRepository.save(dailyExpense);
    }

    public void delete(Long id) {
        dailyExpenseRepository.deleteById(id);
    }

    /**
     * Get expense report summary for a date range
     */
    public ExpenseReportSummary getExpenseReport(LocalDate startDate, LocalDate endDate, String category) {
        ExpenseReportSummary summary = new ExpenseReportSummary();
        summary.setStartDate(startDate);
        summary.setEndDate(endDate);

        if (category != null && !category.isEmpty()) {
            summary.setExpenses(findByCategoryAndDateRange(category, startDate, endDate));
            summary.setTotalAmount(getTotalByCategoryAndDateRange(category, startDate, endDate));
            summary.setFilteredCategory(category);
        } else {
            summary.setExpenses(findByDateRange(startDate, endDate));
            summary.setTotalAmount(getTotalByDateRange(startDate, endDate));
            summary.setCategorySummary(getCategorySummaryByDateRange(startDate, endDate));
        }

        return summary;
    }

    /**
     * Inner class for expense report summary
     */
    public static class ExpenseReportSummary {
        private LocalDate startDate;
        private LocalDate endDate;
        private List<DailyExpense> expenses;
        private Long totalAmount;
        private String filteredCategory;
        private Map<String, Long> categorySummary;

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

        public List<DailyExpense> getExpenses() {
            return expenses;
        }

        public void setExpenses(List<DailyExpense> expenses) {
            this.expenses = expenses;
        }

        public Long getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(Long totalAmount) {
            this.totalAmount = totalAmount;
        }

        public String getFilteredCategory() {
            return filteredCategory;
        }

        public void setFilteredCategory(String filteredCategory) {
            this.filteredCategory = filteredCategory;
        }

        public Map<String, Long> getCategorySummary() {
            return categorySummary;
        }

        public void setCategorySummary(Map<String, Long> categorySummary) {
            this.categorySummary = categorySummary;
        }
    }
}
