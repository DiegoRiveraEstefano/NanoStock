package cl.diego.NanoStock.purchases.dto;

import java.time.LocalDate;
import java.util.List;

import cl.diego.NanoStock.purchases.model.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordReceiptRequest {

    @NotNull(message = "El ID del comercio es obligatorio")
    private Long storeId;

    @NotNull(message = "La fecha de compra es obligatoria")
    private LocalDate purchaseDate;

    private PaymentMethod paymentMethod;

    private String notes;

    @NotEmpty(message = "La boleta debe incluir al menos un producto")
    @Valid
    private List<ReceiptItemRequest> items;
}
