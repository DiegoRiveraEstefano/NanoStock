package cl.diego.NanoStock.purchases.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddShoppingItemRequest {

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productId;

    @NotNull(message = "La cantidad requerida es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private Double quantity;

    private BigDecimal unitPriceEstimate;
}
