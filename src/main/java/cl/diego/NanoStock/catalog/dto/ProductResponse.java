package cl.diego.NanoStock.catalog.dto;

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
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private String sku;
    private String barCode;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private UnitOfMeasure unitOfMeasure;
    private Double minStockThreshold;
    private Double targetStockQuantity;
    private boolean isPerishable;
    private LocalDateTime createdAt;
}
