package cl.diego.NanoStock.inventory.dto;

import java.time.LocalDateTime;

import cl.diego.NanoStock.inventory.model.StorageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponse {

    private Long id;
    private String name;
    private StorageType storageType;
    private String description;
    private long activeBatchesCount;
    private double totalQuantity;
    private LocalDateTime createdAt;
}
