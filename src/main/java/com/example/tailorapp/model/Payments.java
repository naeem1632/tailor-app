package com.example.tailorapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentInstallment> installments = new ArrayList<>();

    // helper method
    public void addInstallment(PaymentInstallment installment) {
        installments.add(installment);
        installment.setPayment(this);
    }

    public Payments() {
    }

    public void updateStatus() {
        long paid = installments.stream().mapToLong(PaymentInstallment::getPaidAmount).sum();
        this.remainingAmount = totalAmount - paid;
        if (remainingAmount <= 0) paymentStatus = "Paid";
        else if (paid > 0) paymentStatus = "Partial";
        else paymentStatus = "Unpaid";
    }

}
