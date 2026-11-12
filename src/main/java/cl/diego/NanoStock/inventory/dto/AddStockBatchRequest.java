package cl.diego.NanoStock.inventory.dto;

import java.time.LocalDate;

import cl.diego.NanoStock.inventory.model.SealedStatus;
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
public class AddStockBatchRequest {

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productId;

    @NotNull(message = "El ID de la ubicación es obligatorio")
    private Long locationId;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private Double quantity;

    private LocalDate expirationDate;

    @Builder.Default
    private SealedStatus sealedStatus = SealedStatus.SEALED;

    private String batchCode;

    private String notes;
}
