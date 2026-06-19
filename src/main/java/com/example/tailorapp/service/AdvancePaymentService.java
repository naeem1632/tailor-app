package com.example.tailorapp.service;

import com.example.tailorapp.model.AdvanceDeduction;
import com.example.tailorapp.model.AdvancePayment;
import com.example.tailorapp.model.Employee;
import com.example.tailorapp.model.EmployeePaymentTransaction;
import com.example.tailorapp.repository.AdvanceDeductionRepository;
import com.example.tailorapp.repository.AdvancePaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AdvancePaymentService {

    @Autowired
    private AdvancePaymentRepository advancePaymentRepository;

    @Autowired
    private AdvanceDeductionRepository advanceDeductionRepository;

    public List<AdvancePayment> findAll() {
        return advancePaymentRepository.findAll();
    }

    public Optional<AdvancePayment> findById(Long id) {
        return advancePaymentRepository.findById(id);
    }

    public List<AdvancePayment> findByEmployee(Employee employee) {
        return advancePaymentRepository.findByEmployeeOrderByAdvanceDateDesc(employee);
    }

    public List<AdvancePayment> findByStatus(String status) {
        return advancePaymentRepository.findByStatusOrderByAdvanceDateDesc(status);
    }

    public List<AdvancePayment> findByEmployeeAndStatus(Employee employee, String status) {
        return advancePaymentRepository.findByEmployeeAndStatusOrderByAdvanceDateDesc(employee, status);
    }

    public List<AdvancePayment> findOutstandingByEmployee(Employee employee) {
        return advancePaymentRepository.findOutstandingByEmployee(employee);
    }

    public Long getTotalOutstandingByEmployee(Employee employee) {
        Long total = advancePaymentRepository.getTotalOutstandingByEmployee(employee);
        return total != null ? total : 0L;
    }

    public AdvancePayment save(AdvancePayment advancePayment) {
        return advancePaymentRepository.save(advancePayment);
    }

    @Transactional
    public void deductAdvance(AdvancePayment advancePayment, Long deductionAmount, EmployeePaymentTransaction paymentTransaction) {
        // Create deduction record
        AdvanceDeduction deduction = new AdvanceDeduction();
        deduction.setAdvancePayment(advancePayment);
        deduction.setPaymentTransaction(paymentTransaction);
        deduction.setDeductionAmount(deductionAmount);
        advanceDeductionRepository.save(deduction);

        // Update advance balance
        advancePayment.setBalanceRemaining(advancePayment.getBalanceRemaining() - deductionAmount);

        // Update status
        if (advancePayment.getBalanceRemaining() == 0) {
            advancePayment.setStatus("FULLY_RECOVERED");
        } else if (advancePayment.getBalanceRemaining() < advancePayment.getAmount()) {
            advancePayment.setStatus("PARTIALLY_RECOVERED");
        }

        advancePaymentRepository.save(advancePayment);
    }

    public List<AdvanceDeduction> findDeductionsByAdvance(AdvancePayment advancePayment) {
        return advanceDeductionRepository.findByAdvancePaymentOrderByDeductedAtDesc(advancePayment);
    }

    public List<AdvanceDeduction> findDeductionsByTransaction(EmployeePaymentTransaction paymentTransaction) {
        return advanceDeductionRepository.findByPaymentTransaction(paymentTransaction);
    }

    public void delete(Long id) {
        advancePaymentRepository.deleteById(id);
    }
}
