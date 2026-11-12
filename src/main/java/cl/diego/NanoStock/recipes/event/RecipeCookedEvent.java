package cl.diego.NanoStock.recipes.event;

import java.time.LocalDateTime;
import java.util.List;

public record RecipeCookedEvent(
    Long recipeId,
    String recipeTitle,
    int portionsCooked,
    List<CookedIngredientDetail> ingredientsConsumed,
    String cookedBy,
    LocalDateTime cookedAt
) {
    public record CookedIngredientDetail(
        Long productId,
        String productName,
        Double quantityConsumed,
        String unitOfMeasure
    ) {}
}
