package com.electricalwarehouse.integration;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.temporal.Temporal;
import java.util.List;

@Service
public class ExcelExportService {

    public byte[] export(TableData table) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Report");
            CellStyle bold = wb.createCellStyle();
            Font font = wb.createFont();
            font.setBold(true);
            bold.setFont(font);
            CellStyle number = wb.createCellStyle();
            number.setDataFormat(wb.createDataFormat().getFormat("#,##0.##"));

            int r = 0;
            Cell title = sheet.createRow(r++).createCell(0);
            title.setCellValue(table.title());
            title.setCellStyle(bold);
            for (String line : table.subtitleLines()) {
                sheet.createRow(r++).createCell(0).setCellValue(line);
            }
            r++;

            Row header = sheet.createRow(r++);
            List<String> headers = table.headers();
            for (int c = 0; c < headers.size(); c++) {
                Cell cell = header.createCell(c);
                cell.setCellValue(headers.get(c));
                cell.setCellStyle(bold);
            }
            for (List<Object> values : table.rows()) {
                Row row = sheet.createRow(r++);
                for (int c = 0; c < values.size(); c++) {
                    Object v = values.get(c);
                    Cell cell = row.createCell(c);
                    if (v instanceof Number n) {
                        cell.setCellValue(n instanceof BigDecimal bd ? bd.doubleValue() : n.doubleValue());
                        cell.setCellStyle(number);
                    } else if (v instanceof Temporal t) {
                        cell.setCellValue(t.toString());
                    } else if (v != null) {
                        cell.setCellValue(v.toString());
                    }
                }
            }
            for (int c = 0; c < headers.size(); c++) {
                sheet.autoSizeColumn(c);
            }
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Không tạo được file Excel", e);
        }
    }
}
