package com.example.tailorapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentInstallment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate paymentDate;
    private Long paidAmount;
    private String paymentType; // Cash, Bank Transfer, JazzCash, EasyPaisa
    private String note;
    @ManyToOne(fetch = FetchType.LAZY)
    private Payments payment;

    @PrePersist
    private void setDefaultPaymentType() {
        // Only set default if paymentType is truly null (not explicitly set by user)
        if (this.paymentType == null) {
            this.paymentType = "Cash";
        }
    }
}
