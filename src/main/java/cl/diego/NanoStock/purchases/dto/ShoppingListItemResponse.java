package cl.diego.NanoStock.purchases.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import cl.diego.NanoStock.purchases.model.ShoppingItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingListItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private String productBarCode;
    private Double suggestedQuantity;
    private Double actualQuantity;
    private boolean isAutoSuggested;
    private ShoppingItemStatus itemStatus;
    private BigDecimal unitPriceEstimate;
    private LocalDateTime createdAt;
}
