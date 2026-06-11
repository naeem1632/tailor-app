package com.example.tailorapp.repository;

import com.example.tailorapp.model.ExpenseHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseHistoryRepository extends JpaRepository<ExpenseHistory, Long> {

    // Find all expense records sorted by effective date descending (newest first)
    List<ExpenseHistory> findAllByOrderByEffectiveDateDesc();

    // Find expense rate that was active on a specific date
    // Gets the most recent expense record with effectiveDate <= orderDate
    List<ExpenseHistory> findByEffectiveDateLessThanEqualOrderByEffectiveDateDesc(LocalDate orderDate);

    // Find all expense records within a date range
    List<ExpenseHistory> findByEffectiveDateBetweenOrderByEffectiveDateDesc(LocalDate startDate, LocalDate endDate);

    // Check if an expense record exists for a specific date
    Optional<ExpenseHistory> findByEffectiveDate(LocalDate effectiveDate);
}
