package cl.diego.NanoStock.inventory.dto;

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
public class ConsumeStockRequest {

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productId;

    @NotNull(message = "La cantidad a consumir es obligatoria")
    @Positive(message = "La cantidad a consumir debe ser mayor a cero")
    private Double quantity;

    private Long batchId;

    private String notes;
}
