package cl.diego.NanoStock.dataimport.service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.diego.NanoStock.catalog.model.Product;
import cl.diego.NanoStock.catalog.repository.ProductRepository;
import cl.diego.NanoStock.common.data.DataFormat;
import cl.diego.NanoStock.dataimport.dto.ImportResultDto;
import cl.diego.NanoStock.dataimport.dto.ImportRowErrorDto;
import cl.diego.NanoStock.dataimport.dto.InventoryImportRowDto;
import cl.diego.NanoStock.inventory.event.StockLevelChangedEvent;
import cl.diego.NanoStock.inventory.model.InventoryMovement;
import cl.diego.NanoStock.inventory.model.Location;
import cl.diego.NanoStock.inventory.model.MovementType;
import cl.diego.NanoStock.inventory.model.SealedStatus;
import cl.diego.NanoStock.inventory.model.StockBatch;
import cl.diego.NanoStock.inventory.model.StorageType;
import cl.diego.NanoStock.inventory.repository.InventoryMovementRepository;
import cl.diego.NanoStock.inventory.repository.LocationRepository;
import cl.diego.NanoStock.inventory.repository.StockBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryImportService {

    private final InventoryDataParser parser;
    private final ProductRepository productRepository;
    private final LocationRepository locationRepository;
    private final StockBatchRepository stockBatchRepository;
    private final InventoryMovementRepository movementRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ImportResultDto importInventory(
            InputStream inputStream,
            DataFormat format,
            boolean dryRun,
            boolean createMissingLocations) {

        List<InventoryImportRowDto> rows;
        try {
            rows = parser.parse(inputStream, format);
        } catch (Exception e) {
            log.error("Error al parsear archivo de inventario: {}", e.getMessage(), e);
            return ImportResultDto.builder()
                .entityType("INVENTORY")
                .dryRun(dryRun)
                .totalRows(0)
                .errorCount(1)
                .errors(List.of(ImportRowErrorDto.builder()
                    .rowNumber(0)
                    .field("file")
                    .errorMessage("Formato inválido o archivo dañado: " + e.getMessage())
                    .build()))
                .summaryMessage("Error al procesar el archivo.")
                .build();
        }

        List<ImportRowErrorDto> errors = new ArrayList<>();
        List<Long> affectedIds = new ArrayList<>();
        int successCount = 0;

        for (InventoryImportRowDto row : rows) {
            int rowNum = row.getRowNumber();

            // 1. Validar identificador de producto
            if (row.getProductIdentifier() == null || row.getProductIdentifier().isBlank()) {
                errors.add(ImportRowErrorDto.builder()
                    .rowNumber(rowNum)
                    .field("productIdentifier")
                    .errorMessage("El código de barras, SKU o nombre del producto es obligatorio.")
                    .build());
                continue;
            }

            String identifier = row.getProductIdentifier().trim();
            Optional<Product> productOpt = findProduct(identifier);
            if (productOpt.isEmpty()) {
                errors.add(ImportRowErrorDto.builder()
                    .rowNumber(rowNum)
                    .identifier(identifier)
                    .field("productIdentifier")
                    .errorMessage("Producto no encontrado en el catálogo con identificador: " + identifier)
                    .rejectedValue(identifier)
                    .build());
                continue;
            }
            Product product = productOpt.get();

            // 2. Validar cantidad
            if (row.getQuantity() == null || row.getQuantity() <= 0) {
                errors.add(ImportRowErrorDto.builder()
                    .rowNumber(rowNum)
                    .identifier(identifier)
                    .field("quantity")
                    .errorMessage("La cantidad debe ser un valor numérico positivo mayor a cero.")
                    .rejectedValue(String.valueOf(row.getQuantity()))
                    .build());
                continue;
            }

            // 3. Validar / Resolver ubicación
            if (row.getLocationName() == null || row.getLocationName().isBlank()) {
                errors.add(ImportRowErrorDto.builder()
                    .rowNumber(rowNum)
                    .identifier(identifier)
                    .field("locationName")
                    .errorMessage("El nombre de la ubicación física es obligatorio.")
                    .build());
                continue;
            }

            String locName = row.getLocationName().trim();
            Optional<Location> locOpt = locationRepository.findByNameIgnoreCase(locName);
            Location location;
            if (locOpt.isEmpty()) {
                if (createMissingLocations) {
                    if (!dryRun) {
                        Location newLoc = Location.builder()
                            .name(locName)
                            .storageType(row.getStorageType() != null ? row.getStorageType() : StorageType.AMBIENT)
                            .description("Ubicación creada durante importación de inventario")
                            .build();
                        location = locationRepository.save(newLoc);
                    } else {
                        location = null;
                    }
                } else {
                    errors.add(ImportRowErrorDto.builder()
                        .rowNumber(rowNum)
                        .identifier(identifier)
                        .field("locationName")
                        .errorMessage("La ubicación '" + locName + "' no existe en el sistema.")
                        .rejectedValue(locName)
                        .build());
                    continue;
                }
            } else {
                location = locOpt.get();
            }

            // 4. Registro físico del lote e historial
            if (!dryRun && location != null) {
                String batchCode = row.getBatchCode() != null && !row.getBatchCode().isBlank()
                    ? row.getBatchCode().trim()
                    : "IMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                StockBatch batch = StockBatch.builder()
                    .product(product)
                    .location(location)
                    .quantity(row.getQuantity())
                    .expirationDate(row.getExpirationDate())
                    .sealedStatus(row.getSealedStatus() != null ? row.getSealedStatus() : SealedStatus.SEALED)
                    .batchCode(batchCode)
                    .build();

                StockBatch savedBatch = stockBatchRepository.save(batch);
                affectedIds.add(savedBatch.getId());

                // Bitácora inmutable de movimiento ENTRY
                InventoryMovement movement = InventoryMovement.builder()
                    .product(product)
                    .batch(savedBatch)
                    .location(location)
                    .movementType(MovementType.ENTRY)
                    .quantity(row.getQuantity())
                    .notes("Carga masiva de inventario inicial (" + format.name() + ")")
                    .build();
                movementRepository.save(movement);

                // Notificar nuevo nivel de stock total
                Double newTotal = stockBatchRepository.sumQuantityByProductId(product.getId());
                eventPublisher.publishEvent(new StockLevelChangedEvent(product.getId(), newTotal != null ? newTotal : row.getQuantity()));
            }

            successCount++;
        }

        String mode = dryRun ? "[SIMULACIÓN / DRY-RUN] " : "";
        String summary = String.format("%sImportación de inventario completada: %d lotes procesados exitosamente, %d errores de un total de %d registros.",
            mode, successCount, errors.size(), rows.size());

        return ImportResultDto.builder()
            .entityType("INVENTORY")
            .totalRows(rows.size())
            .successCount(successCount)
            .skippedCount(0)
            .errorCount(errors.size())
            .dryRun(dryRun)
            .errors(errors)
            .affectedIds(affectedIds)
            .summaryMessage(summary)
            .build();
    }

    private Optional<Product> findProduct(String identifier) {
        Optional<Product> byBarcode = productRepository.findByBarCode(identifier);
        if (byBarcode.isPresent()) return byBarcode;

        Optional<Product> bySku = productRepository.findBySku(identifier);
        if (bySku.isPresent()) return bySku;

        return productRepository.findByName(identifier);
    }
}
