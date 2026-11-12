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
public class UpdateProductRequest {

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String name;

    private String description;

    private String sku;

    @NotBlank(message = "El código de barras es obligatorio")
    private String barCode;

    private Long categoryId;

    private Long brandId;

    private UnitOfMeasure unitOfMeasure;

    private Double minStockThreshold;

    private Double targetStockQuantity;

    private Boolean isPerishable;
}
