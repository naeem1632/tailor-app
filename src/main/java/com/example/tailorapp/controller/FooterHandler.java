package com.example.tailorapp.controller;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

// === Page Event Helper ===
class FooterHandler extends PdfPageEventHelper {
    private final String note;

    public FooterHandler(String note) {
        this.note = note;
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        PdfPTable footer = new PdfPTable(2);
        try {
            footer.setWidths(new float[]{60f, 40f});
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

            // Right: Return date line
            PdfPCell rightCell = new PdfPCell(new Phrase("Return Date: __________",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7)));
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            footer.addCell(rightCell);

            // Write at fixed position (bottom margin)
            footer.writeSelectedRows(0, -1,
                    document.leftMargin(),
                    document.bottomMargin() - 2, // just above bottom margin
                    writer.getDirectContent());
        } catch (DocumentException de) {
            throw new ExceptionConverter(de);
        }
    }
}
