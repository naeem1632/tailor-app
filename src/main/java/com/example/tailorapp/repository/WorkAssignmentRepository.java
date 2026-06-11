package com.example.tailorapp.repository;

import com.example.tailorapp.model.Employee;
import com.example.tailorapp.model.Payments;
import com.example.tailorapp.model.WorkAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkAssignmentRepository extends JpaRepository<WorkAssignment, Long> {

    // Find assignments by payment/order
    List<WorkAssignment> findByPaymentOrderByAssignedDateDesc(Payments payment);

    // Find assignments by employee
    List<WorkAssignment> findByEmployeeOrderByAssignedDateDesc(Employee employee);

    // Find assignments by employee and status
    List<WorkAssignment> findByEmployeeAndStatusOrderByAssignedDateDesc(Employee employee, String status);

    // Find assignments by employee and date range
    List<WorkAssignment> findByEmployeeAndAssignedDateBetweenOrderByAssignedDateDesc(
            Employee employee, LocalDate startDate, LocalDate endDate);

    // Find unpaid completed assignments for an employee
    List<WorkAssignment> findByEmployeeAndStatusAndIsPaidOrderByCompletedDate(
            Employee employee, String status, Boolean isPaid);

    // Find assignments by status
    List<WorkAssignment> findByStatusOrderByAssignedDateDesc(String status);

    // Find assignments by work type
    List<WorkAssignment> findByWorkTypeOrderByAssignedDateDesc(String workType);

    // Find all unpaid assignments for payroll calculation
    List<WorkAssignment> findByIsPaidAndStatus(Boolean isPaid, String status);

    // Find assignments by employee within date range and not paid
    List<WorkAssignment> findByEmployeeAndCompletedDateBetweenAndIsPaid(
            Employee employee, LocalDate startDate, LocalDate endDate, Boolean isPaid);
}