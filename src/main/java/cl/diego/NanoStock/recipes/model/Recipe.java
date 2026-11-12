package cl.diego.NanoStock.recipes.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "nanostock_recipes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El título de la receta es obligatorio")
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Las instrucciones de preparación son obligatorias")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String instructions;

    @Min(value = 1, message = "Las raciones base deben ser al menos 1")
    @Column(nullable = false)
    @Builder.Default
    private Integer servings = 4;

    @NotNull(message = "El tipo de porción es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "portion_type", nullable = false, length = 30)
    @Builder.Default
    private PortionType portionType = PortionType.SCALABLE;

    @Column(name = "prep_time_minutes")
    private Integer prepTimeMinutes;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private RecipeDifficulty difficulty = RecipeDifficulty.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    @Builder.Default
    private RecipeCategory category = RecipeCategory.LUNCH;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.servings == null || this.servings < 1) {
            this.servings = (this.portionType == PortionType.FIXED_SINGLE) ? 1 : 4;
        }
        if (this.portionType == null) {
            this.portionType = PortionType.SCALABLE;
        }
        if (this.difficulty == null) {
            this.difficulty = RecipeDifficulty.MEDIUM;
        }
        if (this.category == null) {
            this.category = RecipeCategory.LUNCH;
        }
    }

    public void addIngredient(RecipeIngredient ingredient) {
        ingredients.add(ingredient);
        ingredient.setRecipe(this);
    }

    public void removeIngredient(RecipeIngredient ingredient) {
        ingredients.remove(ingredient);
        ingredient.setRecipe(null);
    }
}
