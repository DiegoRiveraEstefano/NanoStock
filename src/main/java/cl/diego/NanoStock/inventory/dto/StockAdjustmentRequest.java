package cl.diego.NanoStock.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentRequest {

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productId;

    @NotNull(message = "El ID de la ubicación es obligatorio")
    private Long locationId;

    @NotNull(message = "La cantidad real contada es obligatoria")
    @PositiveOrZero(message = "La cantidad no puede ser negativa")
    private Double actualQuantity;

    private String notes;
}
