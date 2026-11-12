package cl.diego.NanoStock.dataexport.dto;

import java.time.LocalDate;

import cl.diego.NanoStock.inventory.model.SealedStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryExportFilter {
    private Long locationId;
    private Long productId;
    private LocalDate expiringBefore;
    private SealedStatus sealedStatus;
    @Builder.Default
    private Boolean onlyInStock = Boolean.TRUE;

    public boolean hasFilters() {
        return locationId != null
            || productId != null
            || expiringBefore != null
            || sealedStatus != null
            || (onlyInStock != null && !onlyInStock);
    }
}
