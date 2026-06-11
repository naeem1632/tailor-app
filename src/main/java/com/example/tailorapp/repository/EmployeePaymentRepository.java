package com.example.tailorapp.repository;

import com.example.tailorapp.model.Employee;
import com.example.tailorapp.model.EmployeePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

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
}