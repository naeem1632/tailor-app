package com.example.tailorapp.controller;

import com.example.tailorapp.model.Client;
import com.example.tailorapp.model.PaymentInstallment;
import com.example.tailorapp.model.Payments;
import com.example.tailorapp.model.PriceSettings;
import com.example.tailorapp.model.WorkAssignment;
import com.example.tailorapp.service.ClientService;
import com.example.tailorapp.service.PaymentsService;
import com.example.tailorapp.service.PriceSettingsService;
import com.example.tailorapp.service.WhatsAppService;
import com.example.tailorapp.service.WorkAssignmentService;
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
    private final WorkAssignmentService workAssignmentService;

    public PaymentsController(PaymentsService paymentsService, ClientService clientService,
                              WhatsAppService whatsAppService, PriceSettingsService priceSettingsService,
                              WorkAssignmentService workAssignmentService) {
        this.paymentsService = paymentsService;
        this.clientService = clientService;
        this.whatsAppService = whatsAppService;
        this.priceSettingsService = priceSettingsService;
        this.workAssignmentService = workAssignmentService;
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

        // Check if there are any work assignments related to this payment
        List<WorkAssignment> assignments = workAssignmentService.findByPayment(p.get());
        if (!assignments.isEmpty()) {
            // Build details message
            StringBuilder details = new StringBuilder();
            details.append("This order has ").append(assignments.size()).append(" work assignment(s):<br>");
            for (WorkAssignment wa : assignments) {
                details.append("• ").append(wa.getEmployee().getName())
                       .append(" - ").append(wa.getWorkType())
                       .append(" (").append(wa.getItemType()).append(") - ")
                       .append(wa.getStatus()).append("<br>");
            }
            details.append("<br>Please delete or reassign these work assignments first.");

            ra.addFlashAttribute("error", details.toString());
            return "redirect:/payments/client/" + clientId;
        }

        paymentsService.delete(id);
        ra.addFlashAttribute("message", "Payment deleted successfully");
        return "redirect:/payments/client/" + clientId;
    }

    // ✅ Add a payment installment
    @PostMapping("/installment/add")
    public String addInstallment(@RequestParam("paymentId") Long paymentId,
                                 @RequestParam("paidAmount") Long paidAmount,
                                 @RequestParam(value = "paymentType", required = false) String paymentType,
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
        installment.setPaymentType(paymentType);
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
                                  @RequestParam(value = "paymentType", required = false) String paymentType,
                                  @RequestParam(value = "note", required = false) String note,
                                  @RequestParam(value = "paymentDate", required = false) LocalDate paymentDate,
                                  RedirectAttributes ra) {

        Optional<Payments> paymentOpt = paymentsService.updateInstallmentAndSync(installmentId, paidAmount, paymentType, note, paymentDate);
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

    // ✅ Show orders due for return based on date range filter and view option
    @GetMapping("/due-returns")
    public String listDueReturns(@RequestParam(value = "fromDate", required = false) LocalDate fromDate,
                                  @RequestParam(value = "toDate", required = false) LocalDate toDate,
                                  @RequestParam(value = "view", required = false, defaultValue = "upcoming") String view,
                                  @RequestParam(value = "readyStatusFilter", required = false, defaultValue = "all") String readyStatusFilter,
                                  Model model) {
        LocalDate today = LocalDate.now();

        // Set date range based on view option
        // View filter takes precedence - if dates are provided, they work within the view's context
        if ("overdue".equals(view)) {
            // Overdue only: Apply overdue filter regardless of custom dates
            if (fromDate == null) {
                fromDate = LocalDate.of(2020, 1, 1);
            }
            // For overdue, toDate must be at most yesterday
            if (toDate == null || toDate.isAfter(today.minusDays(1))) {
                toDate = today.minusDays(1);
            }
        } else if ("all".equals(view)) {
            // All orders: Allow full range
            if (fromDate == null) {
                fromDate = LocalDate.of(2020, 1, 1);
            }
            if (toDate == null) {
                toDate = today.plusYears(1);
            }
        } else {
            // Default "upcoming": Apply upcoming filter
            if (fromDate == null || fromDate.isBefore(today)) {
                fromDate = today;
            }
            if (toDate == null) {
                toDate = today.plusDays(7);
            }
        }

        List<Payments> allPayments = paymentsService.findAll();

        // Filter orders based on date range (not yet picked up)
        final LocalDate finalFromDate = fromDate;
        final LocalDate finalToDate = toDate;

        List<Payments> dueReturns = allPayments.stream()
                .filter(p -> p.getReturnDate() != null)
                .filter(p -> !"PICKED_UP".equals(p.getReadyStatus()))
                .filter(p -> {
                    // Filter by ready status
                    if ("ready".equals(readyStatusFilter)) {
                        return "READY".equals(p.getReadyStatus()) || "NOTIFIED".equals(p.getReadyStatus());
                    } else if ("notready".equals(readyStatusFilter)) {
                        return p.getReadyStatus() == null || p.getReadyStatus().isEmpty() || "NOT_READY".equals(p.getReadyStatus());
                    }
                    // "all" - no filtering by ready status
                    return true;
                })
                .filter(p -> {
                    // Filter by date range
                    LocalDate returnDate = p.getReturnDate();

                    if (finalFromDate != null && finalToDate != null) {
                        // Both dates provided: filter between fromDate and toDate (inclusive)
                        return !returnDate.isBefore(finalFromDate) && !returnDate.isAfter(finalToDate);
                    } else if (finalFromDate != null) {
                        // Only fromDate provided: filter from that date onwards
                        return !returnDate.isBefore(finalFromDate);
                    } else if (finalToDate != null) {
                        // Only toDate provided: filter up to that date
                        return !returnDate.isAfter(finalToDate);
                    }

                    return true;
                })
                .sorted(Comparator.comparing(Payments::getReturnDate))
                .toList();

        // Calculate total dress count
        long totalDressCount = dueReturns.stream()
                .mapToLong(p -> p.getDressCount() != null ? p.getDressCount() : 0)
                .sum();

        // Calculate total waistcoat count
        long totalWaistcoatCount = dueReturns.stream()
                .mapToLong(p -> p.getWaistcoatCount() != null ? p.getWaistcoatCount() : 0)
                .sum();

        // Calculate total shirt count
        long totalShirtCount = dueReturns.stream()
                .mapToLong(p -> p.getShirtCount() != null ? p.getShirtCount() : 0)
                .sum();

        // Calculate overdue vs upcoming breakdown
        // Note: These counts are already within the filtered date range (dueReturns list)
        // Overdue: return date is before today
        long overdueDressCount = dueReturns.stream()
                .filter(p -> p.getReturnDate().isBefore(today))
                .mapToLong(p -> p.getDressCount() != null ? p.getDressCount() : 0)
                .sum();

        long overdueWaistcoatCount = dueReturns.stream()
                .filter(p -> p.getReturnDate().isBefore(today))
                .mapToLong(p -> p.getWaistcoatCount() != null ? p.getWaistcoatCount() : 0)
                .sum();

        long overdueShirtCount = dueReturns.stream()
                .filter(p -> p.getReturnDate().isBefore(today))
                .mapToLong(p -> p.getShirtCount() != null ? p.getShirtCount() : 0)
                .sum();

        // Upcoming: return date is today or later
        long upcomingDressCount = dueReturns.stream()
                .filter(p -> !p.getReturnDate().isBefore(today)) // >= today
                .mapToLong(p -> p.getDressCount() != null ? p.getDressCount() : 0)
                .sum();

        long upcomingWaistcoatCount = dueReturns.stream()
                .filter(p -> !p.getReturnDate().isBefore(today)) // >= today
                .mapToLong(p -> p.getWaistcoatCount() != null ? p.getWaistcoatCount() : 0)
                .sum();

        long upcomingShirtCount = dueReturns.stream()
                .filter(p -> !p.getReturnDate().isBefore(today)) // >= today
                .mapToLong(p -> p.getShirtCount() != null ? p.getShirtCount() : 0)
                .sum();

        model.addAttribute("payments", dueReturns);
        model.addAttribute("totalDressCount", totalDressCount);
        model.addAttribute("totalWaistcoatCount", totalWaistcoatCount);
        model.addAttribute("totalShirtCount", totalShirtCount);

        // Add overdue counts
        model.addAttribute("overdueDressCount", overdueDressCount);
        model.addAttribute("overdueWaistcoatCount", overdueWaistcoatCount);
        model.addAttribute("overdueShirtCount", overdueShirtCount);

        // Add upcoming counts
        model.addAttribute("upcomingDressCount", upcomingDressCount);
        model.addAttribute("upcomingWaistcoatCount", upcomingWaistcoatCount);
        model.addAttribute("upcomingShirtCount", upcomingShirtCount);

        // Add date filter values, view option, and ready status filter
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("view", view);
        model.addAttribute("readyStatusFilter", readyStatusFilter);
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
        Optional<Payments> paymentOpt = paymentsService.findById(paymentId);
        if (paymentOpt.isEmpty()) {
            ra.addFlashAttribute("error", "Payment not found");
            return "redirect:/clients";
        }

        Payments payment = paymentOpt.get();

        // Validate: If marking as READY, NOTIFIED, or PICKED_UP, check if required work is completed
        if (readyStatus != null && !readyStatus.isEmpty()) {
            List<WorkAssignment> assignments = workAssignmentService.findByPayment(payment);

            // Build a validation error message
            StringBuilder errorMsg = new StringBuilder();

            // Check if order has items that need work
            boolean hasItemsInOrder = false;

            // For DRESS items (Kameez, Shalwar, Pajama)
            if (payment.getDressCount() != null && payment.getDressCount() > 0) {
                hasItemsInOrder = true;
                String dressError = validateItemCompletion(assignments, payment.getDressCount(),
                    new String[]{"KAMEEZ", "SHALWAR", "PAJAMA", "DRESS"}, "Dress items");
                if (dressError != null) {
                    errorMsg.append(dressError);
                }
            }

            // For SHIRT
            if (payment.getShirtCount() != null && payment.getShirtCount() > 0) {
                hasItemsInOrder = true;
                String shirtError = validateItemCompletion(assignments, payment.getShirtCount(),
                    new String[]{"SHIRT"}, "Shirt");
                if (shirtError != null) {
                    errorMsg.append(shirtError);
                }
            }

            // For WAISTCOAT
            if (payment.getWaistcoatCount() != null && payment.getWaistcoatCount() > 0) {
                hasItemsInOrder = true;
                String waistcoatError = validateItemCompletion(assignments, payment.getWaistcoatCount(),
                    new String[]{"WAISTCOAT"}, "Waistcoat");
                if (waistcoatError != null) {
                    errorMsg.append(waistcoatError);
                }
            }

            // For TROUSER
            if (payment.getTrouserCount() != null && payment.getTrouserCount() > 0) {
                hasItemsInOrder = true;
                String trouserError = validateItemCompletion(assignments, payment.getTrouserCount(),
                    new String[]{"TROUSER"}, "Trouser");
                if (trouserError != null) {
                    errorMsg.append(trouserError);
                }
            }

            // If order has items but validation failed
            if (hasItemsInOrder && errorMsg.length() > 0) {
                ra.addFlashAttribute("error", "Cannot mark order as Ready:\n" + errorMsg.toString());
                return "redirect:/payments/client/" + payment.getClient().getId();
            }

            // Also check if there are any incomplete assignments (additional safety check)
            if (!assignments.isEmpty()) {
                List<WorkAssignment> incompleteAssignments = assignments.stream()
                    .filter(a -> !"COMPLETED".equals(a.getStatus()))
                    .toList();

                if (!incompleteAssignments.isEmpty()) {
                    StringBuilder incompleteMsg = new StringBuilder("Cannot mark order as Ready. The following work is still pending:\n");
                    for (WorkAssignment assignment : incompleteAssignments) {
                        incompleteMsg.append("• ")
                               .append(assignment.getEmployee().getName())
                               .append(" - ")
                               .append(assignment.getWorkType())
                               .append(" (")
                               .append(assignment.getItemType())
                               .append(") - Status: ")
                               .append(assignment.getStatus())
                               .append("\n");
                    }
                    incompleteMsg.append("Please complete all work assignments before marking the order as Ready.");

                    ra.addFlashAttribute("error", incompleteMsg.toString());
                    return "redirect:/payments/client/" + payment.getClient().getId();
                }
            }
        }

        // If validation passes or clearing ready status, proceed with update
        paymentOpt = paymentsService.updateReadyStatus(paymentId, readyStatus);
        if (paymentOpt.isEmpty()) {
            ra.addFlashAttribute("error", "Failed to update ready status");
            return "redirect:/clients";
        }

        String statusMessage = (readyStatus == null || readyStatus.isEmpty())
            ? "Ready status cleared"
            : "Ready status updated to: " + readyStatus;
        ra.addFlashAttribute("message", statusMessage);
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

    /**
     * Validate that required work (cutting + stitching) is completed for item types
     *
     * @param assignments All work assignments for the order
     * @param requiredQuantity Required quantity from order (e.g., shirtCount)
     * @param itemTypes Item types to check (e.g., ["SHIRT"] or ["KAMEEZ", "SHALWAR", "PAJAMA"])
     * @param itemLabel Label for error messages (e.g., "Shirt" or "Dress items")
     * @return Error message if validation fails, null if passes
     */
    private String validateItemCompletion(List<WorkAssignment> assignments, Long requiredQuantity,
                                          String[] itemTypes, String itemLabel) {
        // Calculate completed cutting for these item types
        long completedCutting = assignments.stream()
            .filter(a -> "CUTTING".equals(a.getWorkType()))
            .filter(a -> "COMPLETED".equals(a.getStatus()))
            .filter(a -> containsItemType(itemTypes, a.getItemType()))
            .mapToLong(WorkAssignment::getAssignedCount)
            .sum();

        // Calculate completed stitching (plain OR design) for these item types
        long completedStitchingPlain = assignments.stream()
            .filter(a -> "STITCHING".equals(a.getWorkType()))
            .filter(a -> "COMPLETED".equals(a.getStatus()))
            .filter(a -> a.getIsDesignWork() == null || !a.getIsDesignWork()) // Plain stitching
            .filter(a -> containsItemType(itemTypes, a.getItemType()))
            .mapToLong(WorkAssignment::getAssignedCount)
            .sum();

        long completedStitchingDesign = assignments.stream()
            .filter(a -> "STITCHING".equals(a.getWorkType()))
            .filter(a -> "COMPLETED".equals(a.getStatus()))
            .filter(a -> a.getIsDesignWork() != null && a.getIsDesignWork()) // Design stitching
            .filter(a -> containsItemType(itemTypes, a.getItemType()))
            .mapToLong(WorkAssignment::getAssignedCount)
            .sum();

        // Total stitching = plain + design
        long completedStitching = completedStitchingPlain + completedStitchingDesign;

        // Validation: BOTH cutting AND stitching must be >= required quantity
        boolean cuttingComplete = completedCutting >= requiredQuantity;
        boolean stitchingComplete = completedStitching >= requiredQuantity;

        if (!cuttingComplete || !stitchingComplete) {
            StringBuilder error = new StringBuilder();
            error.append("• ").append(itemLabel).append(" (").append(requiredQuantity).append(" in order):\n");

            if (!cuttingComplete) {
                error.append("  - Cutting: ").append(completedCutting).append("/").append(requiredQuantity)
                     .append(" completed (").append(requiredQuantity - completedCutting).append(" remaining)\n");
            }

            if (!stitchingComplete) {
                error.append("  - Stitching: ").append(completedStitching).append("/").append(requiredQuantity)
                     .append(" completed (").append(requiredQuantity - completedStitching).append(" remaining)\n");
                if (completedStitchingPlain > 0 || completedStitchingDesign > 0) {
                    error.append("    (Plain: ").append(completedStitchingPlain)
                         .append(", Design: ").append(completedStitchingDesign).append(")\n");
                }
            }

            return error.toString();
        }

        return null; // Validation passed
    }

    /**
     * Check if an item type matches any of the allowed types
     */
    private boolean containsItemType(String[] allowedTypes, String itemType) {
        if (itemType == null) return false;
        for (String allowed : allowedTypes) {
            if (allowed.equalsIgnoreCase(itemType)) {
                return true;
            }
        }
        return false;
    }
}
