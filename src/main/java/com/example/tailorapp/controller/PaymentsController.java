package com.example.tailorapp.controller;

import com.example.tailorapp.model.Client;
import com.example.tailorapp.model.Payments;
import com.example.tailorapp.service.ClientService;
import com.example.tailorapp.service.PaymentsService;
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

    public PaymentsController(PaymentsService paymentsService, ClientService clientService) {
        this.paymentsService = paymentsService;
        this.clientService = clientService;
    }

    @GetMapping("/client/{clientId}")
    public String listPayments(@PathVariable Long clientId, Model model) {
        Optional<Client> clientOpt = clientService.findById(clientId);
        if (clientOpt.isEmpty()) return "redirect:/clients";

        List<Payments> payments = paymentsService.findByClient(clientId);
        payments.sort(Comparator.comparing(Payments::getDate).reversed());

        Payments payment = new Payments();
        payment.setClient(clientOpt.get());
        payment.setDate(LocalDate.now());

        model.addAttribute("client", clientOpt.get());
        model.addAttribute("payments", payments);
        model.addAttribute("payment", payment);
        return "payments/list";
    }


    // Save payment
    @PostMapping("/save")
    public String savePayment(@ModelAttribute Payments payment, RedirectAttributes ra) {
        paymentsService.save(payment);
        ra.addFlashAttribute("message", "Payment saved successfully");
        return "redirect:/payments/client/" + payment.getClient().getId();
    }


    // Delete payment
    @GetMapping("/delete/{id}")
    public String deletePayment(@PathVariable Long id, RedirectAttributes ra) {
        Optional<Payments> p = paymentsService.findById(id);
        if (p.isEmpty()) return "redirect:/clients";

        Long clientId = p.get().getClient().getId();
        paymentsService.delete(id);
        ra.addFlashAttribute("message", "Payment deleted successfully");
        return "redirect:/payments/client/" + clientId;
    }
}
