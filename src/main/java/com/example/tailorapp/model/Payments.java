package com.example.tailorapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
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

    @NotNull(message = "Payment date is required")
    @PastOrPresent(message = "Payment date cannot be in the future")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate date;

    @Min(value = 0, message = "Dress count must be zero or positive")
    Long dressCount;

    @Min(value = 0, message = "Dress rate must be zero or positive")
    Long dressRate;

    @Min(value = 0, message = "Waistcoat count must be zero or positive")
    Long waistcoatCount;

    @Min(value = 0, message = "Waistcoat rate must be zero or positive")
    Long waistcoatRate;

    @Min(value = 0, message = "Shirt count must be zero or positive")
    Long shirtCount;

    @Min(value = 0, message = "Shirt rate must be zero or positive")
    Long shirtRate;

    @NotNull(message = "Total amount is required")
    @Min(value = 0, message = "Total amount must be zero or positive")
    Long totalAmount;

    @Min(value = 0, message = "Paid amount must be zero or positive")
    Long paidAmount;

    @Min(value = 0, message = "Remaining amount must be zero or positive")
    Long remainingAmount;

    String paymentStatus; // paid, unpaid, partial

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate returnDate;

    String readyStatus; // NOT_READY, READY, NOTIFIED, PICKED_UP

    String orderStatus; // ACTIVE, CANCELLED, REFUNDED, COMPLETED (default: ACTIVE)

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    LocalDateTime notifiedAt;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    String notes;

    // Button fields (Matel and Tich)
    @Min(value = 0, message = "Matel amount must be zero or positive")
    Long matelAmount;
    Integer withMatel;

    @Min(value = 0, message = "Tich amount must be zero or positive")
    Long tichAmount;
    Integer withTich;

    // Kanta and Jali fields
    @Min(value = 0, message = "Kanta amount must be zero or positive")
    Long kantaAmount;
    Integer withKanta;

    @Min(value = 0, message = "Jali amount must be zero or positive")
    Long jaliAmount;
    Integer withJali;

    // Krhai fields
    @Min(value = 0, message = "Krhai amount must be zero or positive")
    Long krhaiAmount;
    Integer withKrhai;

    // Discount
    @Min(value = 0, message = "Discount must be zero or positive")
    Long discount;

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
