package cl.diego.NanoStock.dataexport.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.diego.NanoStock.catalog.model.Product;
import cl.diego.NanoStock.catalog.repository.ProductRepository;
import cl.diego.NanoStock.common.data.DataFormat;
import cl.diego.NanoStock.dataexport.dto.ExportDataResult;
import cl.diego.NanoStock.dataexport.dto.ProductExportDto;
import cl.diego.NanoStock.dataexport.dto.ProductExportFilter;
import cl.diego.NanoStock.inventory.repository.StockBatchRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductExportService {

    private final ProductRepository productRepository;
    private final StockBatchRepository stockBatchRepository;
    private final TabularDataExporter tabularExporter;

    private static final String[] HEADERS = {
        "ID", "Nombre", "Descripcion", "SKU", "CodigoBarras", "Categoria", "Marca",
        "UnidadMedida", "StockMinimo", "StockOptimo", "StockActual",
        "AlertaBajoStock", "EsPerecible", "FechaCreacion"
    };

    @Transactional(readOnly = true)
    public ExportDataResult exportProducts(ProductExportFilter filter, DataFormat format) throws Exception {
        Specification<Product> spec = buildSpecification(filter);
        List<Product> products = productRepository.findAll(spec);

        List<ProductExportDto> dtoList = new ArrayList<>();
        for (Product p : products) {
            Double currentStock = stockBatchRepository.sumQuantityByProductId(p.getId());
            double totalStock = currentStock != null ? currentStock : 0.0;
            boolean lowStock = totalStock <= (p.getMinStockThreshold() != null ? p.getMinStockThreshold() : 0.0);

            if (filter != null && Boolean.TRUE.equals(filter.getOnlyLowStock()) && !lowStock) {
                continue;
            }

            dtoList.add(ProductExportDto.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .sku(p.getSku())
                .barCode(p.getBarCode())
                .categoryName(p.getCategory() != null ? p.getCategory().getName() : "")
                .brandName(p.getBrand() != null ? p.getBrand().getName() : "")
                .unitOfMeasure(p.getUnitOfMeasure())
                .minStockThreshold(p.getMinStockThreshold())
                .targetStockQuantity(p.getTargetStockQuantity())
                .isPerishable(p.isPerishable())
                .currentStockQuantity(totalStock)
                .isLowStock(lowStock)
                .createdAt(p.getCreatedAt())
                .build());
        }

        byte[] content;
        if (format == DataFormat.JSON) {
            content = tabularExporter.exportToJson(dtoList);
        } else {
            List<List<Object>> rows = new ArrayList<>();
            for (ProductExportDto dto : dtoList) {
                List<Object> row = new ArrayList<>();
                row.add(dto.getId());
                row.add(dto.getName());
                row.add(dto.getDescription());
                row.add(dto.getSku());
                row.add(dto.getBarCode());
                row.add(dto.getCategoryName());
                row.add(dto.getBrandName());
                row.add(dto.getUnitOfMeasure() != null ? dto.getUnitOfMeasure().name() : "");
                row.add(dto.getMinStockThreshold());
                row.add(dto.getTargetStockQuantity());
                row.add(dto.getCurrentStockQuantity());
                row.add(dto.getIsLowStock() != null && dto.getIsLowStock() ? "SÍ" : "NO");
                row.add(dto.getIsPerishable() != null && dto.getIsPerishable() ? "SÍ" : "NO");
                row.add(dto.getCreatedAt());
                rows.add(row);
            }

            content = format == DataFormat.EXCEL
                ? tabularExporter.exportToExcel("Productos", HEADERS, rows)
                : tabularExporter.exportToCsv(HEADERS, rows);
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "productos_export_" + timestamp + "." + format.getFileExtension();

        return ExportDataResult.builder()
            .data(content)
            .filename(filename)
            .contentType(format.getMediaType())
            .recordCount(dtoList.size())
            .build();
    }

    private Specification<Product> buildSpecification(ProductExportFilter filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
                String pattern = "%" + filter.getSearch().trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), pattern);
                Predicate barcodeMatch = cb.like(cb.lower(root.get("barCode")), pattern);
                Predicate skuMatch = cb.like(cb.lower(root.get("sku")), pattern);
                predicates.add(cb.or(nameMatch, barcodeMatch, skuMatch));
            }

            if (filter.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.getCategoryId()));
            }

            if (filter.getBrandId() != null) {
                predicates.add(cb.equal(root.get("brand").get("id"), filter.getBrandId()));
            }

            if (filter.getIsPerishable() != null) {
                predicates.add(cb.equal(root.get("isPerishable"), filter.getIsPerishable()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
