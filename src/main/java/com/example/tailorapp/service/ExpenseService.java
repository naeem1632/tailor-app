package com.example.tailorapp.service;

import com.example.tailorapp.model.Expense;
import com.example.tailorapp.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository repo;

    public ExpenseService(ExpenseRepository repo) {
        this.repo = repo;
    }

    /** Returns the single expense settings row, creating a blank one if it doesn't exist yet. */
    public Expense getExpense() {
        return repo.findById(1L).orElseGet(() -> repo.save(new Expense()));
    }

    public Expense save(Expense expense) {
        expense.setId(1L); // always enforce single row
        return repo.save(expense);
    }
}