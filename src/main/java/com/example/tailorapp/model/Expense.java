package com.example.tailorapp.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Expense {

    @Id
    Long id = 1L; // Always a single row with id=1

    Long dressExpense;
    Long waistcoatExpense;
    Long shirtExpense;

    Long matelExpense;
    Long tichExpense;
    Long kantaExpense;
    Long jaliExpense;
    Long krhaiExpense;

    public Expense() {
        this.id = 1L;
        this.dressExpense = 0L;
        this.waistcoatExpense = 0L;
        this.shirtExpense = 0L;
        this.matelExpense = 0L;
        this.tichExpense = 0L;
        this.kantaExpense = 0L;
        this.jaliExpense = 0L;
        this.krhaiExpense = 0L;
    }
}