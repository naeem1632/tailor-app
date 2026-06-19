package com.example.tailorapp.repository;

import com.example.tailorapp.model.AdvanceDeduction;
import com.example.tailorapp.model.AdvancePayment;
import com.example.tailorapp.model.EmployeePaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdvanceDeductionRepository extends JpaRepository<AdvanceDeduction, Long> {

    List<AdvanceDeduction> findByAdvancePaymentOrderByDeductedAtDesc(AdvancePayment advancePayment);

    List<AdvanceDeduction> findByPaymentTransaction(EmployeePaymentTransaction paymentTransaction);
}
