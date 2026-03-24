package com.example.tailorapp.controller;

import com.example.tailorapp.config.AppConstants;
import com.example.tailorapp.model.Client;
import com.example.tailorapp.model.DressMeasurement;
import com.example.tailorapp.model.Payments;
import com.example.tailorapp.model.ShirtMeasurement;
import com.example.tailorapp.model.WaistcoatMeasurement;
import com.example.tailorapp.service.ClientService;
import com.example.tailorapp.service.MeasurementService;
import com.example.tailorapp.service.PaymentsService;
import com.example.tailorapp.service.ShirtService;
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
    private final ShirtService shirtService;
    private final PaymentsService paymentsService;

    public PrintController(ClientService clientService,
                           MeasurementService measurementService,
                           StorageProperties storageProperties,
                           WaistcoatService waistcoatService,
                           ShirtService shirtService,
                           PaymentsService paymentsService) {
        this.clientService = clientService;
        this.measurementService = measurementService;
        this.storageProperties = storageProperties;
        this.waistcoatService = waistcoatService;
        this.shirtService = shirtService;
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

        Rectangle slipSize = new Rectangle(PageSize.LEGAL.getWidth() / 2, PageSize.LEGAL.getHeight() / 2);
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

        // Right: Print date (aligned to left side)
        PdfPCell dateCell = new PdfPCell(new Phrase("Printed: " + now,
                FontFactory.getFont(FontFactory.HELVETICA, 6)));
        dateCell.setBorder(Rectangle.NO_BORDER);
        dateCell.setHorizontalAlignment(Element.ALIGN_LEFT); // Changed from RIGHT to LEFT
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

        // First column: Length, Terra, Terra Down, Chest, Chest Fitting, Waist, Hip, Bain Size
        // Second column: Arm, Shoulder-arm, Upper arm, Center arm, Lower arm, Cuff length, Cuff width, Collar size
        addRow4IfNotNull(table, "Length", nvl(m.getKameezLength()), "Arm", nvl(m.getArm()));
        addRow4IfNotNull(table, "Terra", nvl(m.getTerra()), "Shoulder-arm", nvl(m.getShoulderArm()));
        addRow4IfNotNull(table, "Terra down", nvl(m.getTerraDown()), "Upper arm", nvl(m.getUpperArm()));
        addRow4IfNotNull(table, "Chest", nvl(m.getChest()), "Center arm", nvl(m.getCenterArm()));
        addRow4IfNotNull(table, "Chest fitting", nvl(m.getChestFitting()), "Lower arm", nvl(m.getLowerArm()));
        addRow4IfNotNull(table, "Waist", nvl(m.getWaist()), "Cuff length", nvl(m.getCuffLength()));
        addRow4IfNotNull(table, "Hip", nvl(m.getHip()), "Cuff width", nvl(m.getCuffWidth()));
        addRow4IfNotNull(table, "Bain size", nvl(m.getBainSize()), "Collar size", nvl(m.getCollarSize()));
        doc.add(table);
        doc.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 2))); // reduced from 3
    }

    // === Shalwar Section ===
    private void addShalwarSection(Document doc, DressMeasurement m) throws DocumentException {
        PdfPTable table = createSectionTable("Shalwar Measurements");

        addRow4IfNotNull(table, "Length", nvl(m.getShalwarLength()),  "Fitting", nvl(m.getShalwarFitting()));
        addRow4IfNotNull(table,"Payncha", nvl(m.getPayncha()), "Asan", nvl(m.getAsan()));

        doc.add(table);
        doc.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 2))); // reduced from 3
    }

    // === Pajama Section ===
    private void addPajamaSection(Document doc, DressMeasurement m) throws DocumentException {
        PdfPTable table = createSectionTable("Pajama Measurements");

        // First column: Length, Asan, Payncha
        // Second column: Upper Fitting, Middle Fitting, Lower Fitting
        addRow4IfNotNull(table, "Pajama Length", nvl(m.getPajamaLength()), "Upper Fitting", nvl(m.getUpperFitting()));
        addRow4IfNotNull(table, "Pajama Asan", nvl(m.getPajamaAsan()), "Middle Fitting", nvl(m.getMiddleFitting()));
        addRow4IfNotNull(table, "Pajama Payncha", nvl(m.getPajamaPayncha()), "Lower Fitting", nvl(m.getLowerFitting()));
        addRow4IfNotNull(table, "Pajama Pocket", nvl(m.getPajamaPocket()), "Elastic", (m.getPajamaElastic() != null && m.getPajamaElastic()) ? "Yes" : "No");

        doc.add(table);
        doc.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 2))); // reduced from 3
    }

    // === Design Section ===
    private void addDesignSection(Document doc, DressMeasurement m) throws DocumentException {
        PdfPTable table = createSectionTable("Design & Finishing");

        // First column: Collar design, Cuff type, Cuff design, Front pocket, Front pocket design, Side pocket, Shalwar pocket, Front patti design, Front patti kaj
        // Second column: Bain, Button, Daman type, Daman stitching, Stitching, Design stitch, Jali, Kanta

        // Row 1: Collar design | Bain design
        String bainValue = null;
        if (m.getHasBain() != null && m.getHasBain() && m.getBainType() != null) {
            bainValue = m.getBainType();
        }
        // Always show both columns with headings
        if (bainValue != null) {
            addImageRow4(table, "Collar design", nvl(m.getCollarType()), "Bain design", bainValue);
        } else {
            addRow4IfNotNull(table, "Collar design", nvl(m.getCollarType()), "Bain design", "");
        }

        // Row 2: Cuff type | Button
        addRow4IfNotNull(table, "Cuff type", nvl(m.getCuffType()), "Button", nvl(m.getButtonType()));

        // Row 3: Cuff design | Daman type
        String cuffDesignValue = null;
        if (m.getHasCuffDesign() != null && m.getHasCuffDesign() && m.getCuffDesign() != null) {
            cuffDesignValue = m.getCuffDesign();
        }
        // Always show both columns with headings
        if (cuffDesignValue != null) {
            addImageRow4(table, "Cuff design", cuffDesignValue, "Daman type", nvl(m.getDamanType()));
        } else {
            addRow4IfNotNull(table, "Cuff design", "", "Daman type", nvl(m.getDamanType()));
        }

        // Row 4: Front pocket | Daman stitching
        addImageRow4(table, "Front pocket", m.getFrontPocket() ? "Yes" : "No", "Daman stitching", nvl(m.getDamanStitching()));

        // Row 5: Front pocket design | Stitching
        addImageRow4(table, "Front pocket design", m.getFrontPocketType(), "Stitching", nvl(m.getStitchType()));

        // Row 6: Side pocket | Design stitch
        addRow4IfNotNull(table, "Side pocket", nvl(m.getSidePocket()), "Design stitch", (m.getDesignStitch() != null && m.getDesignStitch()) ? "Yes" : "No");

        // Row 7: Shalwar pocket | Jali
        addRow4IfNotNull(table, "Shalwar pocket", m.getShalwarPocket() ? "Yes" : "No", "Jali", nvl(m.getJali()));

        // Row 8: Front patti design | Kanta
        addRow4IfNotNull(table, "Front patti design", nvl(m.getFrontPattiType()), "Kanta", (m.getKanta() != null && m.getKanta()) ? "Yes" : "No");

        // Row 9: Front patti kaj (left column only)
        addRow4IfNotNull(table, "Front patti kaj", nvl(m.getFrontPattiKaj()), "", "");

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
        // Always add rows with fixed column positions, showing headings even if values are empty
        // Skip the row only if both labels are empty or null
        if ((l1 == null || l1.isEmpty()) && (l2 == null || l2.isEmpty())) return;

        addRow4(table, l1, v1, l2, v2);
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

        Rectangle slipSize = new Rectangle(PageSize.LEGAL.getWidth() / 2, PageSize.LEGAL.getHeight() / 2);
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
        PdfPCell nameCell = new PdfPCell(new Phrase(nvl(client.getName() + " (" + client.getId() + ")"),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        nameCell.setBorder(Rectangle.NO_BORDER);
        nameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        nameCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(nameCell);

        // Right: Print date (aligned to left side)
        PdfPCell dateCell = new PdfPCell(new Phrase("Printed: " + now,
                FontFactory.getFont(FontFactory.HELVETICA, 6)));
        dateCell.setBorder(Rectangle.NO_BORDER);
        dateCell.setHorizontalAlignment(Element.ALIGN_LEFT); // Changed from RIGHT to LEFT
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
        // Always show label even if value is empty - fixed column positions
        if (label == null || label.isEmpty()) return;
        table.addCell(makeLabelCell(label));
        table.addCell(makeValueCell(value));
    }

    // Helper method to check if pajama measurements exist
    // Only print pajama section if pajamaLength has a value
    private boolean hasPajamaData(DressMeasurement m) {
        return m.getPajamaLength() != null && !m.getPajamaLength().toString().trim().isEmpty();
    }

    // Print PDF - Shirt
    @GetMapping("/shirt/{id}")
    public void printShirtSlip(@PathVariable Long id, HttpServletResponse response) throws Exception {
        Optional<Client> c = clientService.findById(id);
        if (c.isEmpty()) return;

        Client client = c.get();

        Optional<ShirtMeasurement> latestMeasurement = shirtService.findByClient(id)
                .stream()
                .max(Comparator.comparing(ShirtMeasurement::getDate, Comparator.nullsLast(Comparator.naturalOrder())));

        if (latestMeasurement.isEmpty()) return;

        ShirtMeasurement shirt = latestMeasurement.get();

        Optional<Payments> latestPaymentWithReturnDate = paymentsService.findByClient(id).stream()
                .filter(p -> p.getReturnDate() != null && !"PICKED_UP".equals(p.getReadyStatus()))
                .max(Comparator.comparing(Payments::getDate, Comparator.nullsLast(Comparator.naturalOrder())));

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=client_" + id + "_shirt_slip.pdf");

        Rectangle slipSize = new Rectangle(PageSize.LEGAL.getWidth() / 2, PageSize.LEGAL.getHeight() / 2);
        Document document = new Document(slipSize, 15, 15, 10, 15);
        String now = LocalDateTime.now().format(DATE_TIME_FORMATTER);

        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
        writer.setPageEvent(new FooterHandler(
                shirt.getNotes(),
                latestPaymentWithReturnDate.map(Payments::getReturnDate).orElse(null)
        ));
        document.open();

        // === Header ===
        PdfPTable headerTable = new PdfPTable(3);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{30f, 40f, 30f});

        PdfPCell leftCell = new PdfPCell(new Phrase(""));
        leftCell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(leftCell);

        PdfPCell nameCell = new PdfPCell(new Phrase(nvl(client.getName() + " (" + client.getId() + ")"),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
        nameCell.setBorder(Rectangle.NO_BORDER);
        nameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        nameCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(nameCell);

        PdfPCell dateCell = new PdfPCell(new Phrase("Printed: " + now,
                FontFactory.getFont(FontFactory.HELVETICA, 6)));
        dateCell.setBorder(Rectangle.NO_BORDER);
        dateCell.setHorizontalAlignment(Element.ALIGN_LEFT); // Changed from RIGHT to LEFT
        dateCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(dateCell);

        headerTable.setSpacingAfter(1f);
        document.add(headerTable);

        // === Qty info row ===
        PdfPTable infoTable = new PdfPTable(1);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingAfter(1.5f);
        infoTable.addCell(makeInfoCell("Shirt Qty: " + nvl(shirt.getQty())));
        document.add(infoTable);

        // === Shirt measurements section ===
        addShirtMeasurementsSection(document, shirt);
        addShirtDesignSection(document, shirt);

        document.close();
    }

    // === Shirt Measurements Section (same fields/order as Kameez) ===
    private void addShirtMeasurementsSection(Document doc, ShirtMeasurement m) throws DocumentException {
        PdfPTable table = createSectionTable("Shirt Measurements");

        addRow4IfNotNull(table, "Length", nvl(m.getKameezLength()), "Arm", nvl(m.getArm()));
        addRow4IfNotNull(table, "Shoulder-arm", nvl(m.getShoulderArm()), "Upper arm", nvl(m.getUpperArm()));
        addRow4IfNotNull(table, "Center arm", nvl(m.getCenterArm()), "Lower arm", nvl(m.getLowerArm()));
        addRow4IfNotNull(table, "Cuff length", nvl(m.getCuffLength()), "Cuff width", nvl(m.getCuffWidth()));
        addRow4IfNotNull(table, "Terra", nvl(m.getTerra()), "Terra down", nvl(m.getTerraDown()));
        addRow4IfNotNull(table, "Collar size", nvl(m.getCollarSize()), "Bain size", nvl(m.getBainSize()));
        addRow4IfNotNull(table, "Chest", nvl(m.getChest()), "Chest fitting", nvl(m.getChestFitting()));
        addRow4IfNotNull(table, "Waist", nvl(m.getWaist()), "Hip", nvl(m.getHip()));

        doc.add(table);
        doc.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 2)));
    }

    // === Shirt Design Section ===
    private void addShirtDesignSection(Document doc, ShirtMeasurement m) throws DocumentException {
        PdfPTable table = createSectionTable("Design & Finishing");

        // Row 1: Collar design (left column only)
        addRow4IfNotNull(table, "Collar design", nvl(m.getCollarType()), "", "");

        // Row 2: Bain design (if selected, left column only)
        if (m.getHasBain() != null && m.getHasBain()) {
            addImageRow4(table, "Bain design", m.getBainType(), "", "");
        }

        // Row 3: Cuff design (if selected, left column only)
        if (m.getHasCuffDesign() != null && m.getHasCuffDesign()) {
            addImageRow4(table, "Cuff design", m.getCuffDesign(), "", "");
        }

        // Row 4: Front pocket | Front pocket design
        addImageRow4(table, "Front pocket", m.getFrontPocket() != null && m.getFrontPocket() ? "Yes" : "No", "Front pocket design", m.getFrontPocketType());

        // Row 5: Cuff type | Stitching
        addRow4IfNotNull(table, "Cuff type", nvl(m.getCuffType()), "Stitching", nvl(m.getStitchType()));

        doc.add(table);
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

        // Left: Brand Name + contact
        Paragraph brandPara = new Paragraph();
        brandPara.add(new Phrase("STITCH & STYLE\n", brandFont));
        brandPara.add(new Phrase("\n", FontFactory.getFont(FontFactory.HELVETICA, 4)));
        brandPara.add(new Phrase("CH Naseer  |  03002645111", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(30, 30, 30))));
        PdfPCell brandCell = new PdfPCell(brandPara);
        brandCell.setBorder(Rectangle.NO_BORDER);
        brandCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        brandCell.setPaddingBottom(4f);
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

        // === ORDER DETAILS — grouped by returnDate ===
        // Group payments by returnDate (null returnDate goes in its own group keyed by null)
        java.util.LinkedHashMap<java.time.LocalDate, java.util.List<Payments>> byReturnDate = new java.util.LinkedHashMap<>();
        for (Payments p : paymentsOnDate) {
            java.time.LocalDate key = p.getReturnDate(); // null is a valid key
            byReturnDate.computeIfAbsent(key, k -> new java.util.ArrayList<>()).add(p);
        }

        Font itemFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
        Font qtyFont = FontFactory.getFont(FontFactory.HELVETICA, 8);
        Font amountFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8);

        // Overall grand totals (across all groups) for PAYMENT SUMMARY
        long grandTotal = 0, grandPaid = 0, grandRemaining = 0, grandDiscount = 0;

        // Render one "ORDER DETAILS" block per returnDate group
        boolean firstGroup = true;
        for (java.util.Map.Entry<java.time.LocalDate, java.util.List<Payments>> entry : byReturnDate.entrySet()) {
            java.time.LocalDate groupReturnDate = entry.getKey();
            java.util.List<Payments> group = entry.getValue();

            // Section header
            PdfPTable orderHeaderTable = new PdfPTable(1);
            orderHeaderTable.setWidthPercentage(100);
            orderHeaderTable.setSpacingBefore(firstGroup ? 5f : 8f);
            firstGroup = false;

            String headerLabel = "ORDER DETAILS";
            PdfPCell orderHeaderCell = new PdfPCell(new Phrase(headerLabel, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE)));
            orderHeaderCell.setBackgroundColor(new Color(0, 102, 204));
            orderHeaderCell.setPadding(4f);
            orderHeaderCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            orderHeaderTable.addCell(orderHeaderCell);
            document.add(orderHeaderTable);

            // Aggregate this group
            long grpDressCount = 0, grpWaistcoatCount = 0, grpShirtCount = 0;
            long grpDressAmount = 0, grpWaistcoatAmount = 0, grpShirtAmount = 0;
            long grpDressRateSum = 0, grpWaistcoatRateSum = 0, grpShirtRateSum = 0;
            int grpDressRateCount = 0, grpWaistcoatRateCount = 0, grpShirtRateCount = 0;
            int grpMatelQty = 0, grpTichQty = 0, grpKantaQty = 0, grpJaliQty = 0, grpKrhaiQty = 0;
            long grpMatel = 0, grpTich = 0, grpKanta = 0, grpJali = 0, grpKrhai = 0;
            java.util.Set<String> grpReadyStatuses = new java.util.HashSet<>();

            for (Payments p : group) {
                long dc = p.getDressCount() != null ? p.getDressCount() : 0;
                long wc = p.getWaistcoatCount() != null ? p.getWaistcoatCount() : 0;
                long sc = p.getShirtCount() != null ? p.getShirtCount() : 0;
                grpDressCount += dc;
                grpWaistcoatCount += wc;
                grpShirtCount += sc;
                if (dc > 0 && p.getDressRate() != null) {
                    grpDressAmount += dc * p.getDressRate();
                    grpDressRateSum += p.getDressRate();
                    grpDressRateCount++;
                }
                if (wc > 0 && p.getWaistcoatRate() != null) {
                    grpWaistcoatAmount += wc * p.getWaistcoatRate();
                    grpWaistcoatRateSum += p.getWaistcoatRate();
                    grpWaistcoatRateCount++;
                }
                if (sc > 0 && p.getShirtRate() != null) {
                    grpShirtAmount += sc * p.getShirtRate();
                    grpShirtRateSum += p.getShirtRate();
                    grpShirtRateCount++;
                }
                if (p.getWithMatel() != null && p.getMatelAmount() != null) { grpMatelQty += p.getWithMatel(); grpMatel += (long) p.getWithMatel() * p.getMatelAmount(); }
                if (p.getWithTich()  != null && p.getTichAmount()  != null) { grpTichQty  += p.getWithTich();  grpTich  += (long) p.getWithTich()  * p.getTichAmount();  }
                if (p.getWithKanta() != null && p.getKantaAmount() != null) { grpKantaQty += p.getWithKanta(); grpKanta += (long) p.getWithKanta() * p.getKantaAmount(); }
                if (p.getWithJali()  != null && p.getJaliAmount()  != null) { grpJaliQty  += p.getWithJali();  grpJali  += (long) p.getWithJali()  * p.getJaliAmount();  }
                if (p.getWithKrhai() != null && p.getKrhaiAmount() != null) { grpKrhaiQty += p.getWithKrhai(); grpKrhai += (long) p.getWithKrhai() * p.getKrhaiAmount(); }

                long pNetTotal = p.getTotalAmount()  != null ? p.getTotalAmount()  : 0;
                long pDisc     = p.getDiscount()     != null ? p.getDiscount()     : 0;
                long pPaid     = p.getPaidAmount()   != null ? p.getPaidAmount()   : 0;
                // Calculate gross total (totalAmount already has discount subtracted, so add it back)
                long pGrossTotal = pNetTotal + pDisc;
                // Calculate remaining: GrossTotal - Discount - Paid
                long pRemain  = Math.max(pGrossTotal - pDisc - pPaid, 0);

                grandTotal     += pGrossTotal;  // Use gross total
                grandPaid      += pPaid;
                grandRemaining += pRemain;
                grandDiscount  += pDisc;

                if (p.getReadyStatus() != null && !p.getReadyStatus().isEmpty()) grpReadyStatuses.add(p.getReadyStatus());
            }

            long avgDressRate     = grpDressRateCount     > 0 ? grpDressRateSum     / grpDressRateCount     : 0;
            long avgWaistcoatRate = grpWaistcoatRateCount > 0 ? grpWaistcoatRateSum / grpWaistcoatRateCount : 0;
            long avgShirtRate     = grpShirtRateCount     > 0 ? grpShirtRateSum     / grpShirtRateCount     : 0;

            // Items table for this group
            PdfPTable itemsTable = new PdfPTable(3);
            itemsTable.setWidthPercentage(100);
            itemsTable.setWidths(new float[]{50f, 20f, 30f});
            itemsTable.setSpacingBefore(2f);

            if (grpDressCount > 0)     addItemRow(itemsTable, "Dresses",    grpDressCount     + "@" + avgDressRate,                             String.valueOf(grpDressAmount),     itemFont, qtyFont, amountFont);
            if (grpWaistcoatCount > 0) addItemRow(itemsTable, "Waistcoats", grpWaistcoatCount + "@" + avgWaistcoatRate,                         String.valueOf(grpWaistcoatAmount), itemFont, qtyFont, amountFont);
            if (grpShirtCount > 0)     addItemRow(itemsTable, "Shirts",     grpShirtCount     + "@" + avgShirtRate,                             String.valueOf(grpShirtAmount),     itemFont, qtyFont, amountFont);
            if (grpMatel > 0)  addItemRow(itemsTable, "Matel",  grpMatelQty  + "@" + (grpMatelQty  > 0 ? grpMatel  / grpMatelQty  : 0), String.valueOf(grpMatel),  itemFont, qtyFont, amountFont);
            if (grpTich  > 0)  addItemRow(itemsTable, "Tich",   grpTichQty   + "@" + (grpTichQty   > 0 ? grpTich   / grpTichQty   : 0), String.valueOf(grpTich),   itemFont, qtyFont, amountFont);
            if (grpKanta > 0)  addItemRow(itemsTable, "Kanta",  grpKantaQty  + "@" + (grpKantaQty  > 0 ? grpKanta  / grpKantaQty  : 0), String.valueOf(grpKanta),  itemFont, qtyFont, amountFont);
            if (grpJali  > 0)  addItemRow(itemsTable, "Jali",   grpJaliQty   + "@" + (grpJaliQty   > 0 ? grpJali   / grpJaliQty   : 0), String.valueOf(grpJali),   itemFont, qtyFont, amountFont);
            if (grpKrhai > 0)  addItemRow(itemsTable, "Krhai",  grpKrhaiQty  + "@" + (grpKrhaiQty  > 0 ? grpKrhai  / grpKrhaiQty  : 0), String.valueOf(grpKrhai),  itemFont, qtyFont, amountFont);

            document.add(itemsTable);

            // Display return date for this group (after dress details)
            if (groupReturnDate != null) {
                Paragraph returnDatePara = new Paragraph();
                returnDatePara.add(new Phrase("Return Date: " + groupReturnDate.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8)));
                returnDatePara.setSpacingBefore(3f);
                document.add(returnDatePara);
            }
        }

        // === PAYMENT SUMMARY (combined across all groups) ===
        PdfPTable paymentHeaderTable = new PdfPTable(1);
        paymentHeaderTable.setWidthPercentage(100);
        paymentHeaderTable.setSpacingBefore(8f);

        PdfPCell paymentHeaderCell = new PdfPCell(new Phrase("PAYMENT SUMMARY", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE)));
        paymentHeaderCell.setBackgroundColor(new Color(0, 102, 204));
        paymentHeaderCell.setPadding(4f);
        paymentHeaderCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        paymentHeaderTable.addCell(paymentHeaderCell);
        document.add(paymentHeaderTable);

        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(100);
        summaryTable.setWidths(new float[]{60f, 40f});
        summaryTable.setSpacingBefore(2f);

        addSummaryRowStyled(summaryTable, "Total Amount:", String.valueOf(grandTotal), smallFont, amountFont);
        if (grandDiscount > 0) {
            addSummaryRowStyled(summaryTable, "Discount:", "-" + grandDiscount, smallFont, itemFont);
        }
        if (grandPaid > 0) {
            addSummaryRowStyled(summaryTable, "Paid Amount:", String.valueOf(grandPaid), smallFont, itemFont);
        }
        if (grandRemaining > 0) {
            addSummaryRowStyled(summaryTable, "Remaining Amount:", String.valueOf(grandRemaining), smallFont,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(204, 0, 0))); // Bold red for remaining
        }

        document.add(summaryTable);

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
