package com.example.tailorapp.controller;

import com.example.tailorapp.service.ClientService;
import com.example.tailorapp.service.PaymentsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class HomeController {

    private final ClientService clientService;
    private final PaymentsService paymentsService;

    public HomeController(ClientService clientService, PaymentsService paymentsService) {
        this.clientService = clientService;
        this.paymentsService = paymentsService;
    }

    @GetMapping({"/", "/index"})
    public String home(Model model) {
        // Dashboard metrics
        model.addAttribute("totalClients", clientService.count());
        model.addAttribute("pendingAmount", paymentsService.countPendingPayments());
        model.addAttribute("dueReturns", paymentsService.countDueReturns());
        model.addAttribute("readyOrders", paymentsService.countReadyOrders());
        model.addAttribute("completedOrders", paymentsService.countCompletedOrders());
        model.addAttribute("inProductionOrders", paymentsService.countInProductionOrders());
        model.addAttribute("totalDresses", paymentsService.countTotalDresses());
        model.addAttribute("totalWaistcoats", paymentsService.countTotalWaistcoats());
        model.addAttribute("totalShirts", paymentsService.countTotalShirts());
        model.addAttribute("inProductionDresses", paymentsService.countInProductionDresses());
        model.addAttribute("inProductionWaistcoats", paymentsService.countInProductionWaistcoats());
        model.addAttribute("inProductionShirts", paymentsService.countInProductionShirts());

        // Get order counts for last 15 days for chart
        Map<LocalDate, Long> orderCountsByDate = paymentsService.getOrderCountsByDate(15);

        // Prepare chart data (ensure all 15 days are present, even with 0 orders)
        List<String> chartLabels = new ArrayList<>();
        List<Long> chartData = new ArrayList<>();

        LocalDate endDate = LocalDate.now();
        for (int i = 14; i >= 0; i--) {
            LocalDate date = endDate.minusDays(i);
            chartLabels.add(date.format(DateTimeFormatter.ofPattern("MMM dd")));
            chartData.add(orderCountsByDate.getOrDefault(date, 0L));
        }

        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartData", chartData);

        return "index";
    }
}
