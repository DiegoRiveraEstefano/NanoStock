package cl.diego.NanoStock.inventory.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
public class StockBatchResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private String productBarCode;
    private Long locationId;
    private String locationName;
    private StorageType storageType;
    private Double quantity;
    private LocalDate expirationDate;
    private SealedStatus sealedStatus;
    private String batchCode;
    private boolean isExpired;
    private Long daysUntilExpiration;
    private LocalDateTime createdAt;
}
