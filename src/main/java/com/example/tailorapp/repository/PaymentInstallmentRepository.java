package com.example.tailorapp.repository;

import com.example.tailorapp.model.PaymentInstallment;
import com.example.tailorapp.model.Payments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentInstallmentRepository extends JpaRepository<PaymentInstallment, Long> {
}
