package cl.diego.NanoStock.recipes.dto;

import cl.diego.NanoStock.catalog.model.UnitOfMeasure;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredientRequest {

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productId;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private Double quantity;

    private UnitOfMeasure unitOfMeasure;

    private boolean isOptional;

    private String notes;
}
