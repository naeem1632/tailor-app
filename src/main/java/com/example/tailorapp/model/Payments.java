package com.example.tailorapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
public class Payments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate date;
    Long dressCount;
    Long dressRate;
    Long waistcoatCount;
    Long waistcoatRate;
    Long totalAmount;
    Long paidAmount;
    Long remainingAmount;
    String paymentStatus; // paid, unpaid, partial
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate returnDate;
    String returnStatus; // returned, Not yet
    String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    Client client;

    public Payments() {
    }

}
