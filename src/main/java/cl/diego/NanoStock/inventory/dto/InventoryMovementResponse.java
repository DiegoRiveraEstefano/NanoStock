package cl.diego.NanoStock.inventory.dto;

import java.time.LocalDateTime;

import cl.diego.NanoStock.inventory.model.MovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovementResponse {

    private Long id;
    private Long productId;
    private String productName;
    private Long batchId;
    private Long locationId;
    private String locationName;
    private MovementType movementType;
    private Double quantity;
    private String wasteReason;
    private String notes;
    private String username;
    private LocalDateTime createdAt;
}
