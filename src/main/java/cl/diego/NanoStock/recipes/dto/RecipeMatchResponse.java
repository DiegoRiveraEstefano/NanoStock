package cl.diego.NanoStock.recipes.dto;

import java.util.List;

import cl.diego.NanoStock.catalog.model.UnitOfMeasure;
import cl.diego.NanoStock.recipes.model.PortionType;
import cl.diego.NanoStock.recipes.model.RecipeCategory;
import cl.diego.NanoStock.recipes.model.RecipeDifficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeMatchResponse {

    private Long recipeId;
    private String recipeTitle;
    private PortionType portionType;
    private Integer servings;
    private Integer prepTimeMinutes;
    private RecipeDifficulty difficulty;
    private RecipeCategory category;
    private String notes;
    private double viabilityPercentage;
    private boolean canCookNow;
    private int expiringIngredientsCount;
    private List<MissingIngredientDto> missingIngredients;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MissingIngredientDto {
        private Long productId;
        private String productName;
        private Double requiredQuantity;
        private Double availableStock;
        private Double missingQuantity;
        private UnitOfMeasure unitOfMeasure;
        private boolean isOptional;
    }
}
