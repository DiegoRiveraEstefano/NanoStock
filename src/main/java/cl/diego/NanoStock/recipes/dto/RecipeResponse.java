package cl.diego.NanoStock.recipes.dto;

import java.time.LocalDateTime;
import java.util.List;

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
public class RecipeResponse {

    private Long id;
    private String title;
    private String description;
    private String instructions;
    private Integer servings;
    private PortionType portionType;
    private Integer prepTimeMinutes;
    private RecipeDifficulty difficulty;
    private RecipeCategory category;
    private String notes;
    private List<RecipeIngredientResponse> ingredients;
    private LocalDateTime createdAt;
}
