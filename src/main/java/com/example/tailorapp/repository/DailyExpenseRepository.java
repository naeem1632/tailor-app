package com.example.tailorapp.repository;

import com.example.tailorapp.model.DailyExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyExpenseRepository extends JpaRepository<DailyExpense, Long> {

    List<DailyExpense> findAllByOrderByExpenseDateDescExpenseTimeDesc();

    List<DailyExpense> findByCategoryOrderByExpenseDateDescExpenseTimeDesc(String category);

    @Query("SELECT e FROM DailyExpense e WHERE e.expenseDate BETWEEN :startDate AND :endDate ORDER BY e.expenseDate DESC, e.expenseTime DESC")
    List<DailyExpense> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT e FROM DailyExpense e WHERE e.category = :category AND e.expenseDate BETWEEN :startDate AND :endDate ORDER BY e.expenseDate DESC, e.expenseTime DESC")
    List<DailyExpense> findByCategoryAndDateRange(@Param("category") String category, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(e.amount) FROM DailyExpense e WHERE e.expenseDate BETWEEN :startDate AND :endDate")
    Long getTotalByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(e.amount) FROM DailyExpense e WHERE e.category = :category AND e.expenseDate BETWEEN :startDate AND :endDate")
    Long getTotalByCategoryAndDateRange(@Param("category") String category, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT e.category, SUM(e.amount) FROM DailyExpense e WHERE e.expenseDate BETWEEN :startDate AND :endDate GROUP BY e.category ORDER BY SUM(e.amount) DESC")
    List<Object[]> getCategorySummaryByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
