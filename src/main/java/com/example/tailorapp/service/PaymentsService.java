package com.example.tailorapp.service;

import com.example.tailorapp.config.AppConstants;
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

    public List<Payments> findAll() {
        return paymentsRepository.findAll();
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
        calculateTotalAmount(payment); // Calculate total including button costs
        syncTotals(payment);
        paymentsRepository.save(payment);
    }

    // ✅ Calculate total amount including button, kanta, and jali costs
    public void calculateTotalAmount(Payments payment) {
        long dressTotal = (payment.getDressCount() != null ? payment.getDressCount() : 0) *
                         (payment.getDressRate() != null ? payment.getDressRate() : 0);

        long waistcoatTotal = (payment.getWaistcoatCount() != null ? payment.getWaistcoatCount() : 0) *
                             (payment.getWaistcoatRate() != null ? payment.getWaistcoatRate() : 0);

        // Calculate Matel button total
        long matelTotal = 0;
        if (payment.getMatelAmount() != null && payment.getWithMatel() != null) {
            matelTotal = payment.getWithMatel() * payment.getMatelAmount();
        }

        // Calculate Tich button total
        long tichTotal = 0;
        if (payment.getTichAmount() != null && payment.getWithTich() != null) {
            tichTotal = payment.getWithTich() * payment.getTichAmount();
        }

        // Calculate Kanta total
        long kantaTotal = 0;
        if (payment.getKantaAmount() != null && payment.getWithKanta() != null) {
            kantaTotal = payment.getWithKanta() * payment.getKantaAmount();
        }

        // Calculate Jali total
        long jaliTotal = 0;
        if (payment.getJaliAmount() != null && payment.getWithJali() != null) {
            jaliTotal = payment.getWithJali() * payment.getJaliAmount();
        }

        payment.setTotalAmount(dressTotal + waistcoatTotal + matelTotal + tichTotal + kantaTotal + jaliTotal);
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

        if (remaining <= 0 && total > 0) payment.setPaymentStatus(AppConstants.PAYMENT_STATUS_PAID);
        else if (totalPaid > 0) payment.setPaymentStatus(AppConstants.PAYMENT_STATUS_PARTIAL);
        else payment.setPaymentStatus(AppConstants.PAYMENT_STATUS_UNPAID);
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

    // ✅ Dashboard helper methods

    // Count active orders (not returned)
    public long countActiveOrders() {
        return paymentsRepository.findAll().stream()
                .filter(p -> p.getReturnStatus() == null || !"returned".equalsIgnoreCase(p.getReturnStatus()))
                .count();
    }

    // Sum all pending payment amounts
    public long sumPendingPayments() {
        return paymentsRepository.findAll().stream()
                .filter(p -> p.getRemainingAmount() != null && p.getRemainingAmount() > 0)
                .mapToLong(Payments::getRemainingAmount)
                .sum();
    }

    // Count orders due for return (overdue + within 7 days)
    public long countDueReturns() {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysLater = today.plusDays(7);

        return paymentsRepository.findAll().stream()
                .filter(p -> p.getReturnDate() != null)
                .filter(p -> p.getReturnStatus() == null || !"returned".equalsIgnoreCase(p.getReturnStatus()))
                .filter(p -> {
                    LocalDate returnDate = p.getReturnDate();
                    // Include both overdue (< today) and upcoming 7 days (<= sevenDaysLater)
                    return !returnDate.isAfter(sevenDaysLater);
                })
                .count();
    }
}
