package cl.diego.NanoStock.dataexport.service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import cl.diego.NanoStock.common.data.SpreadsheetSanitizer;
import lombok.RequiredArgsConstructor;

@Component
public class TabularDataExporter {

    private final ObjectMapper objectMapper;

    public TabularDataExporter(@org.springframework.beans.factory.annotation.Autowired(required = false) ObjectMapper objectMapper) {
        if (objectMapper != null) {
            this.objectMapper = objectMapper;
        } else {
            this.objectMapper = new ObjectMapper().findAndRegisterModules();
        }
    }
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] exportToExcel(String sheetName, String[] headers, List<List<Object>> rows) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(sheetName);

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Data Cell Style
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // Header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowIdx = 1;
            for (List<Object> rowData : rows) {
                Row row = sheet.createRow(rowIdx++);
                for (int colIdx = 0; colIdx < rowData.size(); colIdx++) {
                    Cell cell = row.createCell(colIdx);
                    cell.setCellStyle(dataStyle);
                    Object val = rowData.get(colIdx);
                    setCellValue(cell, val);
                }
            }

            // Auto-size columns (capped at reasonable width)
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                if (sheet.getColumnWidth(i) > 15000) {
                    sheet.setColumnWidth(i, 15000);
                }
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportToCsv(String[] headers, List<List<Object>> rows) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.builder().setHeader(headers).build())) {

            for (List<Object> rowData : rows) {
                Object[] values = new Object[rowData.size()];
                for (int i = 0; i < rowData.size(); i++) {
                    Object val = rowData.get(i);
                    if (val == null) {
                        values[i] = "";
                    } else if (val instanceof String s) {
                        values[i] = SpreadsheetSanitizer.sanitize(s);
                    } else if (val instanceof LocalDate ld) {
                        values[i] = ld.format(DATE_FMT);
                    } else if (val instanceof LocalDateTime ldt) {
                        values[i] = ldt.format(DATE_TIME_FMT);
                    } else {
                        values[i] = String.valueOf(val);
                    }
                }
                csvPrinter.printRecord(values);
            }

            csvPrinter.flush();
            return out.toByteArray();
        }
    }

    public <T> byte[] exportToJson(List<T> items) throws Exception {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(items);
    }

    private void setCellValue(Cell cell, Object val) {
        if (val == null) {
            cell.setCellValue("");
        } else if (val instanceof Number n) {
            cell.setCellValue(n.doubleValue());
        } else if (val instanceof Boolean b) {
            cell.setCellValue(b);
        } else if (val instanceof LocalDate ld) {
            cell.setCellValue(ld.format(DATE_FMT));
        } else if (val instanceof LocalDateTime ldt) {
            cell.setCellValue(ldt.format(DATE_TIME_FMT));
        } else {
            String text = val.toString();
            cell.setCellValue(SpreadsheetSanitizer.sanitize(text));
        }
    }
}
