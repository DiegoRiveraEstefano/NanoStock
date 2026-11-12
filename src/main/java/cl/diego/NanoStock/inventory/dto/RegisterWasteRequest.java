package cl.diego.NanoStock.inventory.dto;

import jakarta.validation.constraints.NotBlank;
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
public class RegisterWasteRequest {

    @NotNull(message = "El ID del lote a dar de baja es obligatorio")
    private Long batchId;

    @NotNull(message = "La cantidad a descartar es obligatoria")
    @Positive(message = "La cantidad a descartar debe ser mayor a cero")
    private Double quantity;

    @NotBlank(message = "El motivo de la merma es obligatorio (ej. Vencido, Descompuesto, Roto)")
    private String wasteReason;

    private String notes;
}
