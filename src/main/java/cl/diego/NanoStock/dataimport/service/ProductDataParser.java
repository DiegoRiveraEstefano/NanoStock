package cl.diego.NanoStock.dataimport.service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import cl.diego.NanoStock.catalog.model.UnitOfMeasure;
import cl.diego.NanoStock.common.data.DataFormat;
import cl.diego.NanoStock.dataimport.dto.ProductImportRowDto;
import lombok.RequiredArgsConstructor;

@Component
public class ProductDataParser {

    private final ObjectMapper objectMapper;

    public ProductDataParser(@org.springframework.beans.factory.annotation.Autowired(required = false) ObjectMapper objectMapper) {
        if (objectMapper != null) {
            this.objectMapper = objectMapper;
        } else {
            this.objectMapper = new ObjectMapper().findAndRegisterModules();
        }
    }

    public List<ProductImportRowDto> parse(InputStream inputStream, DataFormat format) throws Exception {
        return switch (format) {
            case EXCEL -> parseExcel(inputStream);
            case CSV -> parseCsv(inputStream);
            case JSON -> parseJson(inputStream);
        };
    }

    private List<ProductImportRowDto> parseExcel(InputStream is) throws Exception {
        List<ProductImportRowDto> rows = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            if (!rowIterator.hasNext()) {
                return rows;
            }

            // Header mapping
            Row headerRow = rowIterator.next();
            Map<String, Integer> colMap = new HashMap<>();
            for (Cell cell : headerRow) {
                String header = normalizeHeader(getCellValueAsString(cell));
                if (!header.isBlank()) {
                    colMap.put(header, cell.getColumnIndex());
                }
            }

            int rowNum = 1;
            while (rowIterator.hasNext()) {
                rowNum++;
                Row r = rowIterator.next();
                if (isRowEmpty(r)) {
                    continue;
                }

                String name = getField(r, colMap, "nombre", "name", "producto");
                String barcode = getField(r, colMap, "codigobarras", "barcode", "bar_code", "ean", "upc", "codigo");
                String sku = getField(r, colMap, "sku", "codigosku");
                String desc = getField(r, colMap, "descripcion", "description", "detalle");
                String category = getField(r, colMap, "categoria", "category", "rubro");
                String brand = getField(r, colMap, "marca", "brand");
                String uomStr = getField(r, colMap, "unidad", "unidadmedida", "uom", "unitofmeasure");
                String minStockStr = getField(r, colMap, "stockminimo", "minstock", "minstockthreshold");
                String targetStockStr = getField(r, colMap, "stockoptimo", "stockdeseado", "targetstock", "targetstockquantity");
                String perishableStr = getField(r, colMap, "perecible", "esperecible", "isperishable", "perishable");

                rows.add(buildProductRow(rowNum, name, desc, sku, barcode, category, brand, uomStr, minStockStr, targetStockStr, perishableStr));
            }
        }
        return rows;
    }

    private List<ProductImportRowDto> parseCsv(InputStream is) throws Exception {
        List<ProductImportRowDto> rows = new ArrayList<>();
        try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
             CSVParser csvParser = CSVFormat.DEFAULT.builder()
                 .setHeader()
                 .setSkipHeaderRecord(true)
                 .setIgnoreHeaderCase(true)
                 .setTrim(true)
                 .build().parse(reader)) {

            Map<String, Integer> headerMap = new HashMap<>();
            for (Map.Entry<String, Integer> entry : csvParser.getHeaderMap().entrySet()) {
                headerMap.put(normalizeHeader(entry.getKey()), entry.getValue());
            }

            int rowNum = 1;
            for (CSVRecord record : csvParser) {
                rowNum++;
                String name = getCsvField(record, headerMap, "nombre", "name", "producto");
                String barcode = getCsvField(record, headerMap, "codigobarras", "barcode", "bar_code", "ean", "upc", "codigo");
                String sku = getCsvField(record, headerMap, "sku", "codigosku");
                String desc = getCsvField(record, headerMap, "descripcion", "description", "detalle");
                String category = getCsvField(record, headerMap, "categoria", "category", "rubro");
                String brand = getCsvField(record, headerMap, "marca", "brand");
                String uomStr = getCsvField(record, headerMap, "unidad", "unidadmedida", "uom", "unitofmeasure");
                String minStockStr = getCsvField(record, headerMap, "stockminimo", "minstock", "minstockthreshold");
                String targetStockStr = getCsvField(record, headerMap, "stockoptimo", "stockdeseado", "targetstock", "targetstockquantity");
                String perishableStr = getCsvField(record, headerMap, "perecible", "esperecible", "isperishable", "perishable");

                rows.add(buildProductRow(rowNum, name, desc, sku, barcode, category, brand, uomStr, minStockStr, targetStockStr, perishableStr));
            }
        }
        return rows;
    }

    private List<ProductImportRowDto> parseJson(InputStream is) throws Exception {
        List<ProductImportRowDto> rows = new ArrayList<>();
        JsonNode root = objectMapper.readTree(is);

        JsonNode arrayNode = root.isArray() ? root : (root.has("products") ? root.get("products") : root.get("items"));
        if (arrayNode == null || !arrayNode.isArray()) {
            if (root.isObject()) {
                // Objeto único
                rows.add(parseSingleJsonNode(root, 1));
                return rows;
            }
            return rows;
        }

        int rowNum = 1;
        for (JsonNode node : arrayNode) {
            rows.add(parseSingleJsonNode(node, rowNum++));
        }
        return rows;
    }

    private ProductImportRowDto parseSingleJsonNode(JsonNode node, int rowNum) {
        String name = getJsonText(node, "name", "nombre", "producto");
        String barcode = getJsonText(node, "barCode", "barcode", "bar_code", "codigoBarras", "codigo");
        String sku = getJsonText(node, "sku", "codigoSku");
        String desc = getJsonText(node, "description", "descripcion", "detalle");
        String category = getJsonText(node, "categoryName", "category", "categoria");
        String brand = getJsonText(node, "brandName", "brand", "marca");
        String uomStr = getJsonText(node, "unitOfMeasure", "uom", "unidad");
        String minStockStr = getJsonText(node, "minStockThreshold", "minStock", "stockMinimo");
        String targetStockStr = getJsonText(node, "targetStockQuantity", "targetStock", "stockOptimo");
        String perishableStr = getJsonText(node, "isPerishable", "perishable", "perecible");

        return buildProductRow(rowNum, name, desc, sku, barcode, category, brand, uomStr, minStockStr, targetStockStr, perishableStr);
    }

    private ProductImportRowDto buildProductRow(
            int rowNum, String name, String desc, String sku, String barcode,
            String category, String brand, String uomStr, String minStockStr,
            String targetStockStr, String perishableStr) {

        UnitOfMeasure uom = parseUnitOfMeasure(uomStr);
        Double minStock = parseDouble(minStockStr, 1.0);
        Double targetStock = parseDouble(targetStockStr, 3.0);
        Boolean isPerishable = parseBoolean(perishableStr, true);

        return ProductImportRowDto.builder()
            .rowNumber(rowNum)
            .name(name != null ? name.trim() : null)
            .description(desc != null ? desc.trim() : null)
            .sku(sku != null && !sku.isBlank() ? sku.trim().toUpperCase() : null)
            .barCode(barcode != null ? barcode.trim() : null)
            .categoryName(category != null && !category.isBlank() ? category.trim() : null)
            .brandName(brand != null && !brand.isBlank() ? brand.trim() : null)
            .unitOfMeasure(uom)
            .minStockThreshold(minStock)
            .targetStockQuantity(targetStock)
            .isPerishable(isPerishable)
            .build();
    }

    private String getField(Row r, Map<String, Integer> colMap, String... candidates) {
        for (String c : candidates) {
            Integer idx = colMap.get(normalizeHeader(c));
            if (idx != null) {
                Cell cell = r.getCell(idx);
                return getCellValueAsString(cell);
            }
        }
        return null;
    }

    private String getCsvField(CSVRecord record, Map<String, Integer> headerMap, String... candidates) {
        for (String c : candidates) {
            Integer idx = headerMap.get(normalizeHeader(c));
            if (idx != null && idx < record.size()) {
                return record.get(idx);
            }
        }
        return null;
    }

    private String getJsonText(JsonNode node, String... fields) {
        for (String f : fields) {
            if (node.has(f) && !node.get(f).isNull()) {
                return node.get(f).asText();
            }
        }
        return null;
    }

    public static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        CellType type = cell.getCellType() == CellType.FORMULA ? cell.getCachedFormulaResultType() : cell.getCellType();
        return switch (type) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    LocalDate ld = cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    yield ld.toString();
                } else {
                    double num = cell.getNumericCellValue();
                    if (num == Math.floor(num)) {
                        yield String.valueOf((long) num);
                    }
                    yield String.valueOf(num);
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK && !getCellValueAsString(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String normalizeHeader(String header) {
        if (header == null) return "";
        return header.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private UnitOfMeasure parseUnitOfMeasure(String text) {
        if (text == null || text.isBlank()) return UnitOfMeasure.UNIT;
        String clean = text.trim().toUpperCase();
        for (UnitOfMeasure u : UnitOfMeasure.values()) {
            if (u.name().equalsIgnoreCase(clean) || u.getSymbol().equalsIgnoreCase(clean) || u.getDisplayName().equalsIgnoreCase(clean)) {
                return u;
            }
        }
        return UnitOfMeasure.UNIT;
    }

    private Double parseDouble(String text, Double defaultValue) {
        if (text == null || text.isBlank()) return defaultValue;
        try {
            return Double.parseDouble(text.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Boolean parseBoolean(String text, Boolean defaultValue) {
        if (text == null || text.isBlank()) return defaultValue;
        String clean = text.trim().toLowerCase();
        if (clean.equals("si") || clean.equals("sí") || clean.equals("true") || clean.equals("1") || clean.equals("yes")) {
            return true;
        }
        if (clean.equals("no") || clean.equals("false") || clean.equals("0")) {
            return false;
        }
        return defaultValue;
    }
}
