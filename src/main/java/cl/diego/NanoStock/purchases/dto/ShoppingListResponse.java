package cl.diego.NanoStock.purchases.dto;

import java.time.LocalDateTime;
import java.util.List;

import cl.diego.NanoStock.purchases.model.ShoppingListStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingListResponse {

    private Long id;
    private String title;
    private ShoppingListStatus status;
    private Long targetStoreId;
    private String targetStoreName;
    private List<ShoppingListItemResponse> items;
    private LocalDateTime createdAt;
}
