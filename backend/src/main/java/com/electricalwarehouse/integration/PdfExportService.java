package com.electricalwarehouse.integration;

import com.electricalwarehouse.config.AppProperties;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/** Landscape A4 table export. Vietnamese text needs a Unicode TTF font (see app.pdf-font). */
@Slf4j
@Service
public class PdfExportService {

    private static final List<String> FALLBACK_FONTS = List.of(
            "C:/Windows/Fonts/arial.ttf",
            "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
            "/usr/share/fonts/dejavu/DejaVuSans.ttf",
            "/Library/Fonts/Arial Unicode.ttf");

    private final BaseFont baseFont;

    public PdfExportService(AppProperties props) {
        this.baseFont = loadFont(props.pdfFont());
    }

    public byte[] export(TableData table) {
        Font titleFont = new Font(baseFont, 14, Font.BOLD);
        Font headerFont = new Font(baseFont, 9, Font.BOLD);
        Font cellFont = new Font(baseFont, 9);
        DecimalFormat numberFormat = new DecimalFormat("#,##0.##");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
        PdfWriter.getInstance(doc, out);
        doc.open();
        Paragraph title = new Paragraph(table.title(), titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        doc.add(title);
        for (String line : table.subtitleLines()) {
            Paragraph p = new Paragraph(line, cellFont);
            p.setAlignment(Element.ALIGN_CENTER);
            doc.add(p);
        }
        doc.add(Chunk.NEWLINE);

        PdfPTable pdfTable = new PdfPTable(table.headers().size());
        pdfTable.setWidthPercentage(100);
        pdfTable.setHeaderRows(1);
        for (String h : table.headers()) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setGrayFill(0.9f);
            pdfTable.addCell(cell);
        }
        for (List<Object> row : table.rows()) {
            for (Object v : row) {
                boolean numeric = v instanceof Number;
                String text = v == null ? "" : numeric
                        ? numberFormat.format(v instanceof BigDecimal bd ? bd : ((Number) v).doubleValue())
                        : v.toString();
                PdfPCell cell = new PdfPCell(new Phrase(text, cellFont));
                if (numeric) {
                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                }
                pdfTable.addCell(cell);
            }
        }
        doc.add(pdfTable);
        doc.close();
        return out.toByteArray();
    }

    private static BaseFont loadFont(String configured) {
        List<String> candidates = new ArrayList<>();
        if (StringUtils.hasText(configured)) {
            candidates.add(configured);
        }
        candidates.addAll(FALLBACK_FONTS);
        for (String path : candidates) {
            if (Files.isReadable(Path.of(path))) {
                try {
                    return BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                } catch (Exception e) {
                    log.warn("Cannot load PDF font {}: {}", path, e.getMessage());
                }
            }
        }
        log.warn("No Unicode font found for PDF export; Vietnamese characters may not render. Set PDF_FONT.");
        try {
            return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
