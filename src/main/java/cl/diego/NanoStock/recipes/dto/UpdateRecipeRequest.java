package cl.diego.NanoStock.recipes.dto;

import java.util.List;

import cl.diego.NanoStock.recipes.model.PortionType;
import cl.diego.NanoStock.recipes.model.RecipeCategory;
import cl.diego.NanoStock.recipes.model.RecipeDifficulty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRecipeRequest {

    @NotBlank(message = "El título de la receta es obligatorio")
    private String title;

    private String description;

    @NotBlank(message = "Las instrucciones de preparación son obligatorias")
    private String instructions;

    @Min(value = 1, message = "Las raciones base deben ser al menos 1")
    private Integer servings;

    private PortionType portionType;

    private Integer prepTimeMinutes;

    private RecipeDifficulty difficulty;

    private RecipeCategory category;

    private String notes;

    @Valid
    private List<RecipeIngredientRequest> ingredients;
}
