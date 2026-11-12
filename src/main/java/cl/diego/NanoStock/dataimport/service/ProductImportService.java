package cl.diego.NanoStock.dataimport.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.diego.NanoStock.catalog.event.ProductCreatedEvent;
import cl.diego.NanoStock.catalog.event.ProductUpdatedEvent;
import cl.diego.NanoStock.catalog.model.Brand;
import cl.diego.NanoStock.catalog.model.Category;
import cl.diego.NanoStock.catalog.model.Product;
import cl.diego.NanoStock.catalog.model.UnitOfMeasure;
import cl.diego.NanoStock.catalog.repository.BrandRepository;
import cl.diego.NanoStock.catalog.repository.CategoryRepository;
import cl.diego.NanoStock.catalog.repository.ProductRepository;
import cl.diego.NanoStock.common.data.DataFormat;
import cl.diego.NanoStock.dataimport.dto.ImportConflictStrategy;
import cl.diego.NanoStock.dataimport.dto.ImportResultDto;
import cl.diego.NanoStock.dataimport.dto.ImportRowErrorDto;
import cl.diego.NanoStock.dataimport.dto.ProductImportRowDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductImportService {

    private final ProductDataParser parser;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ImportResultDto importProducts(
            InputStream inputStream,
            DataFormat format,
            boolean dryRun,
            ImportConflictStrategy conflictStrategy,
            boolean createMissingMetadata) {

        List<ProductImportRowDto> rows;
        try {
            rows = parser.parse(inputStream, format);
        } catch (Exception e) {
            log.error("Error al parsear archivo de productos: {}", e.getMessage(), e);
            return ImportResultDto.builder()
                .entityType("PRODUCT")
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
        int skippedCount = 0;

        Set<String> seenBarcodes = new HashSet<>();
        Set<String> seenSkus = new HashSet<>();

        for (ProductImportRowDto row : rows) {
            int rowNum = row.getRowNumber();

            // 1. Validaciones básicas de campos obligatorios
            if (row.getName() == null || row.getName().isBlank()) {
                errors.add(ImportRowErrorDto.builder()
                    .rowNumber(rowNum)
                    .identifier(row.getBarCode())
                    .field("name")
                    .errorMessage("El nombre del producto es obligatorio.")
                    .rejectedValue(row.getName())
                    .build());
                continue;
            }

            if (row.getBarCode() == null || row.getBarCode().isBlank()) {
                errors.add(ImportRowErrorDto.builder()
                    .rowNumber(rowNum)
                    .identifier(row.getName())
                    .field("barCode")
                    .errorMessage("El código de barras es obligatorio.")
                    .rejectedValue(row.getBarCode())
                    .build());
                continue;
            }

            String cleanBarcode = row.getBarCode().trim();
            if (seenBarcodes.contains(cleanBarcode)) {
                errors.add(ImportRowErrorDto.builder()
                    .rowNumber(rowNum)
                    .identifier(cleanBarcode)
                    .field("barCode")
                    .errorMessage("Código de barras duplicado dentro del mismo archivo: " + cleanBarcode)
                    .rejectedValue(cleanBarcode)
                    .build());
                continue;
            }
            seenBarcodes.add(cleanBarcode);

            String sku = row.getSku() != null && !row.getSku().isBlank()
                ? row.getSku().trim().toUpperCase(Locale.ROOT)
                : generateSku(row.getName());

            if (seenSkus.contains(sku)) {
                sku = sku + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase(Locale.ROOT);
            }
            seenSkus.add(sku);

            if (row.getMinStockThreshold() != null && row.getMinStockThreshold() < 0) {
                errors.add(ImportRowErrorDto.builder()
                    .rowNumber(rowNum)
                    .identifier(cleanBarcode)
                    .field("minStockThreshold")
                    .errorMessage("El stock mínimo no puede ser negativo.")
                    .rejectedValue(String.valueOf(row.getMinStockThreshold()))
                    .build());
                continue;
            }

            // 2. Comprobar existencia previa en BD
            Optional<Product> existingOpt = productRepository.findByBarCode(cleanBarcode);
            if (existingOpt.isEmpty() && productRepository.existsBySku(sku)) {
                existingOpt = productRepository.findBySku(sku);
            }

            if (existingOpt.isPresent()) {
                Product existing = existingOpt.get();
                if (conflictStrategy == ImportConflictStrategy.FAIL) {
                    errors.add(ImportRowErrorDto.builder()
                        .rowNumber(rowNum)
                        .identifier(cleanBarcode)
                        .field("barCode")
                        .errorMessage("Ya existe un producto registrado con código de barras " + cleanBarcode + " (ID: " + existing.getId() + ")")
                        .rejectedValue(cleanBarcode)
                        .build());
                    continue;
                } else if (conflictStrategy == ImportConflictStrategy.SKIP) {
                    skippedCount++;
                    continue;
                } else if (conflictStrategy == ImportConflictStrategy.UPDATE) {
                    if (!dryRun) {
                        Category category = resolveCategory(row.getCategoryName(), createMissingMetadata);
                        Brand brand = resolveBrand(row.getBrandName(), createMissingMetadata);

                        existing.setName(row.getName());
                        if (row.getDescription() != null) existing.setDescription(row.getDescription());
                        if (category != null) existing.setCategory(category);
                        if (brand != null) existing.setBrand(brand);
                        if (row.getUnitOfMeasure() != null) existing.setUnitOfMeasure(row.getUnitOfMeasure());
                        if (row.getMinStockThreshold() != null) existing.setMinStockThreshold(row.getMinStockThreshold());
                        if (row.getTargetStockQuantity() != null) existing.setTargetStockQuantity(row.getTargetStockQuantity());
                        if (row.getIsPerishable() != null) existing.setPerishable(row.getIsPerishable());

                        Product updated = productRepository.save(existing);
                        affectedIds.add(updated.getId());
                        eventPublisher.publishEvent(new ProductUpdatedEvent(
                            updated.getId(), updated.getName(), updated.getBarCode(),
                            updated.getSku()
                        ));
                    }
                    successCount++;
                    continue;
                }
            }

            // 3. Creación de nuevo producto
            if (!dryRun) {
                Category category = resolveCategory(row.getCategoryName(), createMissingMetadata);
                Brand brand = resolveBrand(row.getBrandName(), createMissingMetadata);

                Product product = Product.builder()
                    .name(row.getName().trim())
                    .description(row.getDescription())
                    .sku(sku)
                    .barCode(cleanBarcode)
                    .category(category)
                    .brand(brand)
                    .unitOfMeasure(row.getUnitOfMeasure() != null ? row.getUnitOfMeasure() : UnitOfMeasure.UNIT)
                    .minStockThreshold(row.getMinStockThreshold() != null ? row.getMinStockThreshold() : 1.0)
                    .targetStockQuantity(row.getTargetStockQuantity() != null ? row.getTargetStockQuantity() : 3.0)
                    .isPerishable(row.getIsPerishable() != null ? row.getIsPerishable() : true)
                    .build();

                Product saved = productRepository.save(product);
                affectedIds.add(saved.getId());

                eventPublisher.publishEvent(new ProductCreatedEvent(
                    saved.getId(), saved.getName(), saved.getBarCode(),
                    saved.getSku(), saved.getMinStockThreshold(), saved.getTargetStockQuantity()
                ));
            }
            successCount++;
        }

        String mode = dryRun ? "[SIMULACIÓN / DRY-RUN] " : "";
        String summary = String.format("%sImportación completada: %d procesados exitosamente, %d omitidos, %d errores de un total de %d filas.",
            mode, successCount, skippedCount, errors.size(), rows.size());

        return ImportResultDto.builder()
            .entityType("PRODUCT")
            .totalRows(rows.size())
            .successCount(successCount)
            .skippedCount(skippedCount)
            .errorCount(errors.size())
            .dryRun(dryRun)
            .errors(errors)
            .affectedIds(affectedIds)
            .summaryMessage(summary)
            .build();
    }

    private Category resolveCategory(String name, boolean createIfMissing) {
        if (name == null || name.isBlank()) return null;
        String clean = name.trim();
        Optional<Category> opt = categoryRepository.findByName(clean);
        if (opt.isPresent()) return opt.get();
        if (createIfMissing) {
            Category newCat = Category.builder().name(clean).description("Creada automáticamente en importación").build();
            return categoryRepository.save(newCat);
        }
        return null;
    }

    private Brand resolveBrand(String name, boolean createIfMissing) {
        if (name == null || name.isBlank()) return null;
        String clean = name.trim();
        Optional<Brand> opt = brandRepository.findByName(clean);
        if (opt.isPresent()) return opt.get();
        if (createIfMissing) {
            Brand newBrand = Brand.builder().name(clean).build();
            return brandRepository.save(newBrand);
        }
        return null;
    }

    private String generateSku(String productName) {
        String base = (productName != null && productName.length() >= 3)
            ? productName.substring(0, 3).toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "X")
            : "PRD";
        return base + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }
}
