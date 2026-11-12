package cl.diego.NanoStock.purchases.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

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
public class ReceiptItemRequest {

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productId;

    @NotNull(message = "La cantidad comprada es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private Double quantity;

    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio unitario debe ser mayor a cero")
    private BigDecimal unitPrice;

    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    private LocalDate expirationDate;

    private Long locationId;
}
