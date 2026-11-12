package cl.diego.NanoStock.purchases.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShoppingListRequest {

    @NotBlank(message = "El título de la lista de compras es obligatorio")
    private String title;

    private Long targetStoreId;
}
