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
public class TransferBatchRequest {

    @NotNull(message = "El lote de origen es obligatorio")
    private Long batchId;

    @NotNull(message = "La ubicación de destino es obligatoria")
    private Long targetLocationId;

    @NotNull(message = "La cantidad a trasladar es obligatoria")
    @Positive(message = "La cantidad a trasladar debe ser mayor a cero")
    private Double quantity;

    private String notes;
}
