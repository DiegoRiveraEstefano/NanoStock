package cl.diego.NanoStock.dataexport.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.diego.NanoStock.common.data.DataFormat;
import cl.diego.NanoStock.dataexport.dto.ExportDataResult;
import cl.diego.NanoStock.dataexport.dto.MovementExportDto;
import cl.diego.NanoStock.dataexport.dto.MovementExportFilter;
import cl.diego.NanoStock.inventory.model.InventoryMovement;
import cl.diego.NanoStock.inventory.repository.InventoryMovementRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovementExportService {

    private final InventoryMovementRepository movementRepository;
    private final TabularDataExporter tabularExporter;

    private static final String[] HEADERS = {
        "ID", "FechaMovimiento", "TipoMovimiento", "Producto", "CodigoBarras",
        "Cantidad", "Motivo", "CodigoLote"
    };

    @Transactional(readOnly = true)
    public ExportDataResult exportMovements(MovementExportFilter filter, DataFormat format) throws Exception {
        Specification<InventoryMovement> spec = buildSpecification(filter);
        List<InventoryMovement> movements = movementRepository.findAll(spec);

        List<MovementExportDto> dtoList = new ArrayList<>();
        for (InventoryMovement m : movements) {
            String reason = (m.getNotes() != null && !m.getNotes().isBlank())
                ? m.getNotes()
                : (m.getWasteReason() != null ? m.getWasteReason() : "");
            String batchCode = m.getBatch() != null ? m.getBatch().getBatchCode() : "";

            dtoList.add(MovementExportDto.builder()
                .id(m.getId())
                .movementDate(m.getCreatedAt())
                .movementType(m.getMovementType())
                .productName(m.getProduct() != null ? m.getProduct().getName() : "")
                .barCode(m.getProduct() != null ? m.getProduct().getBarCode() : "")
                .quantity(m.getQuantity())
                .reason(reason)
                .batchCode(batchCode)
                .build());
        }

        byte[] content;
        if (format == DataFormat.JSON) {
            content = tabularExporter.exportToJson(dtoList);
        } else {
            List<List<Object>> rows = new ArrayList<>();
            for (MovementExportDto dto : dtoList) {
                List<Object> row = new ArrayList<>();
                row.add(dto.getId());
                row.add(dto.getMovementDate());
                row.add(dto.getMovementType() != null ? dto.getMovementType().name() : "");
                row.add(dto.getProductName());
                row.add(dto.getBarCode());
                row.add(dto.getQuantity());
                row.add(dto.getReason());
                row.add(dto.getBatchCode());
                rows.add(row);
            }

            content = format == DataFormat.EXCEL
                ? tabularExporter.exportToExcel("Movimientos", HEADERS, rows)
                : tabularExporter.exportToCsv(HEADERS, rows);
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "movimientos_export_" + timestamp + "." + format.getFileExtension();

        return ExportDataResult.builder()
            .data(content)
            .filename(filename)
            .contentType(format.getMediaType())
            .recordCount(dtoList.size())
            .build();
    }

    private Specification<InventoryMovement> buildSpecification(MovementExportFilter filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getMovementType() != null) {
                predicates.add(cb.equal(root.get("movementType"), filter.getMovementType()));
            }

            if (filter.getProductId() != null) {
                predicates.add(cb.equal(root.get("product").get("id"), filter.getProductId()));
            }

            if (filter.getFromDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filter.getFromDate()));
            }

            if (filter.getToDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filter.getToDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
