package cl.diego.NanoStock.dataimport.controller;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cl.diego.NanoStock.common.data.DataFormat;
import cl.diego.NanoStock.dataimport.dto.ImportConflictStrategy;
import cl.diego.NanoStock.dataimport.dto.ImportResultDto;
import cl.diego.NanoStock.dataimport.service.ImportTemplateService;
import cl.diego.NanoStock.dataimport.service.InventoryImportService;
import cl.diego.NanoStock.dataimport.service.ProductImportService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class DataImportController {

    private final ProductImportService productImportService;
    private final InventoryImportService inventoryImportService;
    private final ImportTemplateService templateService;

    @PostMapping(value = "/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResultDto> importProductsFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "format", required = false) String formatStr,
            @RequestParam(value = "dryRun", defaultValue = "false") boolean dryRun,
            @RequestParam(value = "conflictStrategy", defaultValue = "UPDATE") ImportConflictStrategy conflictStrategy,
            @RequestParam(value = "createMissingMetadata", defaultValue = "true") boolean createMissingMetadata) throws Exception {

        DataFormat format = formatStr != null
            ? DataFormat.fromString(formatStr).orElse(DataFormat.fromFilename(file.getOriginalFilename()))
            : DataFormat.fromFilename(file.getOriginalFilename());

        try (InputStream is = file.getInputStream()) {
            ImportResultDto result = productImportService.importProducts(
                is, format, dryRun, conflictStrategy, createMissingMetadata
            );
            return ResponseEntity.status(result.isSuccessful() ? HttpStatus.OK : HttpStatus.MULTI_STATUS).body(result);
        }
    }

    @PostMapping(value = "/products/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ImportResultDto> importProductsJson(
            @RequestBody String jsonContent,
            @RequestParam(value = "dryRun", defaultValue = "false") boolean dryRun,
            @RequestParam(value = "conflictStrategy", defaultValue = "UPDATE") ImportConflictStrategy conflictStrategy,
            @RequestParam(value = "createMissingMetadata", defaultValue = "true") boolean createMissingMetadata) {

        try (InputStream is = new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8))) {
            ImportResultDto result = productImportService.importProducts(
                is, DataFormat.JSON, dryRun, conflictStrategy, createMissingMetadata
            );
            return ResponseEntity.status(result.isSuccessful() ? HttpStatus.OK : HttpStatus.MULTI_STATUS).body(result);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error procesando JSON de productos: " + e.getMessage());
        }
    }

    @PostMapping(value = "/inventory", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResultDto> importInventoryFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "format", required = false) String formatStr,
            @RequestParam(value = "dryRun", defaultValue = "false") boolean dryRun,
            @RequestParam(value = "createMissingLocations", defaultValue = "true") boolean createMissingLocations) throws Exception {

        DataFormat format = formatStr != null
            ? DataFormat.fromString(formatStr).orElse(DataFormat.fromFilename(file.getOriginalFilename()))
            : DataFormat.fromFilename(file.getOriginalFilename());

        try (InputStream is = file.getInputStream()) {
            ImportResultDto result = inventoryImportService.importInventory(
                is, format, dryRun, createMissingLocations
            );
            return ResponseEntity.status(result.isSuccessful() ? HttpStatus.OK : HttpStatus.MULTI_STATUS).body(result);
        }
    }

    @PostMapping(value = "/inventory/json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ImportResultDto> importInventoryJson(
            @RequestBody String jsonContent,
            @RequestParam(value = "dryRun", defaultValue = "false") boolean dryRun,
            @RequestParam(value = "createMissingLocations", defaultValue = "true") boolean createMissingLocations) {

        try (InputStream is = new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8))) {
            ImportResultDto result = inventoryImportService.importInventory(
                is, DataFormat.JSON, dryRun, createMissingLocations
            );
            return ResponseEntity.status(result.isSuccessful() ? HttpStatus.OK : HttpStatus.MULTI_STATUS).body(result);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error procesando JSON de inventario: " + e.getMessage());
        }
    }

    @GetMapping("/templates/{entity}")
    public ResponseEntity<byte[]> downloadTemplate(
            @PathVariable String entity,
            @RequestParam(value = "format", defaultValue = "EXCEL") String formatStr) throws Exception {

        DataFormat format = DataFormat.fromString(formatStr).orElse(DataFormat.EXCEL);
        byte[] bytes = templateService.generateTemplate(entity, format);

        String filename = "plantilla_" + entity + "." + format.getFileExtension();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.parseMediaType(format.getMediaType()))
            .body(bytes);
    }
}
