package cl.diego.NanoStock.dataexport.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.diego.NanoStock.common.data.DataFormat;
import cl.diego.NanoStock.dataexport.dto.ExportDataResult;
import cl.diego.NanoStock.dataexport.dto.InventoryExportFilter;
import cl.diego.NanoStock.dataexport.dto.MovementExportFilter;
import cl.diego.NanoStock.dataexport.dto.ProductExportFilter;
import cl.diego.NanoStock.dataexport.service.InventoryExportService;
import cl.diego.NanoStock.dataexport.service.MovementExportService;
import cl.diego.NanoStock.dataexport.service.ProductExportService;
import cl.diego.NanoStock.inventory.model.MovementType;
import cl.diego.NanoStock.inventory.model.SealedStatus;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class DataExportController {

    private final ProductExportService productExportService;
    private final InventoryExportService inventoryExportService;
    private final MovementExportService movementExportService;

    // --- Exportación de Productos ---

    @GetMapping("/products")
    public ResponseEntity<byte[]> exportProducts(
            @RequestParam(value = "format", defaultValue = "EXCEL") String formatStr,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "brandId", required = false) Long brandId,
            @RequestParam(value = "isPerishable", required = false) Boolean isPerishable,
            @RequestParam(value = "onlyLowStock", required = false) Boolean onlyLowStock) throws Exception {

        DataFormat format = DataFormat.fromString(formatStr).orElse(DataFormat.EXCEL);
        ProductExportFilter filter = ProductExportFilter.builder()
            .search(search)
            .categoryId(categoryId)
            .brandId(brandId)
            .isPerishable(isPerishable)
            .onlyLowStock(onlyLowStock)
            .build();

        return buildExportResponse(productExportService.exportProducts(filter, format));
    }

    @PostMapping("/products")
    public ResponseEntity<byte[]> exportProductsWithFilter(
            @RequestParam(value = "format", defaultValue = "EXCEL") String formatStr,
            @RequestBody(required = false) ProductExportFilter filter) throws Exception {

        DataFormat format = DataFormat.fromString(formatStr).orElse(DataFormat.EXCEL);
        return buildExportResponse(productExportService.exportProducts(filter, format));
    }

    // --- Exportación de Inventario y Lotes ---

    @GetMapping("/inventory")
    public ResponseEntity<byte[]> exportInventory(
            @RequestParam(value = "format", defaultValue = "EXCEL") String formatStr,
            @RequestParam(value = "locationId", required = false) Long locationId,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "expiringBefore", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiringBefore,
            @RequestParam(value = "sealedStatus", required = false) SealedStatus sealedStatus,
            @RequestParam(value = "onlyInStock", defaultValue = "true") Boolean onlyInStock) throws Exception {

        DataFormat format = DataFormat.fromString(formatStr).orElse(DataFormat.EXCEL);
        InventoryExportFilter filter = InventoryExportFilter.builder()
            .locationId(locationId)
            .productId(productId)
            .expiringBefore(expiringBefore)
            .sealedStatus(sealedStatus)
            .onlyInStock(onlyInStock)
            .build();

        return buildExportResponse(inventoryExportService.exportInventory(filter, format));
    }

    @PostMapping("/inventory")
    public ResponseEntity<byte[]> exportInventoryWithFilter(
            @RequestParam(value = "format", defaultValue = "EXCEL") String formatStr,
            @RequestBody(required = false) InventoryExportFilter filter) throws Exception {

        DataFormat format = DataFormat.fromString(formatStr).orElse(DataFormat.EXCEL);
        return buildExportResponse(inventoryExportService.exportInventory(filter, format));
    }

    // --- Exportación de Movimientos / Auditoría ---

    @GetMapping("/movements")
    public ResponseEntity<byte[]> exportMovements(
            @RequestParam(value = "format", defaultValue = "EXCEL") String formatStr,
            @RequestParam(value = "movementType", required = false) MovementType movementType,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) throws Exception {

        DataFormat format = DataFormat.fromString(formatStr).orElse(DataFormat.EXCEL);
        MovementExportFilter filter = MovementExportFilter.builder()
            .movementType(movementType)
            .productId(productId)
            .fromDate(fromDate)
            .toDate(toDate)
            .build();

        return buildExportResponse(movementExportService.exportMovements(filter, format));
    }

    @PostMapping("/movements")
    public ResponseEntity<byte[]> exportMovementsWithFilter(
            @RequestParam(value = "format", defaultValue = "EXCEL") String formatStr,
            @RequestBody(required = false) MovementExportFilter filter) throws Exception {

        DataFormat format = DataFormat.fromString(formatStr).orElse(DataFormat.EXCEL);
        return buildExportResponse(movementExportService.exportMovements(filter, format));
    }

    private ResponseEntity<byte[]> buildExportResponse(ExportDataResult result) {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.getFilename() + "\"")
            .header("X-Total-Count", String.valueOf(result.getRecordCount()))
            .contentType(MediaType.parseMediaType(result.getContentType()))
            .body(result.getData());
    }
}
