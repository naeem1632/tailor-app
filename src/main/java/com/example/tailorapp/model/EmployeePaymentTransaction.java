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
public class EmployeePaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_payment_id", nullable = false)
    @NotNull(message = "Employee payment record is required")
    private EmployeePayment employeePayment;

    @NotNull(message = "Payment date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate paymentDate;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime paymentTime;

    @NotNull(message = "Amount paid is required")
    @Min(value = 1, message = "Amount paid must be greater than zero")
    private Long amountPaid;

    @NotNull(message = "Payment method is required")
    private String paymentMethod; // CASH, BANK_TRANSFER, JAZZCASH, EASYPAISA

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (paymentMethod == null || paymentMethod.isEmpty()) {
            paymentMethod = "CASH";
        }
        if (paymentTime == null) {
            paymentTime = LocalTime.now();
        }
    }
}