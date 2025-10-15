package com.example.tailorapp.controller;

import com.example.tailorapp.model.Client;
import com.example.tailorapp.model.DressMeasurement;
import com.example.tailorapp.model.WaistcoatMeasurement;
import com.example.tailorapp.service.ClientService;
import com.example.tailorapp.service.MeasurementService;
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
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/print")
@EnableConfigurationProperties(StorageProperties.class)
public class PrintController {

    private final ClientService clientService;
    private final MeasurementService measurementService;
    private final StorageProperties storageProperties;
    private final WaistcoatService waistcoatService;

    public PrintController(ClientService clientService,
                           MeasurementService measurementService,
                           StorageProperties storageProperties, WaistcoatService waistcoatService) {
        this.clientService = clientService;
        this.measurementService = measurementService;
        this.storageProperties = storageProperties;
        this.waistcoatService = waistcoatService;
    }

    // Print PDF
    @GetMapping("/dress/{id}")
    public void printClientSlip(@PathVariable Long id, HttpServletResponse response) throws Exception {
        Optional<Client> c = clientService.findById(id);
        if (c.isEmpty()) return;

        Client client = c.get();
        List<DressMeasurement> dressMeasurements = measurementService.findByClient(id);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=client_" + id + "_slip.pdf");

        Rectangle slipSize = new Rectangle(PageSize.A4.getWidth() / 2, PageSize.A4.getHeight() / 2);
        Document document = new Document(slipSize, 15, 15, 25, 20); // extra bottom margin for footer
        String now = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date());

        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
        writer.setPageEvent(new FooterHandler(now)); // attach footer handler
        document.open();

        // === Title ===
        Paragraph title = new Paragraph(nvl(client.getName() + " (" + client.getId() +")"),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10));
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 4)));

        // === Measurements ===
        for (DressMeasurement m : dressMeasurements) {
            addKameezSection(document, m);
            addShalwarSection(document, m);
            addDesignSection(document, m);
            addNotesSection(document, m.getNotes());
        }

        document.close();
    }

    // === Kameez Section ===
    private void addKameezSection(Document doc, DressMeasurement m) throws DocumentException {
        PdfPTable table = createSectionTable("Kameez Measurements");

        addRow4(table, "Kameez length", nvl(m.getKameezLength()), "Arm", nvl(m.getArm()));
        addRow4(table, "Upper arm", nvl(m.getUpperArm()), "Center aram", nvl(m.getCenterArm()));
        addRow4(table,  "Lower arm", nvl(m.getLowerArm()), "Shoulder-aram", nvl(m.getShoulderArm()));
        addRow4(table, "Cuff length", nvl(m.getCuffLength()), "Cuff width", nvl(m.getCuffWidth()));
        addRow4(table, "Terra", nvl(m.getTerra()), "Terra down", nvl(m.getTerraDown()));
        addRow4(table, "Collar size", nvl(m.getCollarSize()), "Bain size", nvl(m.getBainSize()));
        addRow4(table, "Chest", nvl(m.getChest()), "Chest fitting", nvl(m.getChestFitting()));
        addRow4(table, "Waist", nvl(m.getWaist()), "Hip", nvl(m.getHip()));
        doc.add(table);
        doc.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 4)));
    }

    // === Shalwar Section ===
    private void addShalwarSection(Document doc, DressMeasurement m) throws DocumentException {
        PdfPTable table = createSectionTable("Shalwar Measurements");

        addRow4(table, "Length", nvl(m.getShalwarLength()), "", null);
        addRow4(table, "Fitting", nvl(m.getShalwarFitting()), "Asan", nvl(m.getAsan()));
        addRow4(table, "Payncha", nvl(m.getPayncha()), "","");

        doc.add(table);
        doc.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 4)));
    }

    // === Design Section ===
    private void addDesignSection(Document doc, DressMeasurement m) throws DocumentException {
        PdfPTable table = createSectionTable("Design & Finishing");

        addRow4(table,"Collar design", nvl(m.getCollarType()), "Bain Design", m.getBainType());
        addImageRow4(table, "Side Pocket", nvl(m.getSidePocket()), "Daman type", m.getDamanType());
        addRow4(table,"Daman stitching", nvl(m.getDamanStitching()), "Front Pocket", (m.getFrontPocket() != null && m.getFrontPocket()) ? "Yes" : "No");
        addImageRow4(table, "Front Pocket design", m.getFrontPocketType(), "Cuff type", m.getCuffType());
        addImageRow4(table,  "Cuff design", m.getCuffDesign(), "","");
        addRow4(table, "Stitch", nvl(m.getStitchType()), "Design Stitch", (m.getDesignStitch() != null && m.getDesignStitch()) ? "Yes" : "No");
        addRow4(table, "Button", nvl(m.getButtonType()), "Front Patti Kaj", nvl(m.getFrontPattiKaj()));
        addRow4(table, "Patti Type", nvl(m.getFrontPattiType()), "","");
        addRow4(table, "Kanta", (m.getKanta() != null && m.getKanta()) ? "Yes" : "No", "Jali", nvl(m.getJali()));

        doc.add(table);
        doc.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 4)));
    }

    // === Notes Section ===
    private void addNotesSection(Document doc, String note) throws DocumentException {
        if (note != null && !note.isEmpty()) {
            Paragraph heading = new Paragraph("Notes:",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7));
            heading.setSpacingBefore(3f);
            heading.setSpacingAfter(1f);
            doc.add(heading);

            Paragraph notes = new Paragraph(note,
                    FontFactory.getFont(FontFactory.HELVETICA, 6));
            notes.setIndentationLeft(10f);
            doc.add(notes);
            doc.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 4)));
        }
    }


    // === Helpers ===
    private PdfPTable createSectionTable(String title) throws DocumentException {
        // 4 columns with custom widths: label(30%), value(20%), label(30%), value(20%)
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{30f, 15f, 30f, 15f});

        PdfPCell headingCell = new PdfPCell(
                new Phrase(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE)));
        headingCell.setBackgroundColor(Color.DARK_GRAY);
        headingCell.setColspan(4);
        headingCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headingCell.setPadding(2f);
        table.addCell(headingCell);

        return table;
    }


    private void addRow4(PdfPTable table, String l1, String v1, String l2, String v2) {
        table.addCell(makeLabelCell(l1));
        table.addCell(makeValueCell(v1));
        table.addCell(makeLabelCell(l2));
        table.addCell(makeValueCell(v2));
    }

    private void addImageRow4(PdfPTable table, String l1, String t1, String l2, String t2) {
        table.addCell(makeLabelCell(l1));
        table.addCell(getImageCell(t1));
        table.addCell(makeLabelCell(l2));
        table.addCell(getImageCell(t2));
    }

    private PdfPCell makeLabelCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text,
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6)));
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(1.5f);
        return cell;
    }


    private PdfPCell makeValueCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(nvl(text),
                FontFactory.getFont(FontFactory.HELVETICA, 7)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);   // center horizontally
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);     // center vertically
        cell.setPadding(1.5f);
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
        List<WaistcoatMeasurement> waistcoatMeasurements = waistcoatService.findByClient(id);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=client_" + id + "_slip.pdf");

        Rectangle slipSize = new Rectangle(PageSize.A4.getWidth() / 2, PageSize.A4.getHeight() / 2);
        Document document = new Document(slipSize, 15, 15, 25, 20); // extra bottom margin for footer
        String now = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date());

        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
        writer.setPageEvent(new FooterHandler(now)); // attach footer handler
        document.open();

        // === Title ===
        Paragraph title = new Paragraph(nvl(client.getName()),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10));
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        document.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 4)));

        // === Measurements ===
        for (WaistcoatMeasurement m : waistcoatMeasurements) {
            addWaistcoatSection(document, m);
            addNotesSection(document, m.getNotes());
        }

        document.close();
    }

    // === waistcoat Section ===
    private void addWaistcoatSection(Document doc, WaistcoatMeasurement m) throws DocumentException {
        PdfPTable table = createSectionTable("Waistcoat Measurements");

        addRow4(table, "Waistcoat length", nvl(m.getLength()), "Shoulder", nvl(m.getShoulder()));
        addRow4(table, "Neck", nvl(m.getLength()), "Chest", nvl(m.getChest()));
        addRow4(table, "Chest fitting", nvl(m.getChestFitting()), "Hip", nvl(m.getHip()));
        addRow4(table, "Bain size", nvl(m.getBainSize()), "Bain style", nvl(m.getBainType()));
        addRow4(table, "Daman style", nvl(m.getDamanType()), "Chest",null);

        doc.add(table);
        doc.add(new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 4)));
    }

}
