package cl.diego.NanoStock.dataimport.service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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

import cl.diego.NanoStock.common.data.DataFormat;
import cl.diego.NanoStock.dataimport.dto.InventoryImportRowDto;
import cl.diego.NanoStock.inventory.model.SealedStatus;
import cl.diego.NanoStock.inventory.model.StorageType;
import lombok.RequiredArgsConstructor;

@Component
public class InventoryDataParser {

    private final ObjectMapper objectMapper;

    public InventoryDataParser(@org.springframework.beans.factory.annotation.Autowired(required = false) ObjectMapper objectMapper) {
        if (objectMapper != null) {
            this.objectMapper = objectMapper;
        } else {
            this.objectMapper = new ObjectMapper().findAndRegisterModules();
        }
    }
    private static final DateTimeFormatter[] DATE_FORMATTERS = new DateTimeFormatter[]{
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy")
    };

    public List<InventoryImportRowDto> parse(InputStream inputStream, DataFormat format) throws Exception {
        return switch (format) {
            case EXCEL -> parseExcel(inputStream);
            case CSV -> parseCsv(inputStream);
            case JSON -> parseJson(inputStream);
        };
    }

    private List<InventoryImportRowDto> parseExcel(InputStream is) throws Exception {
        List<InventoryImportRowDto> rows = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            if (!rowIterator.hasNext()) {
                return rows;
            }

            Row headerRow = rowIterator.next();
            Map<String, Integer> colMap = new HashMap<>();
            for (Cell cell : headerRow) {
                String header = normalizeHeader(ProductDataParser.getCellValueAsString(cell));
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

                String identifier = getField(r, colMap, "codigobarras", "barcode", "sku", "producto", "product", "productidentifier");
                String location = getField(r, colMap, "ubicacion", "location", "lugar", "zona");
                String storageType = getField(r, colMap, "tipoalmacenamiento", "storagetype", "conservacion");
                String qtyStr = getField(r, colMap, "cantidad", "quantity", "qty");
                String expDateStr = getField(r, colMap, "fechavencimiento", "vencimiento", "expirationdate", "caducidad");
                String sealedStr = getField(r, colMap, "estado", "sealedstatus", "sellado");
                String batchCode = getField(r, colMap, "lote", "batchcode", "codigolote", "batch");

                rows.add(buildInventoryRow(rowNum, identifier, location, storageType, qtyStr, expDateStr, sealedStr, batchCode));
            }
        }
        return rows;
    }

    private List<InventoryImportRowDto> parseCsv(InputStream is) throws Exception {
        List<InventoryImportRowDto> rows = new ArrayList<>();
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
                String identifier = getCsvField(record, headerMap, "codigobarras", "barcode", "sku", "producto", "product", "productidentifier");
                String location = getCsvField(record, headerMap, "ubicacion", "location", "lugar", "zona");
                String storageType = getCsvField(record, headerMap, "tipoalmacenamiento", "storagetype", "conservacion");
                String qtyStr = getCsvField(record, headerMap, "cantidad", "quantity", "qty");
                String expDateStr = getCsvField(record, headerMap, "fechavencimiento", "vencimiento", "expirationdate", "caducidad");
                String sealedStr = getCsvField(record, headerMap, "estado", "sealedstatus", "sellado");
                String batchCode = getCsvField(record, headerMap, "lote", "batchcode", "codigolote", "batch");

                rows.add(buildInventoryRow(rowNum, identifier, location, storageType, qtyStr, expDateStr, sealedStr, batchCode));
            }
        }
        return rows;
    }

    private List<InventoryImportRowDto> parseJson(InputStream is) throws Exception {
        List<InventoryImportRowDto> rows = new ArrayList<>();
        JsonNode root = objectMapper.readTree(is);

        JsonNode arrayNode = root.isArray() ? root : (root.has("inventory") ? root.get("inventory") : root.get("items"));
        if (arrayNode == null || !arrayNode.isArray()) {
            if (root.isObject()) {
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

    private InventoryImportRowDto parseSingleJsonNode(JsonNode node, int rowNum) {
        String identifier = getJsonText(node, "productIdentifier", "barCode", "barcode", "sku", "product");
        String location = getJsonText(node, "locationName", "location", "ubicacion");
        String storageType = getJsonText(node, "storageType", "tipoAlmacenamiento");
        String qtyStr = getJsonText(node, "quantity", "cantidad");
        String expDateStr = getJsonText(node, "expirationDate", "vencimiento", "fechaVencimiento");
        String sealedStr = getJsonText(node, "sealedStatus", "estado", "sellado");
        String batchCode = getJsonText(node, "batchCode", "lote", "codigoLote");

        return buildInventoryRow(rowNum, identifier, location, storageType, qtyStr, expDateStr, sealedStr, batchCode);
    }

    private InventoryImportRowDto buildInventoryRow(
            int rowNum, String identifier, String location, String storageTypeStr,
            String qtyStr, String expDateStr, String sealedStr, String batchCode) {

        Double qty = null;
        if (qtyStr != null && !qtyStr.isBlank()) {
            try {
                qty = Double.parseDouble(qtyStr.trim().replace(',', '.'));
            } catch (NumberFormatException ignored) {}
        }

        LocalDate expDate = parseLocalDate(expDateStr);
        StorageType storageType = parseStorageType(storageTypeStr);
        SealedStatus sealedStatus = parseSealedStatus(sealedStr);

        return InventoryImportRowDto.builder()
            .rowNumber(rowNum)
            .productIdentifier(identifier != null ? identifier.trim() : null)
            .locationName(location != null ? location.trim() : null)
            .storageType(storageType)
            .quantity(qty)
            .expirationDate(expDate)
            .sealedStatus(sealedStatus)
            .batchCode(batchCode != null && !batchCode.isBlank() ? batchCode.trim() : null)
            .build();
    }

    private String getField(Row r, Map<String, Integer> colMap, String... candidates) {
        for (String c : candidates) {
            Integer idx = colMap.get(normalizeHeader(c));
            if (idx != null) {
                Cell cell = r.getCell(idx);
                return ProductDataParser.getCellValueAsString(cell);
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

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK && !ProductDataParser.getCellValueAsString(cell).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String normalizeHeader(String header) {
        if (header == null) return "";
        return header.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private LocalDate parseLocalDate(String text) {
        if (text == null || text.isBlank()) return null;
        String clean = text.trim();
        for (DateTimeFormatter dtf : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(clean, dtf);
            } catch (DateTimeParseException ignored) {}
        }
        return null;
    }

    private StorageType parseStorageType(String text) {
        if (text == null || text.isBlank()) return StorageType.AMBIENT;
        String clean = text.trim().toUpperCase();
        for (StorageType st : StorageType.values()) {
            if (st.name().equalsIgnoreCase(clean)) {
                return st;
            }
        }
        if (clean.contains("REFRIG") || clean.contains("HELAD")) return StorageType.REFRIGERATED;
        if (clean.contains("CONG") || clean.contains("FREEZ")) return StorageType.FROZEN;
        return StorageType.AMBIENT;
    }

    private SealedStatus parseSealedStatus(String text) {
        if (text == null || text.isBlank()) return SealedStatus.SEALED;
        String clean = text.trim().toUpperCase();
        if (clean.contains("ABIERTO") || clean.contains("OPEN")) {
            return SealedStatus.OPEN;
        }
        return SealedStatus.SEALED;
    }
}
