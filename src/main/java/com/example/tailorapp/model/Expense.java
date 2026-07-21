package com.example.tailorapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Data
public class Expense {

    @Id
    Long id = 1L; // Always a single row with id=1

    @NotNull(message = "Dress expense is required")
    @Min(value = 0, message = "Dress expense must be zero or positive")
    Long dressExpense;

    @NotNull(message = "Waistcoat expense is required")
    @Min(value = 0, message = "Waistcoat expense must be zero or positive")
    Long waistcoatExpense;

    @NotNull(message = "Shirt expense is required")
    @Min(value = 0, message = "Shirt expense must be zero or positive")
    Long shirtExpense;

    @Min(value = 0, message = "Trouser expense must be zero or positive")
    Long trouserExpense = 0L;

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

    public Expense() {
        this.id = 1L;
        this.dressExpense = 0L;
        this.waistcoatExpense = 0L;
        this.shirtExpense = 0L;
        this.trouserExpense = 0L;
        this.matelExpense = 0L;
        this.tichExpense = 0L;
        this.kantaExpense = 0L;
        this.jaliExpense = 0L;
        this.krhaiExpense = 0L;
    }
}