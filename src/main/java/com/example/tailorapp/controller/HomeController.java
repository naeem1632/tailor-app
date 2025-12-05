package com.example.tailorapp.controller;

import com.example.tailorapp.service.ClientService;
import com.example.tailorapp.service.PaymentsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ClientService clientService;
    private final PaymentsService paymentsService;

    public HomeController(ClientService clientService, PaymentsService paymentsService) {
        this.clientService = clientService;
        this.paymentsService = paymentsService;
    }

    @GetMapping("/")
    public String home(Model model) {
        // Dashboard metrics
        model.addAttribute("totalClients", clientService.count());
        model.addAttribute("pendingAmount", paymentsService.sumPendingPayments());
        model.addAttribute("dueReturns", paymentsService.countDueReturns());

        return "index";
    }
}
