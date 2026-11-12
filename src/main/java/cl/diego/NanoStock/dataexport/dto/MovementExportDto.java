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
public class MovementExportDto {
    private Long id;
    private LocalDateTime movementDate;
    private MovementType movementType;
    private String productName;
    private String barCode;
    private Double quantity;
    private String reason;
    private String batchCode;
}
