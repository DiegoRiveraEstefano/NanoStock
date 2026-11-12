package cl.diego.NanoStock.dataexport.dto;

import java.time.LocalDateTime;

import cl.diego.NanoStock.catalog.model.UnitOfMeasure;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductExportDto {
    private Long id;
    private String name;
    private String description;
    private String sku;
    private String barCode;
    private String categoryName;
    private String brandName;
    private UnitOfMeasure unitOfMeasure;
    private Double minStockThreshold;
    private Double targetStockQuantity;
    private Boolean isPerishable;
    private Double currentStockQuantity;
    private Boolean isLowStock;
    private LocalDateTime createdAt;
}
