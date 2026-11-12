package cl.diego.NanoStock.dataimport.dto;

import java.time.LocalDate;

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
public class InventoryImportRowDto {
    private int rowNumber;
    private String productIdentifier; // barCode o SKU o nombre
    private String locationName;
    private StorageType storageType;
    private Double quantity;
    private LocalDate expirationDate;
    @Builder.Default
    private SealedStatus sealedStatus = SealedStatus.SEALED;
    private String batchCode;
}
