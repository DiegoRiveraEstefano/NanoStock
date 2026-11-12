package cl.diego.NanoStock.recipes.model;

import cl.diego.NanoStock.catalog.model.Product;
import cl.diego.NanoStock.catalog.model.UnitOfMeasure;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "nanostock_recipe_ingredients")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    @ToString.Exclude
    private Recipe recipe;

    @NotNull(message = "El producto asociado al ingrediente es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull(message = "La cantidad del ingrediente es obligatoria")
    @Positive(message = "La cantidad del ingrediente debe ser mayor a cero")
    @Column(nullable = false)
    private Double quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_of_measure", nullable = false, length = 30)
    @Builder.Default
    private UnitOfMeasure unitOfMeasure = UnitOfMeasure.UNIT;

    @Column(name = "is_optional", nullable = false)
    @Builder.Default
    private boolean isOptional = false;

    @Column(length = 255)
    private String notes;
}
