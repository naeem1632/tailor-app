package com.example.tailorapp.controller;

import com.example.tailorapp.model.Client;
import com.example.tailorapp.model.Payments;
import com.example.tailorapp.service.ClientService;
import com.example.tailorapp.service.PaymentsService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/print")
public class ClientReportController {

    private final ClientService clientService;
    private final PaymentsService paymentsService;

    public ClientReportController(ClientService clientService, PaymentsService paymentsService) {
        this.clientService = clientService;
        this.paymentsService = paymentsService;
    }

    @GetMapping("/report")
    public void generateReport(@RequestParam("startDate") LocalDate startDate,
                               @RequestParam("endDate") LocalDate endDate,
                               HttpServletResponse response) throws Exception {

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=client_report.pdf");

        Document document = new Document(PageSize.A4.rotate(), 25, 25, 25, 25);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{8, 20, 10, 10, 12, 12, 12});

        addHeaderCell(table, "ID", headerFont);
        addHeaderCell(table, "Name", headerFont);
        addHeaderCell(table, "Dress Count", headerFont);
        addHeaderCell(table, "Waistcoat Count", headerFont);
        addHeaderCell(table, "Total Amount", headerFont);
        addHeaderCell(table, "Paid", headerFont);
        addHeaderCell(table, "Remaining", headerFont);

        List<Client> clients = clientService.findAll();

        long grandDress = 0, grandWaistcoat = 0, grandTotal = 0, grandPaid = 0, grandRemain = 0;

        for (Client client : clients) {
            List<Payments> clientPayments = paymentsService.findByClient(client.getId()).stream()
                    .filter(p -> p.getDate() != null &&
                            !p.getDate().isBefore(startDate) &&
                            !p.getDate().isAfter(endDate))
                    .toList();

            if (clientPayments.isEmpty()) continue;


            long totalDress = clientPayments.stream()
                    .mapToLong(p -> p.getDressCount() != null ? p.getDressCount() : 0).sum();
            long totalWaistcoat = clientPayments.stream()
                    .mapToLong(p -> p.getWaistcoatCount() != null ? p.getWaistcoatCount() : 0).sum();
            long totalAmount = clientPayments.stream()
                    .mapToLong(p -> p.getTotalAmount() != null ? p.getTotalAmount() : 0).sum();
            long totalPaid = clientPayments.stream()
                    .mapToLong(p -> p.getPaidAmount() != null ? p.getPaidAmount() : 0).sum();
            long totalRemaining = clientPayments.stream()
                    .mapToLong(p -> p.getRemainingAmount() != null ? p.getRemainingAmount() : 0).sum();

            addCell(table, client.getId().toString(), cellFont);
            addCell(table, client.getName(), cellFont);
            addCell(table, String.valueOf(totalDress), cellFont);
            addCell(table, String.valueOf(totalWaistcoat), cellFont);
            addCell(table, String.valueOf(totalAmount), cellFont);
            addCell(table, String.valueOf(totalPaid), cellFont);
            addCell(table, String.valueOf(totalRemaining), cellFont);

            grandDress += totalDress;
            grandWaistcoat += totalWaistcoat;
            grandTotal += totalAmount;
            grandPaid += totalPaid;
            grandRemain += totalRemaining;
        }

        // ✅ Summary row
        PdfPCell summaryCell = new PdfPCell(new Phrase("TOTAL", headerFont));
        summaryCell.setBackgroundColor(Color.DARK_GRAY);
        summaryCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        summaryCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        summaryCell.setColspan(2);
        summaryCell.setPadding(5f);
        summaryCell.setPhrase(new Phrase("TOTAL", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
        table.addCell(summaryCell);

        addSummaryCell(table, grandDress);
        addSummaryCell(table, grandWaistcoat);
        addSummaryCell(table, grandTotal);
        addSummaryCell(table, grandPaid);
        addSummaryCell(table, grandRemain);

        document.add(table);
        document.close();
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5f);
        table.addCell(cell);
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(4f);
        table.addCell(cell);
    }

    private void addSummaryCell(PdfPTable table, long value) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(value), font));
        cell.setBackgroundColor(Color.DARK_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5f);
        table.addCell(cell);
    }
}
