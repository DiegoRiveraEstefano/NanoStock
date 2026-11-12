package cl.diego.NanoStock.catalog.dto;

import cl.diego.NanoStock.catalog.model.UnitOfMeasure;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String name;

    private String description;

    private String sku;

    @NotBlank(message = "El código de barras es obligatorio")
    private String barCode;

    private Long categoryId;

    private Long brandId;

    @Builder.Default
    private UnitOfMeasure unitOfMeasure = UnitOfMeasure.UNIT;

    @Builder.Default
    private Double minStockThreshold = 1.0;

    @Builder.Default
    private Double targetStockQuantity = 3.0;

    @Builder.Default
    private Boolean isPerishable = true;
}
