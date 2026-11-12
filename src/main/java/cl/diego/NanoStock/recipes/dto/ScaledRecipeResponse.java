package cl.diego.NanoStock.recipes.dto;

import java.util.List;

import cl.diego.NanoStock.catalog.model.UnitOfMeasure;
import cl.diego.NanoStock.recipes.model.PortionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScaledRecipeResponse {

    private Long recipeId;
    private String recipeTitle;
    private PortionType portionType;
    private Integer baseServings;
    private Integer targetServings;
    private Double scalingFactor;
    private boolean canCookTargetServings;
    private List<ScaledIngredientDto> ingredients;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScaledIngredientDto {
        private Long productId;
        private String productName;
        private Double baseQuantity;
        private Double scaledQuantity;
        private Double availableStock;
        private boolean isSufficient;
        private UnitOfMeasure unitOfMeasure;
        private boolean isOptional;
        private String notes;
    }
}
