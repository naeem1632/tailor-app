package com.example.tailorapp.controller;

import com.example.tailorapp.model.Client;
import com.example.tailorapp.model.Expense;
import com.example.tailorapp.model.Payments;
import com.example.tailorapp.service.ClientService;
import com.example.tailorapp.service.ExpenseService;
import com.example.tailorapp.service.PaymentsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/profit-analysis")
public class ProfitAnalysisController {

    private final ClientService clientService;
    private final PaymentsService paymentsService;
    private final ExpenseService expenseService;

    public ProfitAnalysisController(ClientService clientService,
                                   PaymentsService paymentsService,
                                   ExpenseService expenseService) {
        this.clientService = clientService;
        this.paymentsService = paymentsService;
        this.expenseService = expenseService;
    }

    @GetMapping("/report")
    public String viewProfitAnalysis(@RequestParam("startDate") LocalDate startDate,
                                    @RequestParam("endDate") LocalDate endDate,
                                    Model model,
                                    jakarta.servlet.http.HttpSession session) {

        List<Client> clients = clientService.findAll();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        Expense expense = expenseService.getExpense();

        List<Map<String, Object>> reportData = new ArrayList<>();

        // Grand totals
        long grandTotalRevenue = 0, grandTotalExpense = 0, grandTotalProfit = 0;
        long grandDressRevenue = 0, grandDressExpense = 0, grandDressProfit = 0;
        long grandWaistcoatRevenue = 0, grandWaistcoatExpense = 0, grandWaistcoatProfit = 0;
        long grandShirtRevenue = 0, grandShirtExpense = 0, grandShirtProfit = 0;
        long grandMatelRevenue = 0, grandMatelExpense = 0, grandMatelProfit = 0;
        long grandTichRevenue = 0, grandTichExpense = 0, grandTichProfit = 0;
        long grandKantaRevenue = 0, grandKantaExpense = 0, grandKantaProfit = 0;
        long grandJaliRevenue = 0, grandJaliExpense = 0, grandJaliProfit = 0;
        long grandKrhaiRevenue = 0, grandKrhaiExpense = 0, grandKrhaiProfit = 0;

        for (Client client : clients) {
            List<Payments> clientPayments = paymentsService.findByClient(client.getId()).stream()
                    .filter(p -> p.getDate() != null &&
                            !p.getDate().isBefore(startDate) &&
                            !p.getDate().isAfter(endDate))
                    .sorted(Comparator.comparing(Payments::getDate))
                    .toList();

            for (Payments p : clientPayments) {
                long dressCount = p.getDressCount() != null ? p.getDressCount() : 0;
                long waistcoatCount = p.getWaistcoatCount() != null ? p.getWaistcoatCount() : 0;
                long shirtCount = p.getShirtCount() != null ? p.getShirtCount() : 0;

                // Calculate revenues (what customer paid)
                long dressRevenue = (p.getDressRate() != null ? p.getDressRate() : 0) * dressCount;
                long waistcoatRevenue = (p.getWaistcoatRate() != null ? p.getWaistcoatRate() : 0) * waistcoatCount;
                long shirtRevenue = (p.getShirtRate() != null ? p.getShirtRate() : 0) * shirtCount;

                // Calculate expenses (our costs)
                long dressExpenseTotal = (expense.getDressExpense() != null ? expense.getDressExpense() : 0) * dressCount;
                long waistcoatExpenseTotal = (expense.getWaistcoatExpense() != null ? expense.getWaistcoatExpense() : 0) * waistcoatCount;
                long shirtExpenseTotal = (expense.getShirtExpense() != null ? expense.getShirtExpense() : 0) * shirtCount;

                // Calculate profits
                long dressProfit = dressRevenue - dressExpenseTotal;
                long waistcoatProfit = waistcoatRevenue - waistcoatExpenseTotal;
                long shirtProfit = shirtRevenue - shirtExpenseTotal;

                // Embellishments
                long matelQty = p.getWithMatel() != null ? p.getWithMatel() : 0;
                long tichQty = p.getWithTich() != null ? p.getWithTich() : 0;
                long kantaQty = p.getWithKanta() != null ? p.getWithKanta() : 0;
                long jaliQty = p.getWithJali() != null ? p.getWithJali() : 0;
                long krhaiQty = p.getWithKrhai() != null ? p.getWithKrhai() : 0;

                long matelRevenue = (p.getMatelAmount() != null ? p.getMatelAmount() : 0) * matelQty;
                long tichRevenue = (p.getTichAmount() != null ? p.getTichAmount() : 0) * tichQty;
                long kantaRevenue = (p.getKantaAmount() != null ? p.getKantaAmount() : 0) * kantaQty;
                long jaliRevenue = (p.getJaliAmount() != null ? p.getJaliAmount() : 0) * jaliQty;
                long krhaiRevenue = (p.getKrhaiAmount() != null ? p.getKrhaiAmount() : 0) * krhaiQty;

                long matelExpenseTotal = (expense.getMatelExpense() != null ? expense.getMatelExpense() : 0) * matelQty;
                long tichExpenseTotal = (expense.getTichExpense() != null ? expense.getTichExpense() : 0) * tichQty;
                long kantaExpenseTotal = (expense.getKantaExpense() != null ? expense.getKantaExpense() : 0) * kantaQty;
                long jaliExpenseTotal = (expense.getJaliExpense() != null ? expense.getJaliExpense() : 0) * jaliQty;
                long krhaiExpenseTotal = (expense.getKrhaiExpense() != null ? expense.getKrhaiExpense() : 0) * krhaiQty;

                long matelProfit = matelRevenue - matelExpenseTotal;
                long tichProfit = tichRevenue - tichExpenseTotal;
                long kantaProfit = kantaRevenue - kantaExpenseTotal;
                long jaliProfit = jaliRevenue - jaliExpenseTotal;
                long krhaiProfit = krhaiRevenue - krhaiExpenseTotal;

                // Row totals
                long totalRevenue = dressRevenue + waistcoatRevenue + shirtRevenue + matelRevenue +
                                   tichRevenue + kantaRevenue + jaliRevenue + krhaiRevenue;
                long totalExpense = dressExpenseTotal + waistcoatExpenseTotal + shirtExpenseTotal +
                                   matelExpenseTotal + tichExpenseTotal + kantaExpenseTotal +
                                   jaliExpenseTotal + krhaiExpenseTotal;
                long totalProfit = totalRevenue - totalExpense;
                double profitPercentage = totalRevenue > 0 ? (totalProfit * 100.0 / totalRevenue) : 0;

                Map<String, Object> row = new HashMap<>();
                row.put("date", p.getDate().format(formatter));
                row.put("clientId", client.getId());
                row.put("clientName", client.getName());
                row.put("mobile", client.getMobile() != null ? client.getMobile() : "-");

                row.put("dressCount", dressCount);
                row.put("dressRevenue", dressRevenue);
                row.put("dressExpense", dressExpenseTotal);
                row.put("dressProfit", dressProfit);

                row.put("waistcoatCount", waistcoatCount);
                row.put("waistcoatRevenue", waistcoatRevenue);
                row.put("waistcoatExpense", waistcoatExpenseTotal);
                row.put("waistcoatProfit", waistcoatProfit);

                row.put("shirtCount", shirtCount);
                row.put("shirtRevenue", shirtRevenue);
                row.put("shirtExpense", shirtExpenseTotal);
                row.put("shirtProfit", shirtProfit);

                row.put("matelRevenue", matelRevenue);
                row.put("matelExpense", matelExpenseTotal);
                row.put("matelProfit", matelProfit);

                row.put("tichRevenue", tichRevenue);
                row.put("tichExpense", tichExpenseTotal);
                row.put("tichProfit", tichProfit);

                row.put("kantaRevenue", kantaRevenue);
                row.put("kantaExpense", kantaExpenseTotal);
                row.put("kantaProfit", kantaProfit);

                row.put("jaliRevenue", jaliRevenue);
                row.put("jaliExpense", jaliExpenseTotal);
                row.put("jaliProfit", jaliProfit);

                row.put("krhaiRevenue", krhaiRevenue);
                row.put("krhaiExpense", krhaiExpenseTotal);
                row.put("krhaiProfit", krhaiProfit);

                row.put("totalRevenue", totalRevenue);
                row.put("totalExpense", totalExpense);
                row.put("totalProfit", totalProfit);
                row.put("profitPercentage", String.format("%.1f", profitPercentage));

                reportData.add(row);

                // Accumulate grand totals
                grandTotalRevenue += totalRevenue;
                grandTotalExpense += totalExpense;
                grandTotalProfit += totalProfit;

                grandDressRevenue += dressRevenue;
                grandDressExpense += dressExpenseTotal;
                grandDressProfit += dressProfit;

                grandWaistcoatRevenue += waistcoatRevenue;
                grandWaistcoatExpense += waistcoatExpenseTotal;
                grandWaistcoatProfit += waistcoatProfit;

                grandShirtRevenue += shirtRevenue;
                grandShirtExpense += shirtExpenseTotal;
                grandShirtProfit += shirtProfit;

                grandMatelRevenue += matelRevenue;
                grandMatelExpense += matelExpenseTotal;
                grandMatelProfit += matelProfit;

                grandTichRevenue += tichRevenue;
                grandTichExpense += tichExpenseTotal;
                grandTichProfit += tichProfit;

                grandKantaRevenue += kantaRevenue;
                grandKantaExpense += kantaExpenseTotal;
                grandKantaProfit += kantaProfit;

                grandJaliRevenue += jaliRevenue;
                grandJaliExpense += jaliExpenseTotal;
                grandJaliProfit += jaliProfit;

                grandKrhaiRevenue += krhaiRevenue;
                grandKrhaiExpense += krhaiExpenseTotal;
                grandKrhaiProfit += krhaiProfit;
            }
        }

        double grandProfitPercentage = grandTotalRevenue > 0 ? (grandTotalProfit * 100.0 / grandTotalRevenue) : 0;

        model.addAttribute("reportData", reportData);
        model.addAttribute("startDate", startDate.format(formatter));
        model.addAttribute("endDate", endDate.format(formatter));
        model.addAttribute("startDateParam", startDate.toString());
        model.addAttribute("endDateParam", endDate.toString());

        model.addAttribute("grandTotalRevenue", grandTotalRevenue);
        model.addAttribute("grandTotalExpense", grandTotalExpense);
        model.addAttribute("grandTotalProfit", grandTotalProfit);
        model.addAttribute("grandProfitPercentage", String.format("%.1f", grandProfitPercentage));

        model.addAttribute("grandDressRevenue", grandDressRevenue);
        model.addAttribute("grandDressExpense", grandDressExpense);
        model.addAttribute("grandDressProfit", grandDressProfit);

        model.addAttribute("grandWaistcoatRevenue", grandWaistcoatRevenue);
        model.addAttribute("grandWaistcoatExpense", grandWaistcoatExpense);
        model.addAttribute("grandWaistcoatProfit", grandWaistcoatProfit);

        model.addAttribute("grandShirtRevenue", grandShirtRevenue);
        model.addAttribute("grandShirtExpense", grandShirtExpense);
        model.addAttribute("grandShirtProfit", grandShirtProfit);

        model.addAttribute("grandMatelRevenue", grandMatelRevenue);
        model.addAttribute("grandMatelExpense", grandMatelExpense);
        model.addAttribute("grandMatelProfit", grandMatelProfit);

        model.addAttribute("grandTichRevenue", grandTichRevenue);
        model.addAttribute("grandTichExpense", grandTichExpense);
        model.addAttribute("grandTichProfit", grandTichProfit);

        model.addAttribute("grandKantaRevenue", grandKantaRevenue);
        model.addAttribute("grandKantaExpense", grandKantaExpense);
        model.addAttribute("grandKantaProfit", grandKantaProfit);

        model.addAttribute("grandJaliRevenue", grandJaliRevenue);
        model.addAttribute("grandJaliExpense", grandJaliExpense);
        model.addAttribute("grandJaliProfit", grandJaliProfit);

        model.addAttribute("grandKrhaiRevenue", grandKrhaiRevenue);
        model.addAttribute("grandKrhaiExpense", grandKrhaiExpense);
        model.addAttribute("grandKrhaiProfit", grandKrhaiProfit);

        // Clear authentication to require password on next report access
        session.removeAttribute("reportAuthenticated");

        return "profit-analysis/report";
    }
}