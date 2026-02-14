package com.example.tailorapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    String readyStatus; // NOT_READY, READY, NOTIFIED, PICKED_UP
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    LocalDateTime notifiedAt;
    String notes;

    // Button fields (Matel and Tich)
    Long matelAmount;
    Integer withMatel;
    Long tichAmount;
    Integer withTich;

    // Kanta and Jali fields
    Long kantaAmount;
    Integer withKanta;
    Long jaliAmount;
    Integer withJali;

    // Krhai fields
    Long krhaiAmount;
    Integer withKrhai;

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
