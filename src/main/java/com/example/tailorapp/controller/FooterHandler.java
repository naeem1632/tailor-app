package com.example.tailorapp.controller;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// === Page Event Helper ===
class FooterHandler extends PdfPageEventHelper {
    private final String note;
    private final LocalDate returnDate;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yy");

    public FooterHandler(String note, LocalDate returnDate) {
        this.note = note;
        this.returnDate = returnDate;
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        PdfPTable footer = new PdfPTable(2);
        try {
            footer.setWidths(new float[]{50f, 50f});
            footer.setTotalWidth(document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin());
            footer.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            // Left: Notes (inline with 7pt font)
            Chunk notesLabel = new Chunk("Notes: ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7f));
            Chunk notesContent = new Chunk((note != null && !note.isEmpty() ? note : ""), FontFactory.getFont(FontFactory.HELVETICA, 7f));

            Phrase notesPhrase = new Phrase();
            notesPhrase.add(notesLabel);
            notesPhrase.add(notesContent);

            PdfPCell leftCell = new PdfPCell(notesPhrase);
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            leftCell.setVerticalAlignment(Element.ALIGN_TOP);
            footer.addCell(leftCell);

            // Right: Return date line (moved to left alignment)
            String returnDateText = returnDate != null
                    ? "Return Date: " + returnDate.format(DATE_FORMATTER)
                    : "Return Date: __________";
            PdfPCell rightCell = new PdfPCell(new Phrase(returnDateText,
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7)));
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setHorizontalAlignment(Element.ALIGN_LEFT); // Changed from RIGHT to LEFT
            footer.addCell(rightCell);

            // Write at fixed position - moved higher up on page
            // Increased offset from 35 to 55 to move footer higher
            footer.writeSelectedRows(0, -1,
                    document.leftMargin(),
                    document.bottomMargin() + 55, // increased from 35 to 55 to move footer higher on page
                    writer.getDirectContent());
        } catch (DocumentException de) {
            throw new ExceptionConverter(de);
        }
    }
}
