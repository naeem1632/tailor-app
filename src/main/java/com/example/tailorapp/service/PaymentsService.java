package com.example.tailorapp.service;

import com.example.tailorapp.model.PaymentInstallment;
import com.example.tailorapp.model.Payments;
import com.example.tailorapp.repository.PaymentInstallmentRepository;
import com.example.tailorapp.repository.PaymentsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentsService {

    private final PaymentsRepository paymentsRepository;
    private final PaymentInstallmentRepository installmentRepository;

    public PaymentsService(PaymentsRepository paymentsRepository, PaymentInstallmentRepository installmentRepository) {
        this.paymentsRepository = paymentsRepository;
        this.installmentRepository = installmentRepository;
    }

    public List<Payments> findByClient(Long clientId) {
        return paymentsRepository.findByClientId(clientId);
    }

    public Optional<Payments> findById(Long id) {
        return paymentsRepository.findById(id);
    }

    public void delete(Long id) {
        paymentsRepository.deleteById(id);
    }

    public void save(Payments payment) {
        paymentsRepository.save(payment);
    }

    // ✅ Save & re-sync totals
    public void saveAndSync(Payments payment) {
        syncTotals(payment);
        paymentsRepository.save(payment);
    }

    // ✅ Central total syncing method
    public void syncTotals(Payments payment) {
        long totalPaid = 0;
        if (payment.getInstallments() != null) {
            totalPaid = payment.getInstallments()
                    .stream()
                    .filter(i -> i.getPaidAmount() != null)
                    .mapToLong(PaymentInstallment::getPaidAmount)
                    .sum();
        }

        long total = (payment.getTotalAmount() != null ? payment.getTotalAmount() : 0);
        long remaining = Math.max(total - totalPaid, 0);

        payment.setPaidAmount(totalPaid);
        payment.setRemainingAmount(remaining);

        if (remaining <= 0 && total > 0) payment.setPaymentStatus("Paid");
        else if (totalPaid > 0) payment.setPaymentStatus("Partial");
        else payment.setPaymentStatus("Unpaid");
    }

    // ✅ Delete installment and sync parent totals
    public Optional<Payments> deleteInstallmentAndSync(Long installmentId) {
        Optional<PaymentInstallment> instOpt = installmentRepository.findById(installmentId);
        if (instOpt.isEmpty()) return Optional.empty();

        Payments parent = instOpt.get().getPayment();
        installmentRepository.deleteById(installmentId);

        parent.getInstallments().removeIf(i -> i.getId().equals(installmentId));
        syncTotals(parent);
        paymentsRepository.save(parent);
        return Optional.of(parent);
    }

    // ✅ Edit installment and sync parent totals
    public Optional<Payments> updateInstallmentAndSync(Long installmentId, Long paidAmount, String note, LocalDate paymentDate) {
        Optional<PaymentInstallment> instOpt = installmentRepository.findById(installmentId);
        if (instOpt.isEmpty()) return Optional.empty();

        PaymentInstallment inst = instOpt.get();
        inst.setPaidAmount(paidAmount);
        inst.setNote(note);
        if (paymentDate != null) inst.setPaymentDate(paymentDate);

        installmentRepository.save(inst);

        Payments parent = inst.getPayment();
        syncTotals(parent);
        paymentsRepository.save(parent);
        return Optional.of(parent);
    }
}
