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
import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdvancePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull(message = "Employee is required")
    private Employee employee;

    @NotNull(message = "Advance date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate advanceDate;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime advanceTime;

    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be at least 1")
    private Long amount;

    private Long balanceRemaining;

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    @NotNull(message = "Payment method is required")
    private String paymentMethod;  // CASH, BANK_TRANSFER, JAZZCASH, EASYPAISA

    private String status;  // PENDING, PARTIALLY_RECOVERED, FULLY_RECOVERED

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (balanceRemaining == null) {
            balanceRemaining = amount;
        }
        if (status == null || status.isEmpty()) {
            status = "PENDING";
        }
    }
}
