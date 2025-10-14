package com.example.tailorapp.controller;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

// === Page Event Helper ===
class FooterHandler extends PdfPageEventHelper {
    private final String now;

    public FooterHandler(String now) {
        this.now = now;
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        PdfPTable footer = new PdfPTable(2);
        try {
            footer.setWidths(new float[]{50f, 50f});
            footer.setTotalWidth(document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin());
            footer.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            // Left: printed date
            PdfPCell leftCell = new PdfPCell(new Phrase("Printed: " + now,
                    FontFactory.getFont(FontFactory.HELVETICA, 7)));
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            footer.addCell(leftCell);

            // Right: return date line
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
