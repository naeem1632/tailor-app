package com.example.tailorapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class ExpenseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotNull(message = "Effective date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate effectiveDate; // When these expense rates became active

    @NotNull(message = "Dress expense is required")
    @Min(value = 0, message = "Dress expense must be zero or positive")
    Long dressExpense;

    @NotNull(message = "Waistcoat expense is required")
    @Min(value = 0, message = "Waistcoat expense must be zero or positive")
    Long waistcoatExpense;

    @NotNull(message = "Shirt expense is required")
    @Min(value = 0, message = "Shirt expense must be zero or positive")
    Long shirtExpense;

    @NotNull(message = "Matel expense is required")
    @Min(value = 0, message = "Matel expense must be zero or positive")
    Long matelExpense;

    @NotNull(message = "Tich expense is required")
    @Min(value = 0, message = "Tich expense must be zero or positive")
    Long tichExpense;

    @NotNull(message = "Kanta expense is required")
    @Min(value = 0, message = "Kanta expense must be zero or positive")
    Long kantaExpense;

    @NotNull(message = "Jali expense is required")
    @Min(value = 0, message = "Jali expense must be zero or positive")
    Long jaliExpense;

    @NotNull(message = "Krhai expense is required")
    @Min(value = 0, message = "Krhai expense must be zero or positive")
    Long krhaiExpense;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    String notes; // Optional notes about why costs changed

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
