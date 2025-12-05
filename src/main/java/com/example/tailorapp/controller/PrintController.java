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

        // Get latest payment with return date (where status is not "returned" and returnDate is not null)
        Optional<Payments> latestPaymentWithReturnDate = paymentsService.findByClient(id).stream()
                .filter(p -> p.getReturnDate() != null && !"returned".equalsIgnoreCase(p.getReturnStatus()))
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

        // Get latest payment with return date (where status is not "returned" and returnDate is not null)
        Optional<Payments> latestPaymentWithReturnDate = paymentsService.findByClient(id).stream()
                .filter(p -> p.getReturnDate() != null && !"returned".equalsIgnoreCase(p.getReturnStatus()))
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

}
