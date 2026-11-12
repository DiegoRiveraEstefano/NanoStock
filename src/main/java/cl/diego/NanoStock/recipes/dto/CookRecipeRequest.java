package cl.diego.NanoStock.recipes.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CookRecipeRequest {

    @Min(value = 1, message = "El número de porciones a cocinar debe ser al menos 1")
    private Integer portions;
}
