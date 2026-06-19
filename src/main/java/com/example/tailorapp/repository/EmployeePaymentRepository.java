package com.example.tailorapp.repository;

import com.example.tailorapp.model.Employee;
import com.example.tailorapp.model.EmployeePayment;
import com.example.tailorapp.model.EmployeePaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeePaymentRepository extends JpaRepository<EmployeePayment, Long> {

    // Find all payment records ordered by creation date
    List<EmployeePayment> findAllByOrderByCreatedAtDesc();

    // Find payments by employee
    List<EmployeePayment> findByEmployeeOrderByCreatedAtDesc(Employee employee);

    // Find payments by employee and status
    List<EmployeePayment> findByEmployeeAndPaymentStatusOrderByCreatedAtDesc(Employee employee, String paymentStatus);

    // Find payments by status
    List<EmployeePayment> findByPaymentStatusOrderByCreatedAtDesc(String paymentStatus);

    // Find payments by employee and work period date range
    List<EmployeePayment> findByEmployeeAndWorkPeriodStartBetweenOrderByCreatedAtDesc(
            Employee employee, LocalDate startDate, LocalDate endDate);

    // Multi-step eager loading to avoid MultipleBagFetchException
    // Step 1: Fetch EmployeePayment with paymentTransactions
    @Query("SELECT DISTINCT p FROM EmployeePayment p " +
           "LEFT JOIN FETCH p.paymentTransactions " +
           "ORDER BY p.createdAt DESC")
    List<EmployeePayment> findAllWithTransactions();

    // Step 2: Fetch advanceDeductions by fetching transactions separately
    @Query("SELECT DISTINCT pt FROM EmployeePaymentTransaction pt " +
           "LEFT JOIN FETCH pt.advanceDeductions " +
           "WHERE pt.employeePayment IN :payments")
    List<EmployeePaymentTransaction> fetchAdvanceDeductionsForTransactions(List<EmployeePayment> payments);

    // Step 3: Fetch workAssignments
    @Query("SELECT DISTINCT p FROM EmployeePayment p " +
           "LEFT JOIN FETCH p.workAssignments " +
           "WHERE p IN :payments")
    List<EmployeePayment> fetchWorkAssignments(List<EmployeePayment> payments);

    // Step 4: Fetch nested payment and client for work assignments (for detail view)
    @Query("SELECT DISTINCT p FROM EmployeePayment p " +
           "LEFT JOIN FETCH p.workAssignments wa " +
           "LEFT JOIN FETCH wa.payment pmt " +
           "LEFT JOIN FETCH pmt.client " +
           "WHERE p IN :payments")
    List<EmployeePayment> fetchWorkAssignmentsWithPaymentAndClient(List<EmployeePayment> payments);

    // Find by employee - step 1
    @Query("SELECT DISTINCT p FROM EmployeePayment p " +
           "LEFT JOIN FETCH p.paymentTransactions " +
           "WHERE p.employee = :employee " +
           "ORDER BY p.createdAt DESC")
    List<EmployeePayment> findByEmployeeWithTransactions(Employee employee);

    // Find by status - step 1
    @Query("SELECT DISTINCT p FROM EmployeePayment p " +
           "LEFT JOIN FETCH p.paymentTransactions " +
           "WHERE p.paymentStatus = :status " +
           "ORDER BY p.createdAt DESC")
    List<EmployeePayment> findByPaymentStatusWithTransactions(String status);

    // Find by id - step 1: fetch with transactions only
    @Query("SELECT p FROM EmployeePayment p " +
           "LEFT JOIN FETCH p.paymentTransactions " +
           "WHERE p.id = :id")
    Optional<EmployeePayment> findByIdWithTransactions(Long id);
}