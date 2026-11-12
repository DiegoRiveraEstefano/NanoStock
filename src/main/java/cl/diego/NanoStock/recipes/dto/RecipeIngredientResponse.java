package cl.diego.NanoStock.recipes.dto;

import cl.diego.NanoStock.catalog.model.UnitOfMeasure;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredientResponse {

    private Long id;
    private Long productId;
    private String productName;
    private Double quantity;
    private UnitOfMeasure unitOfMeasure;
    private boolean isOptional;
    private String notes;
}
