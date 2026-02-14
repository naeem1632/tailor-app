package com.example.tailorapp.controller;

import com.example.tailorapp.config.AppConstants;
import com.example.tailorapp.model.Client;
import com.example.tailorapp.model.DressMeasurement;
import com.example.tailorapp.model.Payments;
import com.example.tailorapp.model.WaistcoatMeasurement;
import com.example.tailorapp.service.ClientService;
import com.example.tailorapp.service.MeasurementService;
import com.example.tailorapp.service.PaymentsService;
import com.example.tailorapp.service.StorageProperties;
import com.example.tailorapp.service.WaistcoatService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Optional;

@Controller
@RequestMapping("/print")
@EnableConfigurationProperties(StorageProperties.class)
public class PrintController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(AppConstants.DATE_TIME_FORMAT);

    private final ClientService clientService;
    private final MeasurementService measurementService;
    private final StorageProperties storageProperties;
    private final WaistcoatService waistcoatService;
    private final PaymentsService paymentsService;

    public PrintController(ClientService clientService,
                           MeasurementService measurementService,
                           StorageProperties storageProperties,
                           WaistcoatService waistcoatService,
                           PaymentsService paymentsService) {
        this.clientService = clientService;
        this.measurementService = measurementService;
        this.storageProperties = storageProperties;
        this.waistcoatService = waistcoatService;
        this.paymentsService = paymentsService;
    }

    // Print PDF
    @GetMapping("/dress/{id}")
    public void printClientSlip(@PathVariable Long id, HttpServletResponse response) throws Exception {
        Optional<Client> c = clientService.findById(id);
        if (c.isEmpty()) return;

        Client client = c.get();

        Optional<DressMeasurement> latestMeasurement = measurementService.findByClient(id)
                .stream()
                .max(Comparator.comparing(DressMeasurement::getDate, Comparator.nullsLast(Comparator.naturalOrder())));

        if (latestMeasurement.isEmpty()) return;

        DressMeasurement dressMeasurement = latestMeasurement.get();

        // Get latest payment with return date (where status is not PICKED_UP and returnDate is not null)
        Optional<Payments> latestPaymentWithReturnDate = paymentsService.findByClient(id).stream()
                .filter(p -> p.getReturnDate() != null && !"PICKED_UP".equals(p.getReadyStatus()))
                .max(Comparator.comparing(Payments::getDate, Comparator.nullsLast(Comparator.naturalOrder())));

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=client_" + id + "_slip.pdf");

        Rectangle slipSize = new Rectangle(PageSize.A4.getWidth() / 2, PageSize.A4.getHeight() / 2);
        Document document = new Document(slipSize, 15, 15, 10, 15); // balanced margins for single page printing
        String now = LocalDateTime.now().format(DATE_TIME_FORMATTER);

        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
        writer.setPageEvent(new FooterHandler(
                dressMeasurement.getNotes(),
                latestPaymentWithReturnDate.map(Payments::getReturnDate).orElse(null)
        )); // pass notes and return date to footer
        document.open();

        // === Header: Name (center) and Print Date (right) on same line ===
        PdfPTable headerTable = new PdfPTable(3);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{30f, 40f, 30f});

        // Left: empty
        PdfPCell leftCell = new PdfPCell(new Phrase(""));
        leftCell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(leftCell);

        // Center: Name
        PdfPCell nameCell = new PdfPCell(new Phrase(nvl(client.getName() + " (" + client.getId() + ")"),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        nameCell.setBorder(Rectangle.NO_BORDER);
        nameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        nameCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(nameCell);

        // Right: Print date
        PdfPCell dateCell = new PdfPCell(new Phrase("Printed: " + now,
                FontFactory.getFont(FontFactory.HELVETICA, 6)));
        dateCell.setBorder(Rectangle.NO_BORDER);
        dateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        dateCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(dateCell);

        headerTable.setSpacingAfter(1f);
        document.add(headerTable);

        // === Header: Info Row (Dress Qty, Collar, Bain, Design, Pajama) ===
        PdfPTable infoTable = new PdfPTable(5);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{20f, 20f, 20f, 20f, 20f});
        infoTable.setSpacingAfter(1.5f);

        infoTable.addCell(makeInfoCell("Dress Qty: " + nvl(dressMeasurement.getDressQty())));
        infoTable.addCell(makeInfoCell("W/Collar: " + nvl(dressMeasurement.getWithCollar())));
        infoTable.addCell(makeInfoCell("W/Bain: " + nvl(dressMeasurement.getWithBain())));
        infoTable.addCell(makeInfoCell("W/Design: " + nvl(dressMeasurement.getWithDesign())));
        infoTable.addCell(makeInfoCell("W/Pajama: " + nvl(dressMeasurement.getWithPajama())));

        document.add(infoTable);

        // === Measurements ===
            addKameezSection(document, dressMeasurement);
            addShalwarSection(document, dressMeasurement);

            // Only add Pajama section if at least one pajama field has data
            if (hasPajamaData(dressMeasurement)) {
                addPajamaSection(document, dressMeasurement);
            }

            addDesignSection(document, dressMeasurement);

        document.close();
    }

    private PdfPCell makeInfoCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(nvl(text),
                FontFactory.getFont(FontFactory.HELVETICA, 7))); // reduced from 9 to 7
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(1f); // reduced from 1.5f
        return cell;
    }

    // === Kameez Section ===
    private void addKameezSection(Document doc, DressMeasurement m) throws DocumentException {
        PdfPTable table = createSectionTable("Kameez Measurements");

        addRow4IfNotNull(table, "Length", nvl(m.getKameezLength()), "Arm", nvl(m.getArm()));
        addRow4IfNotNull(table, "Shoulder-aram", nvl(m.getShoulderArm()), "Upper arm", nvl(m.getUpperArm()));
        addRow4IfNotNull(table, "Center aram", nvl(m.getCenterArm()),  "Lower arm", nvl(m.getLowerArm()));
        addRow4IfNotNull(table, "Cuff length", nvl(m.getCuffLength()), "Cuff width", nvl(m.getCuffWidth()));
        addRow4IfNotNull(table, "Terra", nvl(m.getTerra()), "Terra down", nvl(m.getTerraDown()));
        addRow4IfNotNull(table, "Collar size", nvl(m.getCollarSize()), "Bain size", nvl(m.getBainSize()));
        addRow4IfNotNull(table, "Chest", nvl(m.getChest()), "Chest fitting", nvl(m.getChestFitting()));
        addRow4IfNotNull(table, "Waist", nvl(m.getWaist()), "Hip", nvl(m.getHip()));
        doc.add(table);
        doc.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 2))); // reduced from 3
    }

    // === Shalwar Section ===
    private void addShalwarSection(Document doc, DressMeasurement m) throws DocumentException {
        PdfPTable table = createSectionTable("Shalwar Measurements");

        addRow4IfNotNull(table, "Length", nvl(m.getShalwarLength()),  "Fitting", nvl(m.getShalwarFitting()));
        addRow4IfNotNull(table,"Asan", nvl(m.getAsan()), "Payncha", nvl(m.getPayncha()));

        doc.add(table);
        doc.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 2))); // reduced from 3
    }

    // === Pajama Section ===
    private void addPajamaSection(Document doc, DressMeasurement m) throws DocumentException {
        PdfPTable table = createSectionTable("Pajama Measurements");

        addRow4IfNotNull(table, "Pajama Asan", nvl(m.getPajamaAsan()), "Pajama Length", nvl(m.getPajamaLength()));
        addRow4IfNotNull(table, "Upper Fitting", nvl(m.getUpperFitting()), "Middle Fitting", nvl(m.getMiddleFitting()));
        addRow4IfNotNull(table, "Lower Fitting", nvl(m.getLowerFitting()), "Pajama Pocket", nvl(m.getPajamaPocket()));

        doc.add(table);
        doc.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 2))); // reduced from 3
    }

    // === Design Section ===
    private void addDesignSection(Document doc, DressMeasurement m) throws DocumentException {
        PdfPTable table = createSectionTable("Design & Finishing");

        addRow4IfNotNull(table,"Collar design", nvl(m.getCollarType()), "", null);
        addImageRow4(table, "Bain design", m.getBainType(), "Cuff design", m.getCuffDesign());
        addImageRow4(table, "Front pocket", m.getFrontPocket() ? "Yes" : "No", "Front pocket design", m.getFrontPocketType());
        addRow4IfNotNull(table, "Side pocket", nvl(m.getSidePocket()), "Shalwar pocket", m.getShalwarPocket() ? "Yes" : "No");
        addRow4IfNotNull(table, "Daman type", m.getDamanType(), "Daman stitching", nvl(m.getDamanStitching()));
        addRow4IfNotNull(table,  "Cuff type", m.getCuffType(), "Stitching", nvl(m.getStitchType()));
        addRow4IfNotNull(table, "Button", nvl(m.getButtonType()), "Design stitch", (m.getDesignStitch() != null && m.getDesignStitch()) ? "Yes" : "No");
        addRow4IfNotNull(table, "Front patti design", nvl(m.getFrontPattiType()), "Front patti kaj", nvl(m.getFrontPattiKaj()));
        addRow4IfNotNull(table, "Kanta", (m.getKanta() != null && m.getKanta()) ? "Yes" : "No", "Jali", nvl(m.getJali()));

        doc.add(table);
        // No spacing after Design section since notes follow immediately
    }

    // === Helpers ===
    private PdfPTable createSectionTable(String title) throws DocumentException {
        // 4 columns with custom widths: label(30%), value(20%), label(30%), value(20%)
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{30f, 15f, 30f, 15f});

        PdfPCell headingCell = new PdfPCell(
                new Phrase(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, Color.WHITE))); // increased from 7 to 7.5
        headingCell.setBackgroundColor(Color.DARK_GRAY);
        headingCell.setColspan(4);
        headingCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headingCell.setPadding(2f); // increased from 1.5f to 2f
        table.addCell(headingCell);

        return table;
    }

    private void addRow4(PdfPTable table, String l1, String v1, String l2, String v2) {
        table.addCell(makeLabelCell(l1));
        table.addCell(makeValueCell(v1));
        table.addCell(makeLabelCell(l2));
        table.addCell(makeValueCell(v2));
    }

    private void addRow4IfNotNull(PdfPTable table, String l1, String v1, String l2, String v2) {
        boolean hasFirst = v1 != null && !v1.isEmpty();
        boolean hasSecond = v2 != null && !v2.isEmpty();

        if (!hasFirst && !hasSecond) return; // skip entire row if both are null/empty

        if (!hasFirst && hasSecond) {
            // shift second column left (no heading gap)
            table.addCell(makeLabelCell(l2));
            table.addCell(makeValueCell(v2));
            table.addCell(new PdfPCell()); // fill remaining empty cells
            table.addCell(new PdfPCell());
        } else if (hasFirst && !hasSecond) {
            table.addCell(makeLabelCell(l1));
            table.addCell(makeValueCell(v1));
            table.addCell(new PdfPCell());
            table.addCell(new PdfPCell());
        } else {
            addRow4(table, l1, v1, l2, v2);
        }
    }

    private void addImageRow4(PdfPTable table, String l1, String t1, String l2, String t2) {
        table.addCell(makeLabelCell(l1));
        table.addCell(getImageCell(t1));
        table.addCell(makeLabelCell(l2));
        table.addCell(getImageCell(t2));
    }

    private PdfPCell makeLabelCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f))); // increased from 7 to 7.5
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(3f); // increased from 2.5f
        return cell;
    }

    private PdfPCell makeValueCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(nvl(text),
                FontFactory.getFont(FontFactory.HELVETICA, 8.5f))); // increased from 8 to 8.5
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);   // center horizontally
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);     // center vertically
        cell.setPadding(2f); // increased from 1.5f
        return cell;
    }

    private PdfPCell getImageCell(String type) {
        PdfPCell cell = new PdfPCell();
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);   // center horizontally
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);     // center vertically
        cell.setPadding(1.5f);

        if (type == null) {
            cell.setPhrase(new Phrase(""));
            return cell;
        }

        try {
            String fileName = switch (type) {
                case "Round" -> "pocket_round.png";
                case "Cut" -> "pocket_cut.png";
                case "Square" -> "pocket_square.png";
                case "Round-Bain" -> "bain_round.png";
                case "Square-Bain" -> "bain_square.png";
                case "Cut-Bain" -> "bain_cut.png";
                case "Round-Cuff" -> "cuff_round.png";
                case "Square-Cuff" -> "cuff_square.png";
                case "Cut-Cuff" -> "cuff_cut.png";
                default -> null;
            };

            if (fileName != null) {
                String basePath = System.getProperty("user.dir") + "/build/resources/main/static/images/";
                Image img = Image.getInstance(basePath + fileName);
                img.scaleToFit(14, 14);
                img.setAlignment(Element.ALIGN_CENTER);       // ensure image itself is centered
                cell.addElement(img);
            } else {
                cell.setPhrase(new Phrase(type, FontFactory.getFont(FontFactory.HELVETICA, 6)));
            }
        } catch (Exception e) {
            cell.setPhrase(new Phrase(type, FontFactory.getFont(FontFactory.HELVETICA, 6)));
        }
        return cell;
    }

    private String nvl(Object obj) {
        return (obj == null) ? "" : obj.toString();
    }

    // Print PDF
    @GetMapping("/waistcoat/{id}")
    public void printWaistcoatSlip(@PathVariable Long id, HttpServletResponse response) throws Exception {
        Optional<Client> c = clientService.findById(id);
        if (c.isEmpty()) return;

        Client client = c.get();

        Optional<WaistcoatMeasurement> latestMeasurement = waistcoatService.findByClient(id)
                .stream()
                .max(Comparator.comparing(WaistcoatMeasurement::getDate, Comparator.nullsLast(Comparator.naturalOrder())));

        if (latestMeasurement.isEmpty()) return;

        WaistcoatMeasurement waistcoatMeasurements = latestMeasurement.get();

        // Get latest payment with return date (where status is not PICKED_UP and returnDate is not null)
        Optional<Payments> latestPaymentWithReturnDate = paymentsService.findByClient(id).stream()
                .filter(p -> p.getReturnDate() != null && !"PICKED_UP".equals(p.getReadyStatus()))
                .max(Comparator.comparing(Payments::getDate, Comparator.nullsLast(Comparator.naturalOrder())));

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=client_" + id + "_slip.pdf");

        Rectangle slipSize = new Rectangle(PageSize.A4.getWidth() / 2, PageSize.A4.getHeight() / 2);
        Document document = new Document(slipSize, 15, 15, 10, 15); // balanced margins for single page printing
        String now = LocalDateTime.now().format(DATE_TIME_FORMATTER);

        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
        writer.setPageEvent(new FooterHandler(
                waistcoatMeasurements.getNotes(),
                latestPaymentWithReturnDate.map(Payments::getReturnDate).orElse(null)
        )); // pass notes and return date to footer
        document.open();

        // === Header: Name (center) and Print Date (right) on same line ===
        PdfPTable headerTable = new PdfPTable(3);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{30f, 40f, 30f});

        // Left: empty
        PdfPCell leftCell = new PdfPCell(new Phrase(""));
        leftCell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(leftCell);

        // Center: Name
        PdfPCell nameCell = new PdfPCell(new Phrase(nvl(client.getName()),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        nameCell.setBorder(Rectangle.NO_BORDER);
        nameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        nameCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(nameCell);

        // Right: Print date
        PdfPCell dateCell = new PdfPCell(new Phrase("Printed: " + now,
                FontFactory.getFont(FontFactory.HELVETICA, 6)));
        dateCell.setBorder(Rectangle.NO_BORDER);
        dateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        dateCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(dateCell);

        headerTable.setSpacingAfter(1.5f);
        document.add(headerTable);

        // === Measurements ===
            addWaistcoatSection(document, waistcoatMeasurements);

        document.close();
    }

    // === waistcoat Section ===
    private void addWaistcoatSection(Document doc, WaistcoatMeasurement m) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{40f, 60f});

        PdfPCell headingCell = new PdfPCell(
                new Phrase("Waistcoat Measurements", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE)));
        headingCell.setBackgroundColor(Color.DARK_GRAY);
        headingCell.setColspan(2);
        headingCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headingCell.setPadding(3f);
        table.addCell(headingCell);

        addRow2IfNotNull(table, "Length", nvl(m.getLength()));
        addRow2IfNotNull(table, "Shoulder", nvl(m.getShoulder()));
        addRow2IfNotNull(table, "Neck", nvl(m.getNeck()));
        addRow2IfNotNull(table, "Chest", nvl(m.getChest()));
        addRow2IfNotNull(table, "Chest fitting", nvl(m.getChestFitting()));
        addRow2IfNotNull(table, "Hip", nvl(m.getHip()));
        addRow2IfNotNull(table, "Bain size", nvl(m.getBainSize()));
        addRow2IfNotNull(table, "Bain design", nvl(m.getBainType()));
        addRow2IfNotNull(table, "Daman design", nvl(m.getDamanType()));

        doc.add(table);
        doc.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 4)));
    }

    private void addRow2IfNotNull(PdfPTable table, String label, String value) {
        if (value == null || value.isEmpty()) return;
        table.addCell(makeLabelCell(label));
        table.addCell(makeValueCell(value));
    }

    // Helper method to check if any pajama field has data
    private boolean hasPajamaData(DressMeasurement m) {
        return (m.getPajamaAsan() != null && !m.getPajamaAsan().toString().trim().isEmpty()) ||
               (m.getPajamaLength() != null && !m.getPajamaLength().toString().trim().isEmpty()) ||
               (m.getUpperFitting() != null && !m.getUpperFitting().toString().trim().isEmpty()) ||
               (m.getMiddleFitting() != null && !m.getMiddleFitting().toString().trim().isEmpty()) ||
               (m.getLowerFitting() != null && !m.getLowerFitting().toString().trim().isEmpty()) ||
               (m.getPajamaPocket() != null && !m.getPajamaPocket().toString().trim().isEmpty());
    }

    // Print Payment Invoice - Groups orders by same date
    @GetMapping("/invoice/client/{clientId}/date/{orderDate}")
    public void printPaymentInvoice(@PathVariable Long clientId,
                                   @PathVariable String orderDate,
                                   HttpServletResponse response) throws Exception {
        Optional<Client> c = clientService.findById(clientId);
        if (c.isEmpty()) return;

        Client client = c.get();

        // Parse the order date
        java.time.LocalDate targetDate = java.time.LocalDate.parse(orderDate);

        // Get all payments for this client on this specific date
        java.util.List<Payments> paymentsOnDate = paymentsService.findByClient(clientId).stream()
                .filter(p -> p.getDate() != null && p.getDate().equals(targetDate))
                .sorted(Comparator.comparing(Payments::getId))
                .toList();

        if (paymentsOnDate.isEmpty()) return;

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=invoice_" + clientId + "_" + orderDate + ".pdf");

        Rectangle slipSize = new Rectangle(PageSize.A4.getWidth() / 2, PageSize.A4.getHeight() / 2);
        Document document = new Document(slipSize, 15, 15, 10, 10);
        String now = LocalDateTime.now().format(DATE_TIME_FORMATTER);

        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        // === STYLISH HEADER WITH BORDER ===
        Font brandFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, new Color(0, 102, 204));
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 7);

        // Header Table with brand name and invoice info
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{60f, 40f});

        // Left: Brand Name
        PdfPCell brandCell = new PdfPCell(new Phrase("STITCH & STYLE", brandFont));
        brandCell.setBorder(Rectangle.NO_BORDER);
        brandCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(brandCell);

        // Right: Invoice Date
        Paragraph invDatePara = new Paragraph();
        invDatePara.add(new Phrase("Invoice #" + client.getId() + "-" + targetDate.format(DateTimeFormatter.ofPattern("ddMMyy")) + "\n", valueFont));
        invDatePara.add(new Phrase(now, smallFont));
        PdfPCell dateCell = new PdfPCell(invDatePara);
        dateCell.setBorder(Rectangle.NO_BORDER);
        dateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        dateCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(dateCell);

        headerTable.setSpacingAfter(3f);
        document.add(headerTable);

        // Horizontal line separator
        com.lowagie.text.pdf.draw.LineSeparator line = new com.lowagie.text.pdf.draw.LineSeparator();
        line.setLineWidth(1f);
        line.setLineColor(new Color(0, 102, 204));
        document.add(new Chunk(line));
        document.add(new Paragraph(" ")); // Small space

        // === CLIENT INFO IN TWO COLUMNS ===
        PdfPTable clientTable = new PdfPTable(2);
        clientTable.setWidthPercentage(100);
        clientTable.setWidths(new float[]{50f, 50f});

        // Left Column
        Paragraph leftCol = new Paragraph();
        leftCol.add(new Phrase("CUSTOMER\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Color.GRAY)));
        leftCol.add(new Phrase(client.getName() + " (" + client.getId() + ")\n\n", valueFont)); // Extra line break for spacing
        leftCol.add(new Phrase("Mobile: " + nvl(client.getMobile()) + "\n", smallFont));
        if (client.getWhatsAppNo() != null && !client.getWhatsAppNo().trim().isEmpty()
            && !client.getWhatsAppNo().equals(client.getMobile())) {
            leftCol.add(new Phrase("WhatsApp: " + client.getWhatsAppNo() + "\n", smallFont));
        }

        PdfPCell leftCell = new PdfPCell(leftCol);
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setPaddingBottom(8f); // Increased padding
        clientTable.addCell(leftCell);

        // Right Column
        Paragraph rightCol = new Paragraph();
        rightCol.add(new Phrase("ORDER INFO\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Color.GRAY)));
        rightCol.add(new Phrase("Order Date: " + targetDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) + "\n", smallFont));

        PdfPCell rightCell = new PdfPCell(rightCol);
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setPaddingBottom(8f); // Increased padding
        clientTable.addCell(rightCell);

        document.add(clientTable);

        // === ORDER DETAILS SECTION ===
        // Section header with background
        PdfPTable orderHeaderTable = new PdfPTable(1);
        orderHeaderTable.setWidthPercentage(100);
        orderHeaderTable.setSpacingBefore(5f); // Increased spacing before order section

        PdfPCell orderHeaderCell = new PdfPCell(new Phrase("ORDER DETAILS", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE)));
        orderHeaderCell.setBackgroundColor(new Color(0, 102, 204));
        orderHeaderCell.setPadding(4f);
        orderHeaderCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        orderHeaderTable.addCell(orderHeaderCell);
        document.add(orderHeaderTable);

        // Calculate totals across all orders on same date
        long totalDressCount = 0, totalWaistcoatCount = 0;
        long totalDressAmount = 0, totalWaistcoatAmount = 0;
        int totalMatelQty = 0, totalTichQty = 0, totalKantaQty = 0, totalJaliQty = 0, totalKrhaiQty = 0;
        long totalMatel = 0, totalTich = 0, totalKanta = 0, totalJali = 0, totalKrhai = 0;
        long grandTotal = 0, grandPaid = 0, grandRemaining = 0;
        java.util.Set<java.time.LocalDate> returnDates = new java.util.HashSet<>();
        java.util.Set<String> paymentStatuses = new java.util.HashSet<>();
        java.util.Set<String> readyStatuses = new java.util.HashSet<>();
        StringBuilder allNotes = new StringBuilder();

        // Track rates for dress and waistcoat (we'll use average if rates differ)
        long dressRateSum = 0, waistcoatRateSum = 0;
        int dressRateCount = 0, waistcoatRateCount = 0;

        for (Payments p : paymentsOnDate) {
            long dressCount = (p.getDressCount() != null ? p.getDressCount() : 0);
            long waistcoatCount = (p.getWaistcoatCount() != null ? p.getWaistcoatCount() : 0);

            totalDressCount += dressCount;
            totalWaistcoatCount += waistcoatCount;

            // Calculate dress amount
            if (dressCount > 0 && p.getDressRate() != null) {
                totalDressAmount += dressCount * p.getDressRate();
                dressRateSum += p.getDressRate();
                dressRateCount++;
            }

            // Calculate waistcoat amount
            if (waistcoatCount > 0 && p.getWaistcoatRate() != null) {
                totalWaistcoatAmount += waistcoatCount * p.getWaistcoatRate();
                waistcoatRateSum += p.getWaistcoatRate();
                waistcoatRateCount++;
            }

            if (p.getWithMatel() != null && p.getMatelAmount() != null) {
                totalMatelQty += p.getWithMatel();
                totalMatel += p.getWithMatel() * p.getMatelAmount();
            }
            if (p.getWithTich() != null && p.getTichAmount() != null) {
                totalTichQty += p.getWithTich();
                totalTich += p.getWithTich() * p.getTichAmount();
            }
            if (p.getWithKanta() != null && p.getKantaAmount() != null) {
                totalKantaQty += p.getWithKanta();
                totalKanta += p.getWithKanta() * p.getKantaAmount();
            }
            if (p.getWithJali() != null && p.getJaliAmount() != null) {
                totalJaliQty += p.getWithJali();
                totalJali += p.getWithJali() * p.getJaliAmount();
            }
            if (p.getWithKrhai() != null && p.getKrhaiAmount() != null) {
                totalKrhaiQty += p.getWithKrhai();
                totalKrhai += p.getWithKrhai() * p.getKrhaiAmount();
            }

            grandTotal += (p.getTotalAmount() != null ? p.getTotalAmount() : 0);
            grandPaid += (p.getPaidAmount() != null ? p.getPaidAmount() : 0);
            grandRemaining += (p.getRemainingAmount() != null ? p.getRemainingAmount() : 0);

            if (p.getReturnDate() != null) {
                returnDates.add(p.getReturnDate());
            }
            if (p.getPaymentStatus() != null) {
                paymentStatuses.add(p.getPaymentStatus());
            }
            if (p.getReadyStatus() != null && !p.getReadyStatus().isEmpty()) {
                readyStatuses.add(p.getReadyStatus());
            }
            if (p.getNotes() != null && !p.getNotes().trim().isEmpty()) {
                if (allNotes.length() > 0) allNotes.append("; ");
                allNotes.append(p.getNotes());
            }
        }

        // Calculate average rates
        long avgDressRate = dressRateCount > 0 ? dressRateSum / dressRateCount : 0;
        long avgWaistcoatRate = waistcoatRateCount > 0 ? waistcoatRateSum / waistcoatRateCount : 0;

        // Order items in a clean table format
        PdfPTable itemsTable = new PdfPTable(3);
        itemsTable.setWidthPercentage(100);
        itemsTable.setWidths(new float[]{50f, 20f, 30f});
        itemsTable.setSpacingBefore(2f);

        Font itemFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
        Font qtyFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
        Font amountFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);

        if (totalDressCount > 0) {
            String dressQty = totalDressCount + "@" + avgDressRate;
            addItemRow(itemsTable, "Dresses", dressQty, String.valueOf(totalDressAmount), itemFont, qtyFont, amountFont);
        }
        if (totalWaistcoatCount > 0) {
            String waistcoatQty = totalWaistcoatCount + "@" + avgWaistcoatRate;
            addItemRow(itemsTable, "Waistcoats", waistcoatQty, String.valueOf(totalWaistcoatAmount), itemFont, qtyFont, amountFont);
        }
        if (totalMatel > 0) {
            long avgMatelRate = totalMatelQty > 0 ? totalMatel / totalMatelQty : 0;
            String matelQty = totalMatelQty + "@" + avgMatelRate;
            addItemRow(itemsTable, "Matel", matelQty, String.valueOf(totalMatel), itemFont, qtyFont, amountFont);
        }
        if (totalTich > 0) {
            long avgTichRate = totalTichQty > 0 ? totalTich / totalTichQty : 0;
            String tichQty = totalTichQty + "@" + avgTichRate;
            addItemRow(itemsTable, "Tich", tichQty, String.valueOf(totalTich), itemFont, qtyFont, amountFont);
        }
        if (totalKanta > 0) {
            long avgKantaRate = totalKantaQty > 0 ? totalKanta / totalKantaQty : 0;
            String kantaQty = totalKantaQty + "@" + avgKantaRate;
            addItemRow(itemsTable, "Kanta", kantaQty, String.valueOf(totalKanta), itemFont, qtyFont, amountFont);
        }
        if (totalJali > 0) {
            long avgJaliRate = totalJaliQty > 0 ? totalJali / totalJaliQty : 0;
            String jaliQty = totalJaliQty + "@" + avgJaliRate;
            addItemRow(itemsTable, "Jali", jaliQty, String.valueOf(totalJali), itemFont, qtyFont, amountFont);
        }
        if (totalKrhai > 0) {
            long avgKrhaiRate = totalKrhaiQty > 0 ? totalKrhai / totalKrhaiQty : 0;
            String krhaiQty = totalKrhaiQty + "@" + avgKrhaiRate;
            addItemRow(itemsTable, "Krhai", krhaiQty, String.valueOf(totalKrhai), itemFont, qtyFont, amountFont);
        }

        document.add(itemsTable);

        // === PAYMENT SUMMARY WITH MODERN DESIGN ===
        PdfPTable paymentHeaderTable = new PdfPTable(1);
        paymentHeaderTable.setWidthPercentage(100);
        paymentHeaderTable.setSpacingBefore(8f); // Increased spacing before payment section

        PdfPCell paymentHeaderCell = new PdfPCell(new Phrase("PAYMENT SUMMARY", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE)));
        paymentHeaderCell.setBackgroundColor(new Color(0, 102, 204));
        paymentHeaderCell.setPadding(4f);
        paymentHeaderCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        paymentHeaderTable.addCell(paymentHeaderCell);
        document.add(paymentHeaderTable);

        // Payment summary table
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(100);
        summaryTable.setWidths(new float[]{60f, 40f});
        summaryTable.setSpacingBefore(2f);

        addSummaryRowStyled(summaryTable, "Total Amount:", String.valueOf(grandTotal), smallFont, amountFont);
        addSummaryRowStyled(summaryTable, "Paid Amount:", String.valueOf(grandPaid), smallFont, itemFont);
        addSummaryRowStyled(summaryTable, "Remaining:", String.valueOf(grandRemaining), smallFont, amountFont);

        document.add(summaryTable);

        // === STATUS & INFO SECTION ===
        PdfPTable statusInfoTable = new PdfPTable(2);
        statusInfoTable.setWidthPercentage(100);
        statusInfoTable.setWidths(new float[]{50f, 50f});
        statusInfoTable.setSpacingBefore(8f); // Increased spacing before status section

        // Left: Payment & Order Status
        Paragraph leftStatus = new Paragraph();
        leftStatus.add(new Phrase("STATUS\n\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Color.GRAY))); // Added extra line break

        // Payment Status - Calculate based on actual totals
        String paymentStatusStr;
        if (grandRemaining <= 0 && grandTotal > 0) {
            paymentStatusStr = "Paid";
        } else if (grandPaid > 0 && grandRemaining > 0) {
            paymentStatusStr = "Partial";
        } else {
            paymentStatusStr = "Unpaid";
        }
        leftStatus.add(new Phrase("Payment: " + paymentStatusStr + "\n\n", smallFont)); // Added extra line break

        // Order Status - Convert to readable format
        String orderStatusStr;
        if (readyStatuses.isEmpty()) {
            orderStatusStr = "Not Ready Yet";
        } else if (readyStatuses.contains("PICKED_UP")) {
            orderStatusStr = "Returned";
        } else if (readyStatuses.contains("NOTIFIED") || readyStatuses.contains("READY")) {
            orderStatusStr = "Ready";
        } else {
            orderStatusStr = "Not Ready Yet";
        }
        leftStatus.add(new Phrase("Order: " + orderStatusStr, smallFont));

        PdfPCell leftStatusCell = new PdfPCell(leftStatus);
        leftStatusCell.setBorder(Rectangle.NO_BORDER);
        leftStatusCell.setPaddingBottom(5f); // Added bottom padding
        statusInfoTable.addCell(leftStatusCell);

        // Right: Return Date
        Paragraph rightStatus = new Paragraph();
        rightStatus.add(new Phrase("DELIVERY\n\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7, Color.GRAY))); // Added extra line break
        if (!returnDates.isEmpty()) {
            String returnDatesStr = returnDates.stream()
                    .sorted()
                    .map(d -> d.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")))
                    .collect(java.util.stream.Collectors.joining(", "));
            rightStatus.add(new Phrase("Return: " + returnDatesStr, smallFont));
        } else {
            rightStatus.add(new Phrase("Return: TBD", smallFont));
        }

        PdfPCell rightStatusCell = new PdfPCell(rightStatus);
        rightStatusCell.setBorder(Rectangle.NO_BORDER);
        rightStatusCell.setPaddingBottom(5f); // Added bottom padding
        statusInfoTable.addCell(rightStatusCell);

        document.add(statusInfoTable);

        // === NOTES (if any) ===
        if (allNotes.length() > 0) {
            PdfPTable notesHeaderTable = new PdfPTable(1);
            notesHeaderTable.setWidthPercentage(100);
            notesHeaderTable.setSpacingBefore(5f);

            PdfPCell notesHeaderCell = new PdfPCell(new Phrase("NOTES", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE)));
            notesHeaderCell.setBackgroundColor(new Color(0, 102, 204));
            notesHeaderCell.setPadding(4f);
            notesHeaderCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            notesHeaderTable.addCell(notesHeaderCell);
            document.add(notesHeaderTable);

            Paragraph notesContent = new Paragraph(allNotes.toString(), smallFont);
            notesContent.setSpacingBefore(2f);
            document.add(notesContent);
        }

        // === FOOTER ===
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 6, Color.GRAY);

        Paragraph printDate = new Paragraph("Invoice Date: " + now, footerFont);
        printDate.setAlignment(Element.ALIGN_CENTER);
        printDate.setSpacingBefore(5f);
        document.add(printDate);

        Paragraph developer = new Paragraph("Developed by NS Developers", footerFont);
        developer.setAlignment(Element.ALIGN_CENTER);
        developer.setSpacingBefore(2f);
        document.add(developer);

        document.close();
    }

    // Helper methods for invoice
    private void addInfoRow(PdfPTable table, String label, String value) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 7);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(2f);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(2f);
        table.addCell(valueCell);
    }

    private void addOrderItemRow(PdfPTable table, String item, String qty, String amount, com.lowagie.text.Font labelFont, com.lowagie.text.Font valueFont) {
        PdfPCell itemCell = new PdfPCell(new Phrase(item, labelFont));
        itemCell.setPadding(2f);
        table.addCell(itemCell);

        PdfPCell qtyCell = new PdfPCell(new Phrase(qty, valueFont));
        qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        qtyCell.setPadding(2f);
        table.addCell(qtyCell);

        PdfPCell amountCell = new PdfPCell(new Phrase(amount, valueFont));
        amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        amountCell.setPadding(2f);
        table.addCell(amountCell);
    }

    private void addPaymentRow(PdfPTable table, String label, String value, com.lowagie.text.Font labelFont, com.lowagie.text.Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setPadding(3f);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(3f);
        table.addCell(valueCell);
    }

    // Helper method for modern item rows
    private void addItemRow(PdfPTable table, String item, String qty, String amount,
                           com.lowagie.text.Font itemFont, com.lowagie.text.Font qtyFont, com.lowagie.text.Font amountFont) {
        PdfPCell itemCell = new PdfPCell(new Phrase(item, itemFont));
        itemCell.setBorder(Rectangle.NO_BORDER);
        itemCell.setPaddingTop(3f);
        itemCell.setPaddingBottom(3f);
        itemCell.setPaddingLeft(5f);
        table.addCell(itemCell);

        PdfPCell qtyCell = new PdfPCell(new Phrase(qty, qtyFont));
        qtyCell.setBorder(Rectangle.NO_BORDER);
        qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        qtyCell.setPaddingTop(3f);
        qtyCell.setPaddingBottom(3f);
        table.addCell(qtyCell);

        PdfPCell amountCell = new PdfPCell(new Phrase(amount, amountFont));
        amountCell.setBorder(Rectangle.NO_BORDER);
        amountCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        amountCell.setPaddingTop(3f);
        amountCell.setPaddingBottom(3f);
        amountCell.setPaddingRight(5f);
        table.addCell(amountCell);
    }

    // Helper method for styled summary rows
    private void addSummaryRowStyled(PdfPTable table, String label, String value,
                                    com.lowagie.text.Font labelFont, com.lowagie.text.Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingTop(3f);
        labelCell.setPaddingBottom(3f);
        labelCell.setPaddingLeft(5f);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPaddingTop(3f);
        valueCell.setPaddingBottom(3f);
        valueCell.setPaddingRight(5f);
        table.addCell(valueCell);
    }

}
