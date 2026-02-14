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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    // HTML report view
    @GetMapping("/report")
    public String viewReport(@RequestParam("startDate") LocalDate startDate,
                             @RequestParam("endDate") LocalDate endDate,
                             Model model) {

        List<Client> clients = clientService.findAll();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

        // Prepare report data with all calculations
        List<Map<String, Object>> reportData = new ArrayList<>();

        long grandDressCount = 0, grandWaistcoatCount = 0;
        long grandDressAmount = 0, grandWaistcoatAmount = 0;
        long grandMatelAmount = 0, grandTichAmount = 0, grandKantaAmount = 0, grandJaliAmount = 0, grandKrhaiAmount = 0;
        long grandTotal = 0, grandPaid = 0, grandRemain = 0;

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
                long dressAmount = (p.getDressRate() != null ? p.getDressRate() : 0) * dressCount;
                long waistcoatAmount = (p.getWaistcoatRate() != null ? p.getWaistcoatRate() : 0) * waistcoatCount;

                long matelAmount = 0;
                if (p.getMatelAmount() != null && p.getWithMatel() != null) {
                    matelAmount = p.getWithMatel() * p.getMatelAmount();
                }

                long tichAmount = 0;
                if (p.getTichAmount() != null && p.getWithTich() != null) {
                    tichAmount = p.getWithTich() * p.getTichAmount();
                }

                long kantaAmount = 0;
                if (p.getKantaAmount() != null && p.getWithKanta() != null) {
                    kantaAmount = p.getWithKanta() * p.getKantaAmount();
                }

                long jaliAmount = 0;
                if (p.getJaliAmount() != null && p.getWithJali() != null) {
                    jaliAmount = p.getWithJali() * p.getJaliAmount();
                }

                long krhaiAmount = 0;
                if (p.getKrhaiAmount() != null && p.getWithKrhai() != null) {
                    krhaiAmount = p.getWithKrhai() * p.getKrhaiAmount();
                }

                long totalAmount = dressAmount + waistcoatAmount + matelAmount + tichAmount + kantaAmount + jaliAmount + krhaiAmount;
                long paidAmount = p.getPaidAmount() != null ? p.getPaidAmount() : 0;
                long remainingAmount = totalAmount - paidAmount;

                Map<String, Object> row = new HashMap<>();
                row.put("date", p.getDate().format(formatter));
                row.put("clientId", client.getId());
                row.put("clientName", client.getName());
                row.put("mobile", client.getMobile() != null ? client.getMobile() : "-");
                row.put("dressCount", dressCount);
                row.put("waistcoatCount", waistcoatCount);
                row.put("dressAmount", dressAmount);
                row.put("waistcoatAmount", waistcoatAmount);
                row.put("matelAmount", matelAmount);
                row.put("tichAmount", tichAmount);
                row.put("kantaAmount", kantaAmount);
                row.put("jaliAmount", jaliAmount);
                row.put("krhaiAmount", krhaiAmount);
                row.put("totalAmount", totalAmount);
                row.put("paidAmount", paidAmount);
                row.put("remainingAmount", remainingAmount);
                reportData.add(row);

                grandDressCount += dressCount;
                grandWaistcoatCount += waistcoatCount;
                grandDressAmount += dressAmount;
                grandWaistcoatAmount += waistcoatAmount;
                grandMatelAmount += matelAmount;
                grandTichAmount += tichAmount;
                grandKantaAmount += kantaAmount;
                grandJaliAmount += jaliAmount;
                grandKrhaiAmount += krhaiAmount;
                grandTotal += totalAmount;
                grandPaid += paidAmount;
                grandRemain += remainingAmount;
            }
        }

        model.addAttribute("reportData", reportData);
        model.addAttribute("startDate", startDate.format(formatter));
        model.addAttribute("endDate", endDate.format(formatter));
        model.addAttribute("startDateParam", startDate.toString());
        model.addAttribute("endDateParam", endDate.toString());
        model.addAttribute("grandDressCount", grandDressCount);
        model.addAttribute("grandWaistcoatCount", grandWaistcoatCount);
        model.addAttribute("grandDressAmount", grandDressAmount);
        model.addAttribute("grandWaistcoatAmount", grandWaistcoatAmount);
        model.addAttribute("grandMatelAmount", grandMatelAmount);
        model.addAttribute("grandTichAmount", grandTichAmount);
        model.addAttribute("grandKantaAmount", grandKantaAmount);
        model.addAttribute("grandJaliAmount", grandJaliAmount);
        model.addAttribute("grandKrhaiAmount", grandKrhaiAmount);
        model.addAttribute("grandTotal", grandTotal);
        model.addAttribute("grandPaid", grandPaid);
        model.addAttribute("grandRemain", grandRemain);

        return "report/client-payment-report";
    }

    // PDF generation
    @GetMapping("/report/pdf")
    public void generateReportPdf(@RequestParam("startDate") LocalDate startDate,
                                  @RequestParam("endDate") LocalDate endDate,
                                  HttpServletResponse response) throws Exception {

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=client_report.pdf");

        Document document = new Document(PageSize.A4, 25, 25, 40, 25);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        // 🎨 Fonts
        Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.DARK_GRAY);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
        Font boldCellFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);

        // 🖼️ Header section (with logo support)
        addHeaderSection(document, subTitleFont, startDate, endDate);

        // 🧾 Table setup
        PdfPTable table = new PdfPTable(15);
        table.setWidthPercentage(100);
        // Optimized column widths to prevent header text wrapping
        table.setWidths(new float[]{7, 16, 9, 6, 6, 7, 7, 6, 6, 6, 6, 8, 6, 8});

        addHeaderCell(table, "Date", headerFont);
        addHeaderCell(table, "Name", headerFont);
        addHeaderCell(table, "Mobile#", headerFont);
        addHeaderCell(table, "Dress Qty", headerFont);
        addHeaderCell(table, "W/C Qty", headerFont);
        addHeaderCell(table, "Dress $", headerFont);
        addHeaderCell(table, "W/C $", headerFont);
        addHeaderCell(table, "Matel $", headerFont);
        addHeaderCell(table, "Tich $", headerFont);
        addHeaderCell(table, "Kanta $", headerFont);
        addHeaderCell(table, "Jali $", headerFont);
        addHeaderCell(table, "Krhai $", headerFont);
        addHeaderCell(table, "Total", headerFont);
        addHeaderCell(table, "Paid", headerFont);
        addHeaderCell(table, "Remaining", headerFont);

        List<Client> clients = clientService.findAll();

        long grandDressCount = 0, grandWaistcoatCount = 0;
        long grandDressAmount = 0, grandWaistcoatAmount = 0;
        long grandMatelAmount = 0, grandTichAmount = 0, grandKantaAmount = 0, grandJaliAmount = 0, grandKrhaiAmount = 0;
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

                // Calculate separate embellishment amounts
                long matelAmount = 0;
                if (p.getMatelAmount() != null && p.getWithMatel() != null) {
                    matelAmount = p.getWithMatel() * p.getMatelAmount();
                }

                long tichAmount = 0;
                if (p.getTichAmount() != null && p.getWithTich() != null) {
                    tichAmount = p.getWithTich() * p.getTichAmount();
                }

                long kantaAmount = 0;
                if (p.getKantaAmount() != null && p.getWithKanta() != null) {
                    kantaAmount = p.getWithKanta() * p.getKantaAmount();
                }

                long jaliAmount = 0;
                if (p.getJaliAmount() != null && p.getWithJali() != null) {
                    jaliAmount = p.getWithJali() * p.getJaliAmount();
                }

                long krhaiAmount = 0;
                if (p.getKrhaiAmount() != null && p.getWithKrhai() != null) {
                    krhaiAmount = p.getWithKrhai() * p.getKrhaiAmount();
                }

                long totalAmount = dressAmount + waistcoatAmount + matelAmount + tichAmount + kantaAmount + jaliAmount + krhaiAmount;
                long paidAmount = p.getPaidAmount() != null ? p.getPaidAmount() : 0;
                long remainingAmount = totalAmount - paidAmount;

                addCellCenter(table, reportDate.format(formatter), cellFont);
                addLeftAlignedCell(table, client.getId() + " - " + client.getName(), boldCellFont);
                addCellCenter(table, client.getMobile() != null ? client.getMobile() : "-", cellFont);
                addCellRight(table, String.valueOf(dressCount), cellFont);
                addCellRight(table, String.valueOf(waistcoatCount), cellFont);
                addCellRight(table, String.valueOf(dressAmount), cellFont);
                addCellRight(table, String.valueOf(waistcoatAmount), cellFont);
                addCellRight(table, String.valueOf(matelAmount), cellFont);
                addCellRight(table, String.valueOf(tichAmount), cellFont);
                addCellRight(table, String.valueOf(kantaAmount), cellFont);
                addCellRight(table, String.valueOf(jaliAmount), cellFont);
                addCellRight(table, String.valueOf(krhaiAmount), cellFont);
                addCellRight(table, String.valueOf(totalAmount), cellFont);
                addCellRight(table, String.valueOf(paidAmount), cellFont);
                addCellRight(table, String.valueOf(remainingAmount), cellFont);

                grandDressCount += dressCount;
                grandWaistcoatCount += waistcoatCount;
                grandDressAmount += dressAmount;
                grandWaistcoatAmount += waistcoatAmount;
                grandMatelAmount += matelAmount;
                grandTichAmount += tichAmount;
                grandKantaAmount += kantaAmount;
                grandJaliAmount += jaliAmount;
                grandKrhaiAmount += krhaiAmount;
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
        summaryCell.setColspan(3);
        summaryCell.setPadding(5f);
        summaryCell.setPhrase(new Phrase("TOTAL", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
        table.addCell(summaryCell);

        addSummaryCell(table, grandDressCount);
        addSummaryCell(table, grandWaistcoatCount);
        addSummaryCell(table, grandDressAmount);
        addSummaryCell(table, grandWaistcoatAmount);
        addSummaryCell(table, grandMatelAmount);
        addSummaryCell(table, grandTichAmount);
        addSummaryCell(table, grandKantaAmount);
        addSummaryCell(table, grandJaliAmount);
        addSummaryCell(table, grandKrhaiAmount);
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
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingTop(4f);
        cell.setPaddingBottom(4f);
        cell.setPaddingLeft(2f);
        cell.setPaddingRight(2f);
        cell.setMinimumHeight(16f);
        cell.setNoWrap(true);
        table.addCell(cell);
    }

    private void addCellCenter(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingTop(3f);
        cell.setPaddingBottom(3f);
        cell.setPaddingLeft(2f);
        cell.setPaddingRight(2f);
        table.addCell(cell);
    }

    private void addLeftAlignedCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingTop(3f);
        cell.setPaddingBottom(3f);
        cell.setPaddingLeft(2f);
        cell.setPaddingRight(2f);
        table.addCell(cell);
    }

    private void addCellRight(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingTop(3f);
        cell.setPaddingBottom(3f);
        cell.setPaddingLeft(2f);
        cell.setPaddingRight(2f);
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
