package cl.diego.NanoStock.dataimport.dto;

import cl.diego.NanoStock.catalog.model.UnitOfMeasure;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImportRowDto {
    private int rowNumber;
    private String name;
    private String description;
    private String sku;
    private String barCode;
    private String categoryName;
    private String brandName;
    @Builder.Default
    private UnitOfMeasure unitOfMeasure = UnitOfMeasure.UNIT;
    @Builder.Default
    private Double minStockThreshold = 1.0;
    @Builder.Default
    private Double targetStockQuantity = 3.0;
    @Builder.Default
    private Boolean isPerishable = Boolean.TRUE;
}
