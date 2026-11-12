package cl.diego.NanoStock.dataexport.dto;

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
public class MovementExportFilter {
    private MovementType movementType;
    private Long productId;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;

    public boolean hasFilters() {
        return movementType != null
            || productId != null
            || fromDate != null
            || toDate != null;
    }
}
