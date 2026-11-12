package cl.diego.NanoStock.dataexport.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.diego.NanoStock.common.data.DataFormat;
import cl.diego.NanoStock.dataexport.dto.ExportDataResult;
import cl.diego.NanoStock.dataexport.dto.InventoryExportDto;
import cl.diego.NanoStock.dataexport.dto.InventoryExportFilter;
import cl.diego.NanoStock.inventory.model.StockBatch;
import cl.diego.NanoStock.inventory.repository.StockBatchRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryExportService {

    private final StockBatchRepository stockBatchRepository;
    private final TabularDataExporter tabularExporter;

    private static final String[] HEADERS = {
        "ID", "Ubicacion", "TipoAlmacenamiento", "Producto", "CodigoBarras", "SKU",
        "Cantidad", "UnidadMedida", "FechaVencimiento", "DiasRestantes",
        "EstadoSellado", "CodigoLote", "FechaIngreso"
    };

    @Transactional(readOnly = true)
    public ExportDataResult exportInventory(InventoryExportFilter filter, DataFormat format) throws Exception {
        Specification<StockBatch> spec = buildSpecification(filter);
        List<StockBatch> batches = stockBatchRepository.findAll(spec);

        LocalDate today = LocalDate.now();
        List<InventoryExportDto> dtoList = new ArrayList<>();
        for (StockBatch b : batches) {
            Long daysRemaining = b.getExpirationDate() != null
                ? ChronoUnit.DAYS.between(today, b.getExpirationDate())
                : null;

            dtoList.add(InventoryExportDto.builder()
                .id(b.getId())
                .locationName(b.getLocation() != null ? b.getLocation().getName() : "")
                .storageType(b.getLocation() != null ? b.getLocation().getStorageType() : null)
                .productName(b.getProduct() != null ? b.getProduct().getName() : "")
                .barCode(b.getProduct() != null ? b.getProduct().getBarCode() : "")
                .sku(b.getProduct() != null ? b.getProduct().getSku() : "")
                .quantity(b.getQuantity())
                .unitOfMeasure(b.getProduct() != null ? b.getProduct().getUnitOfMeasure() : null)
                .expirationDate(b.getExpirationDate())
                .daysRemaining(daysRemaining)
                .sealedStatus(b.getSealedStatus())
                .batchCode(b.getBatchCode())
                .createdAt(b.getCreatedAt())
                .build());
        }

        byte[] content;
        if (format == DataFormat.JSON) {
            content = tabularExporter.exportToJson(dtoList);
        } else {
            List<List<Object>> rows = new ArrayList<>();
            for (InventoryExportDto dto : dtoList) {
                List<Object> row = new ArrayList<>();
                row.add(dto.getId());
                row.add(dto.getLocationName());
                row.add(dto.getStorageType() != null ? dto.getStorageType().name() : "");
                row.add(dto.getProductName());
                row.add(dto.getBarCode());
                row.add(dto.getSku());
                row.add(dto.getQuantity());
                row.add(dto.getUnitOfMeasure() != null ? dto.getUnitOfMeasure().name() : "");
                row.add(dto.getExpirationDate());
                row.add(dto.getDaysRemaining());
                row.add(dto.getSealedStatus() != null ? dto.getSealedStatus().name() : "");
                row.add(dto.getBatchCode());
                row.add(dto.getCreatedAt());
                rows.add(row);
            }

            content = format == DataFormat.EXCEL
                ? tabularExporter.exportToExcel("Inventario", HEADERS, rows)
                : tabularExporter.exportToCsv(HEADERS, rows);
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "inventario_export_" + timestamp + "." + format.getFileExtension();

        return ExportDataResult.builder()
            .data(content)
            .filename(filename)
            .contentType(format.getMediaType())
            .recordCount(dtoList.size())
            .build();
    }

    private Specification<StockBatch> buildSpecification(InventoryExportFilter filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.greaterThan(root.get("quantity"), 0.0);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (Boolean.TRUE.equals(filter.getOnlyInStock())) {
                predicates.add(cb.greaterThan(root.get("quantity"), 0.0));
            }

            if (filter.getLocationId() != null) {
                predicates.add(cb.equal(root.get("location").get("id"), filter.getLocationId()));
            }

            if (filter.getProductId() != null) {
                predicates.add(cb.equal(root.get("product").get("id"), filter.getProductId()));
            }

            if (filter.getExpiringBefore() != null) {
                predicates.add(cb.and(
                    cb.isNotNull(root.get("expirationDate")),
                    cb.lessThanOrEqualTo(root.get("expirationDate"), filter.getExpiringBefore())
                ));
            }

            if (filter.getSealedStatus() != null) {
                predicates.add(cb.equal(root.get("sealedStatus"), filter.getSealedStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
