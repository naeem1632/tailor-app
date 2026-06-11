package com.example.tailorapp.repository;

import com.example.tailorapp.model.EmployeePayment;
import com.example.tailorapp.model.EmployeePaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeePaymentTransactionRepository extends JpaRepository<EmployeePaymentTransaction, Long> {

    List<EmployeePaymentTransaction> findByEmployeePaymentOrderByPaymentDateDesc(EmployeePayment employeePayment);

    List<EmployeePaymentTransaction> findByEmployeePayment_IdOrderByPaymentDateDesc(Long employeePaymentId);
}