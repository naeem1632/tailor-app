package com.example.tailorapp.service;

import com.example.tailorapp.model.Employee;
import com.example.tailorapp.model.EmployeePayment;
import com.example.tailorapp.model.EmployeePaymentTransaction;
import com.example.tailorapp.model.WorkAssignment;
import com.example.tailorapp.repository.EmployeePaymentRepository;
import com.example.tailorapp.repository.EmployeePaymentTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EmployeePaymentService {

    private final EmployeePaymentRepository employeePaymentRepository;
    private final EmployeePaymentTransactionRepository transactionRepository;
    private final WorkAssignmentService workAssignmentService;

    public EmployeePaymentService(EmployeePaymentRepository employeePaymentRepository,
                                 EmployeePaymentTransactionRepository transactionRepository,
                                 WorkAssignmentService workAssignmentService) {
        this.employeePaymentRepository = employeePaymentRepository;
        this.transactionRepository = transactionRepository;
        this.workAssignmentService = workAssignmentService;
    }

    public List<EmployeePayment> findAll() {
        return employeePaymentRepository.findAll();
    }

    public Optional<EmployeePayment> findById(Long id) {
        return employeePaymentRepository.findById(id);
    }

    public EmployeePayment save(EmployeePayment payment) {
        return employeePaymentRepository.save(payment);
    }

    public void delete(Long id) {
        employeePaymentRepository.deleteById(id);
    }

    // ===============================================
    // LEGACY METHODS - Removed (use generatePaymentRecord instead)
    // ===============================================

    /**
     * Find payment records by employee
     */
    public List<EmployeePayment> findByEmployee(Employee employee) {
        return employeePaymentRepository.findByEmployeeOrderByCreatedAtDesc(employee);
    }

    /**
     * Calculate total amount paid to employee (sum of all amountPaid)
     */
    public Long calculateTotalPayments(Employee employee) {
        List<EmployeePayment> payments = findByEmployee(employee);
        return payments.stream()
                .mapToLong(EmployeePayment::getAmountPaid)
                .sum();
    }

    /**
     * Get payroll summary for employee
     */
    public PayrollSummary getPayrollSummary(Employee employee) {
        Long totalPaid = calculateTotalPayments(employee);
        Long unpaidAmount = workAssignmentService.calculateUnpaidAmount(employee);
        List<WorkAssignment> unpaidWork = workAssignmentService.findUnpaidCompletedWork(employee);
        List<EmployeePayment> paymentHistory = findByEmployee(employee);

        return new PayrollSummary(employee, totalPaid, unpaidAmount, unpaidWork.size(),
                                 paymentHistory, unpaidWork);
    }

    // ===============================================
    // NEW PAYMENT RECORD & TRANSACTION METHODS
    // ===============================================

    /**
     * Generate a payment record for unpaid completed work in date range
     * This creates a PENDING payment record without actually paying
     * Only includes work that hasn't been assigned to any payment record yet
     */
    public EmployeePayment generatePaymentRecord(Employee employee, LocalDate workPeriodStart,
                                                 LocalDate workPeriodEnd, String notes) {
        // Get all unpaid completed work in date range
        List<WorkAssignment> completedWork = workAssignmentService.findCompletedWorkInDateRange(
                employee, workPeriodStart, workPeriodEnd);

        // Filter out work that's already been assigned to a payment record
        // This prevents duplicate inclusion in multiple payment records
        List<WorkAssignment> unpaidWork = completedWork.stream()
                .filter(assignment -> assignment.getEmployeePayment() == null)
                .toList();

        if (unpaidWork.isEmpty()) {
            throw new IllegalArgumentException("No unpaid work found for the specified date range. " +
                    "All completed work has already been included in payment records.");
        }

        // Calculate total amount
        Long totalAmount = unpaidWork.stream()
                .mapToLong(WorkAssignment::getTotalAmount)
                .sum();

        // Create payment record
        EmployeePayment paymentRecord = new EmployeePayment();
        paymentRecord.setEmployee(employee);
        paymentRecord.setWorkPeriodStart(workPeriodStart);
        paymentRecord.setWorkPeriodEnd(workPeriodEnd);
        paymentRecord.setTotalAmount(totalAmount);
        paymentRecord.setAmountPaid(0L);
        paymentRecord.setPaymentStatus("PENDING");
        paymentRecord.setNotes(notes);

        // Save payment record first
        paymentRecord = employeePaymentRepository.save(paymentRecord);

        // Link work assignments to this payment record
        for (WorkAssignment assignment : unpaidWork) {
            assignment.setEmployeePayment(paymentRecord);
            workAssignmentService.save(assignment);
        }

        return paymentRecord;
    }

    /**
     * Add a payment transaction to a payment record
     * Supports full or partial payments
     */
    public EmployeePaymentTransaction addPaymentTransaction(Long paymentRecordId, LocalDate paymentDate,
                                                            LocalTime paymentTime, Long amountPaid,
                                                            String paymentMethod, String notes) {
        // Find payment record
        EmployeePayment paymentRecord = employeePaymentRepository.findById(paymentRecordId)
                .orElseThrow(() -> new IllegalArgumentException("Payment record not found"));

        // Check if payment amount is valid
        Long remaining = paymentRecord.getAmountRemaining();
        if (amountPaid > remaining) {
            throw new IllegalArgumentException(
                    "Payment amount (" + amountPaid + ") exceeds remaining amount (" + remaining + ")");
        }

        // Create transaction
        EmployeePaymentTransaction transaction = new EmployeePaymentTransaction();
        transaction.setEmployeePayment(paymentRecord);
        transaction.setPaymentDate(paymentDate);
        transaction.setPaymentTime(paymentTime != null ? paymentTime : LocalTime.now());
        transaction.setAmountPaid(amountPaid);
        transaction.setPaymentMethod(paymentMethod);
        transaction.setNotes(notes);

        // Save transaction
        transaction = transactionRepository.save(transaction);

        // Update payment record
        paymentRecord.addPaymentTransaction(transaction);

        // If fully paid, mark all work assignments as paid
        if ("PAID".equals(paymentRecord.getPaymentStatus())) {
            for (WorkAssignment assignment : paymentRecord.getWorkAssignments()) {
                assignment.setIsPaid(true);
                assignment.setPaidDate(paymentDate);
                workAssignmentService.save(assignment);
            }
        }

        employeePaymentRepository.save(paymentRecord);

        return transaction;
    }

    /**
     * Get all payment records ordered by creation date
     */
    public List<EmployeePayment> findAllPaymentRecords() {
        return employeePaymentRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Get payment records by status
     */
    public List<EmployeePayment> findByPaymentStatus(String status) {
        return employeePaymentRepository.findByPaymentStatusOrderByCreatedAtDesc(status);
    }

    /**
     * Get payment records by employee
     */
    public List<EmployeePayment> findPaymentRecordsByEmployee(Employee employee) {
        return employeePaymentRepository.findByEmployeeOrderByCreatedAtDesc(employee);
    }

    /**
     * Get payment transactions for a payment record
     */
    public List<EmployeePaymentTransaction> getTransactionsForPayment(Long paymentRecordId) {
        return transactionRepository.findByEmployeePayment_IdOrderByPaymentDateDesc(paymentRecordId);
    }

    /**
     * Inner class for payroll summary
     */
    public static class PayrollSummary {
        private final Employee employee;
        private final Long totalPaid;
        private final Long unpaidAmount;
        private final int unpaidWorkCount;
        private final List<EmployeePayment> paymentHistory;
        private final List<WorkAssignment> unpaidWork;

        public PayrollSummary(Employee employee, Long totalPaid, Long unpaidAmount,
                             int unpaidWorkCount, List<EmployeePayment> paymentHistory,
                             List<WorkAssignment> unpaidWork) {
            this.employee = employee;
            this.totalPaid = totalPaid;
            this.unpaidAmount = unpaidAmount;
            this.unpaidWorkCount = unpaidWorkCount;
            this.paymentHistory = paymentHistory;
            this.unpaidWork = unpaidWork;
        }

        public Employee getEmployee() { return employee; }
        public Long getTotalPaid() { return totalPaid; }
        public Long getUnpaidAmount() { return unpaidAmount; }
        public int getUnpaidWorkCount() { return unpaidWorkCount; }
        public List<EmployeePayment> getPaymentHistory() { return paymentHistory; }
        public List<WorkAssignment> getUnpaidWork() { return unpaidWork; }
        public Long getTotalEarned() { return totalPaid + unpaidAmount; }
    }
}