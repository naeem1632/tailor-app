package com.example.tailorapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdvanceDeduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advance_payment_id", nullable = false)
    @NotNull(message = "Advance payment is required")
    private AdvancePayment advancePayment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_transaction_id", nullable = false)
    @NotNull(message = "Payment transaction is required")
    private EmployeePaymentTransaction paymentTransaction;

    @NotNull(message = "Deduction amount is required")
    @Min(value = 1, message = "Deduction amount must be at least 1")
    private Long deductionAmount;

    private LocalDateTime deductedAt;

    @PrePersist
    protected void onCreate() {
        deductedAt = LocalDateTime.now();
    }
}
