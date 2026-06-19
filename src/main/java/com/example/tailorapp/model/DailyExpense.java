package com.example.tailorapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Expense date is required")
    private LocalDate expenseDate;

    @NotNull(message = "Expense time is required")
    private LocalTime expenseTime;

    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be at least 1")
    private Long amount;

    @NotBlank(message = "Category is required")
    private String category; // UTILITIES, RENT, MATERIALS, SALARIES, MAINTENANCE, MISCELLANEOUS, OTHER

    private String customCategory; // Used when category = OTHER

    @Column(length = 500)
    private String description;

    private String paymentMethod; // CASH, BANK_TRANSFER, JAZZCASH, EASYPAISA

    @Column(length = 1000)
    private String notes;
}
