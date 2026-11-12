package cl.diego.NanoStock.purchases.dto;

import cl.diego.NanoStock.purchases.model.ShoppingItemStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShoppingItemStatusRequest {

    @NotNull(message = "El estado del ítem es obligatorio")
    private ShoppingItemStatus itemStatus;

    private Double actualQuantity;
}
