package com.example.tailorapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull(message = "Employee is required")
    private Employee employee;

    // Date range for which work is being paid
    @NotNull(message = "Work period start date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate workPeriodStart;

    @NotNull(message = "Work period end date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate workPeriodEnd;

    // Total amount to be paid (sum of all work assignments)
    @Min(value = 0, message = "Total amount must be zero or positive")
    private Long totalAmount;

    // Amount paid so far (sum of all payment transactions)
    @Min(value = 0, message = "Amount paid must be zero or positive")
    private Long amountPaid = 0L;

    // Payment status: PENDING, PARTIALLY_PAID, PAID
    private String paymentStatus = "PENDING";

    // Link to work assignments included in this payment
    // Don't cascade delete - work assignments should remain even if payment is deleted
    @OneToMany(mappedBy = "employeePayment")
    private List<WorkAssignment> workAssignments = new ArrayList<>();

    // Payment transactions (can be multiple for partial payments)
    @OneToMany(mappedBy = "employeePayment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EmployeePaymentTransaction> paymentTransactions = new ArrayList<>();

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (paymentStatus == null || paymentStatus.isEmpty()) {
            paymentStatus = "PENDING";
        }
        if (amountPaid == null) {
            amountPaid = 0L;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatePaymentStatus();
    }

    // Calculate remaining amount
    public Long getAmountRemaining() {
        return totalAmount - (amountPaid != null ? amountPaid : 0L);
    }

    // Update payment status based on amounts
    public void updatePaymentStatus() {
        if (amountPaid == null || amountPaid == 0) {
            paymentStatus = "PENDING";
        } else if (amountPaid >= totalAmount) {
            paymentStatus = "PAID";
        } else {
            paymentStatus = "PARTIALLY_PAID";
        }
    }

    // Add a payment transaction
    public void addPaymentTransaction(EmployeePaymentTransaction transaction) {
        paymentTransactions.add(transaction);
        transaction.setEmployeePayment(this);
        amountPaid = (amountPaid != null ? amountPaid : 0L) + transaction.getAmountPaid();
        updatePaymentStatus();
    }

    /**
     * Calculate total advance deductions across all payment transactions
     * Used by templates to avoid stream operations with lambda expressions
     */
    public Long getTotalAdvanceDeduction() {
        if (paymentTransactions == null || paymentTransactions.isEmpty()) {
            return 0L;
        }
        return paymentTransactions.stream()
                .mapToLong(EmployeePaymentTransaction::getTotalAdvanceDeduction)
                .sum();
    }
}