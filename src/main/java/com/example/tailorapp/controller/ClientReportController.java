package com.example.tailorapp.controller;

import com.example.tailorapp.model.Client;
import com.example.tailorapp.model.Payments;
import com.example.tailorapp.service.ClientService;
import com.example.tailorapp.service.PaymentsService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

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

        Document document = new Document(PageSize.A4, 25, 25, 40, 25);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        // 🎨 Fonts
        Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.DARK_GRAY);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
        Font boldCellFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);

        // 🖼️ Header section (with logo support)
        addHeaderSection(document, subTitleFont, startDate, endDate);

        // 🧾 Table setup
        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100);
        // Wider Date, smaller Count fields
        table.setWidths(new float[]{14, 26, 7, 7, 10, 10, 10, 8, 8});

        addHeaderCell(table, "Date", headerFont);
        addHeaderCell(table, "Client (ID - Name)", headerFont);
        addHeaderCell(table, "Dress Count", headerFont);
        addHeaderCell(table, "Waistcoat Count", headerFont);
        addHeaderCell(table, "Dress Amt", headerFont);
        addHeaderCell(table, "Waistcoat Amt", headerFont);
        addHeaderCell(table, "Total Amt", headerFont);
        addHeaderCell(table, "Paid", headerFont);
        addHeaderCell(table, "Remaining", headerFont);

        List<Client> clients = clientService.findAll();

        long grandDressCount = 0, grandWaistcoatCount = 0;
        long grandDressAmount = 0, grandWaistcoatAmount = 0;
        long grandTotal = 0, grandPaid = 0, grandRemain = 0;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

        // 🔹 Sort all payments by date ascending
        for (Client client : clients) {
            List<Payments> clientPayments = paymentsService.findByClient(client.getId()).stream()
                    .filter(p -> p.getDate() != null &&
                            !p.getDate().isBefore(startDate) &&
                            !p.getDate().isAfter(endDate))
                    .sorted(Comparator.comparing(Payments::getDate)) // ascending order
                    .toList();

            if (clientPayments.isEmpty()) continue;

            for (Payments p : clientPayments) {
                LocalDate reportDate = p.getDate();

                long dressCount = p.getDressCount() != null ? p.getDressCount() : 0;
                long waistcoatCount = p.getWaistcoatCount() != null ? p.getWaistcoatCount() : 0;
                long dressAmount = (p.getDressRate() != null ? p.getDressRate() : 0) * dressCount;
                long waistcoatAmount = (p.getWaistcoatRate() != null ? p.getWaistcoatRate() : 0) * waistcoatCount;
                long totalAmount = dressAmount + waistcoatAmount;
                long paidAmount = p.getPaidAmount() != null ? p.getPaidAmount() : 0;
                long remainingAmount = totalAmount - paidAmount;

                addCellCenter(table, reportDate.format(formatter), cellFont);
                addLeftAlignedCell(table, client.getId() + " - " + client.getName(), boldCellFont);
                addCellCenter(table, String.valueOf(dressCount), cellFont);
                addCellCenter(table, String.valueOf(waistcoatCount), cellFont);
                addCellRight(table, String.valueOf(dressAmount), cellFont);
                addCellRight(table, String.valueOf(waistcoatAmount), cellFont);
                addCellRight(table, String.valueOf(totalAmount), cellFont);
                addCellRight(table, String.valueOf(paidAmount), cellFont);
                addCellRight(table, String.valueOf(remainingAmount), cellFont);

                grandDressCount += dressCount;
                grandWaistcoatCount += waistcoatCount;
                grandDressAmount += dressAmount;
                grandWaistcoatAmount += waistcoatAmount;
                grandTotal += totalAmount;
                grandPaid += paidAmount;
                grandRemain += remainingAmount;
            }
        }

        // ✅ Summary Row
        PdfPCell summaryCell = new PdfPCell(new Phrase("TOTAL", headerFont));
        summaryCell.setBackgroundColor(Color.DARK_GRAY);
        summaryCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        summaryCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        summaryCell.setColspan(2);
        summaryCell.setPadding(5f);
        summaryCell.setPhrase(new Phrase("TOTAL", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
        table.addCell(summaryCell);

        addSummaryCell(table, grandDressCount);
        addSummaryCell(table, grandWaistcoatCount);
        addSummaryCell(table, grandDressAmount);
        addSummaryCell(table, grandWaistcoatAmount);
        addSummaryCell(table, grandTotal);
        addSummaryCell(table, grandPaid);
        addSummaryCell(table, grandRemain);

        document.add(table);
        document.close();
    }

    private void addHeaderSection(Document document, Font subTitleFont,
                                  LocalDate startDate, LocalDate endDate) throws Exception {

        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);

        Font companyFont = FontFactory.getFont(FontFactory.HELVETICA_BOLDOBLIQUE, 24, new Color(0, 102, 204));
        Paragraph companyName = new Paragraph("STITCH & STYLE", companyFont);
        companyName.setAlignment(Element.ALIGN_CENTER);

        Font reportFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.DARK_GRAY);
        Paragraph reportTitle = new Paragraph("Client Payment Report", reportFont);
        reportTitle.setSpacingBefore(5);
        reportTitle.setAlignment(Element.ALIGN_CENTER);

        String rangeText = String.format("Period: %s to %s",
                startDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")),
                endDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")));
        Paragraph dateRange = new Paragraph(rangeText, subTitleFont);
        dateRange.setSpacingBefore(3);
        dateRange.setAlignment(Element.ALIGN_CENTER);

        PdfPCell textCell = new PdfPCell();
        textCell.addElement(companyName);
        textCell.addElement(reportTitle);
        textCell.addElement(dateRange);
        textCell.setBorder(Rectangle.NO_BORDER);
        textCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        header.addCell(textCell);
        document.add(header);

        document.add(new Paragraph("\n"));
        LineSeparator separator = new LineSeparator();
        separator.setLineWidth(1f);
        document.add(separator);
        document.add(new Paragraph("\n"));
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5f);
        table.addCell(cell);
    }

    private void addCellCenter(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(4f);
        table.addCell(cell);
    }

    private void addLeftAlignedCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(4f);
        table.addCell(cell);
    }

    private void addCellRight(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setPadding(4f);
        table.addCell(cell);
    }

    private void addSummaryCell(PdfPTable table, long value) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(value), font));
        cell.setBackgroundColor(Color.DARK_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5f);
        table.addCell(cell);
    }
}
