package cl.diego.NanoStock.dataexport.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import cl.diego.NanoStock.catalog.model.UnitOfMeasure;
import cl.diego.NanoStock.inventory.model.SealedStatus;
import cl.diego.NanoStock.inventory.model.StorageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryExportDto {
    private Long id;
    private String locationName;
    private StorageType storageType;
    private String productName;
    private String barCode;
    private String sku;
    private Double quantity;
    private UnitOfMeasure unitOfMeasure;
    private LocalDate expirationDate;
    private Long daysRemaining;
    private SealedStatus sealedStatus;
    private String batchCode;
    private LocalDateTime createdAt;
}
