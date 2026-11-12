package cl.diego.NanoStock.recipes.dto;

import java.util.List;

import cl.diego.NanoStock.recipes.model.PortionType;
import cl.diego.NanoStock.recipes.model.RecipeCategory;
import cl.diego.NanoStock.recipes.model.RecipeDifficulty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRecipeRequest {

    @NotBlank(message = "El título de la receta es obligatorio")
    private String title;

    private String description;

    @NotBlank(message = "Las instrucciones de preparación son obligatorias")
    private String instructions;

    @Min(value = 1, message = "Las raciones base deben ser al menos 1")
    @Builder.Default
    private Integer servings = 4;

    @Builder.Default
    private PortionType portionType = PortionType.SCALABLE;

    private Integer prepTimeMinutes;

    @Builder.Default
    private RecipeDifficulty difficulty = RecipeDifficulty.MEDIUM;

    @Builder.Default
    private RecipeCategory category = RecipeCategory.LUNCH;

    private String notes;

    @NotEmpty(message = "La receta debe incluir al menos un ingrediente")
    @Valid
    private List<RecipeIngredientRequest> ingredients;
}
