package com.example.tailorapp.controller;

import com.example.tailorapp.model.Client;
import com.example.tailorapp.model.PaymentInstallment;
import com.example.tailorapp.model.Payments;
import com.example.tailorapp.model.PriceSettings;
import com.example.tailorapp.service.ClientService;
import com.example.tailorapp.service.PaymentsService;
import com.example.tailorapp.service.PriceSettingsService;
import com.example.tailorapp.service.WhatsAppService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/payments")
public class PaymentsController {

    private final PaymentsService paymentsService;
    private final ClientService clientService;
    private final WhatsAppService whatsAppService;
    private final PriceSettingsService priceSettingsService;

    public PaymentsController(PaymentsService paymentsService, ClientService clientService,
                              WhatsAppService whatsAppService, PriceSettingsService priceSettingsService) {
        this.paymentsService = paymentsService;
        this.clientService = clientService;
        this.whatsAppService = whatsAppService;
        this.priceSettingsService = priceSettingsService;
    }

    /** Returns default rates as JSON for the Add Payment form pre-fill. */
    @GetMapping("/default-rates")
    @ResponseBody
    public ResponseEntity<PriceSettings> getDefaultRates() {
        return ResponseEntity.ok(priceSettingsService.getSettings());
    }

    // ✅ Show all payments for a client
    @GetMapping("/client/{clientId}")
    public String listPayments(@PathVariable Long clientId, Model model) {
        Optional<Client> clientOpt = clientService.findById(clientId);
        if (clientOpt.isEmpty()) return "redirect:/clients";

        List<Payments> payments = paymentsService.findByClient(clientId);
        payments.sort(Comparator.comparing(Payments::getDate).reversed());

        // Auto-sync each payment totals
        payments.forEach(paymentsService::syncTotals);

        Payments newPayment = new Payments();
        newPayment.setClient(clientOpt.get());
        newPayment.setDate(LocalDate.now());

        model.addAttribute("client", clientOpt.get());
        model.addAttribute("payments", payments);
        model.addAttribute("payment", newPayment);
        return "payments/list";
    }

    // ✅ Save new or edited payment
    @PostMapping("/save")
    public String savePayment(@ModelAttribute Payments payment, RedirectAttributes ra) {
        if (payment.getId() != null) {
            // Load existing payment to retain installments
            Payments existing = paymentsService.findById(payment.getId()).orElse(null);
            if (existing != null) {
                payment.setInstallments(existing.getInstallments());
                payment.setClient(existing.getClient());
            }
        }

        paymentsService.saveAndSync(payment);
        ra.addFlashAttribute("message", "Payment saved successfully");
        return "redirect:/payments/client/" + payment.getClient().getId();
    }


    // ✅ Delete a payment
    @GetMapping("/delete/{id}")
    public String deletePayment(@PathVariable Long id, RedirectAttributes ra) {
        Optional<Payments> p = paymentsService.findById(id);
        if (p.isEmpty()) return "redirect:/clients";

        Long clientId = p.get().getClient().getId();
        paymentsService.delete(id);
        ra.addFlashAttribute("message", "Payment deleted successfully");
        return "redirect:/payments/client/" + clientId;
    }

    // ✅ Add a payment installment
    @PostMapping("/installment/add")
    public String addInstallment(@RequestParam("paymentId") Long paymentId,
                                 @RequestParam("paidAmount") Long paidAmount,
                                 @RequestParam(value = "note", required = false) String note,
                                 @RequestParam(value = "paymentDate", required = false) LocalDate paymentDate,
                                 RedirectAttributes ra) {

        Payments payment = paymentsService.findById(paymentId).orElse(null);
        if (payment == null) {
            ra.addFlashAttribute("error", "Payment not found");
            return "redirect:/clients";
        }

        if (paymentDate == null) paymentDate = LocalDate.now();

        PaymentInstallment installment = new PaymentInstallment();
        installment.setPayment(payment);
        installment.setPaidAmount(paidAmount);
        installment.setNote(note);
        installment.setPaymentDate(paymentDate);

        payment.getInstallments().add(installment);

        paymentsService.syncTotals(payment); // 🔁 Auto-update totals
        paymentsService.save(payment);

        ra.addFlashAttribute("message", "Installment added successfully");
        return "redirect:/payments/client/" + payment.getClient().getId();
    }

    // ✅ Delete an installment
    @GetMapping("/installment/delete/{installmentId}")
    public String deleteInstallment(@PathVariable Long installmentId, RedirectAttributes ra) {
        Optional<Payments> paymentOpt = paymentsService.deleteInstallmentAndSync(installmentId);
        if (paymentOpt.isEmpty()) {
            ra.addFlashAttribute("error", "Installment not found");
            return "redirect:/clients";
        }
        Payments payment = paymentOpt.get();
        ra.addFlashAttribute("message", "Installment deleted successfully");
        return "redirect:/payments/client/" + payment.getClient().getId();
    }

    // ✅ Edit an installment
    @PostMapping("/installment/edit")
    public String editInstallment(@RequestParam("installmentId") Long installmentId,
                                  @RequestParam("paidAmount") Long paidAmount,
                                  @RequestParam(value = "note", required = false) String note,
                                  @RequestParam(value = "paymentDate", required = false) LocalDate paymentDate,
                                  RedirectAttributes ra) {

        Optional<Payments> paymentOpt = paymentsService.updateInstallmentAndSync(installmentId, paidAmount, note, paymentDate);
        if (paymentOpt.isEmpty()) {
            ra.addFlashAttribute("error", "Installment not found");
            return "redirect:/clients";
        }
        Payments payment = paymentOpt.get();
        ra.addFlashAttribute("message", "Installment updated successfully");
        return "redirect:/payments/client/" + payment.getClient().getId();
    }

    // ✅ Show all pending payments (remainingAmount > 0)
    @GetMapping("/pending")
    public String listPendingPayments(Model model) {
        List<Payments> allPayments = paymentsService.findAll();

        // Filter pending payments (remainingAmount > 0)
        List<Payments> pendingPayments = allPayments.stream()
                .filter(p -> p.getRemainingAmount() != null && p.getRemainingAmount() > 0)
                .sorted(Comparator.comparing(Payments::getDate).reversed())
                .toList();

        // Calculate totals
        long totalAmount = pendingPayments.stream()
                .mapToLong(p -> p.getTotalAmount() != null ? p.getTotalAmount() : 0)
                .sum();

        long totalPaid = pendingPayments.stream()
                .mapToLong(p -> p.getPaidAmount() != null ? p.getPaidAmount() : 0)
                .sum();

        long totalRemaining = pendingPayments.stream()
                .mapToLong(p -> p.getRemainingAmount() != null ? p.getRemainingAmount() : 0)
                .sum();

        model.addAttribute("payments", pendingPayments);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("totalRemaining", totalRemaining);

        return "payments/pending";
    }

    // ✅ Show orders due for return within 7 days and all overdue orders
    @GetMapping("/due-returns")
    public String listDueReturns(Model model) {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysLater = today.plusDays(7);

        List<Payments> allPayments = paymentsService.findAll();

        // Filter overdue orders and orders due within 7 days (not yet picked up)
        List<Payments> dueReturns = allPayments.stream()
                .filter(p -> p.getReturnDate() != null)
                .filter(p -> !"PICKED_UP".equals(p.getReadyStatus()))
                .filter(p -> {
                    LocalDate returnDate = p.getReturnDate();
                    // Include both overdue (< today) and upcoming 7 days (<= sevenDaysLater)
                    return !returnDate.isAfter(sevenDaysLater);
                })
                .sorted(Comparator.comparing(Payments::getReturnDate))
                .toList();

        model.addAttribute("payments", dueReturns);
        return "payments/due-returns";
    }

    // ✅ WhatsApp Notification Endpoints

    /**
     * Update ready status for an order
     */
    @PostMapping("/update-ready-status")
    public String updateReadyStatus(@RequestParam("paymentId") Long paymentId,
                                    @RequestParam("readyStatus") String readyStatus,
                                    RedirectAttributes ra) {
        Optional<Payments> paymentOpt = paymentsService.updateReadyStatus(paymentId, readyStatus);
        if (paymentOpt.isEmpty()) {
            ra.addFlashAttribute("error", "Payment not found");
            return "redirect:/clients";
        }

        ra.addFlashAttribute("message", "Ready status updated to: " + readyStatus);
        return "redirect:/payments/client/" + paymentOpt.get().getClient().getId();
    }

    /**
     * Generate WhatsApp click-to-chat link and mark as notified
     */
    @GetMapping("/whatsapp/ready/{paymentId}")
    public String sendWhatsAppNotification(@PathVariable Long paymentId, RedirectAttributes ra) {
        Optional<Payments> paymentOpt = paymentsService.findById(paymentId);
        if (paymentOpt.isEmpty()) {
            ra.addFlashAttribute("error", "Payment not found");
            return "redirect:/clients";
        }

        Payments payment = paymentOpt.get();
        Client client = payment.getClient();

        // Generate WhatsApp link
        String whatsappLink = whatsAppService.generateReadyForPickupLink(client, payment);

        // Mark as notified
        paymentsService.markAsNotified(paymentId);

        // Redirect to WhatsApp link
        return "redirect:" + whatsappLink;
    }

    /**
     * Generate reminder WhatsApp link
     */
    @GetMapping("/whatsapp/reminder/{paymentId}")
    public String sendWhatsAppReminder(@PathVariable Long paymentId, RedirectAttributes ra) {
        Optional<Payments> paymentOpt = paymentsService.findById(paymentId);
        if (paymentOpt.isEmpty()) {
            ra.addFlashAttribute("error", "Payment not found");
            return "redirect:/clients";
        }

        Payments payment = paymentOpt.get();
        Client client = payment.getClient();

        // Generate WhatsApp reminder link
        String whatsappLink = whatsAppService.generateReminderLink(client, payment);

        // Redirect to WhatsApp link
        return "redirect:" + whatsappLink;
    }
}
