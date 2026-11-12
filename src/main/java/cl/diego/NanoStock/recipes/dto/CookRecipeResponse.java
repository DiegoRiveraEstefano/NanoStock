package cl.diego.NanoStock.recipes.dto;

import java.util.List;

import cl.diego.NanoStock.catalog.model.UnitOfMeasure;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CookRecipeResponse {

    private Long recipeId;
    private String recipeTitle;
    private int portionsCooked;
    private String message;
    private List<ConsumedIngredientDto> consumedIngredients;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsumedIngredientDto {
        private Long productId;
        private String productName;
        private Double quantityConsumed;
        private UnitOfMeasure unitOfMeasure;
    }
}
