package cl.diego.NanoStock.analytics.dto;

import java.time.LocalDate;

import cl.diego.NanoStock.catalog.model.UnitOfMeasure;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsumptionAutonomyResponse {

    private Long productId;
    private String productName;
    private String sku;
    private UnitOfMeasure unitOfMeasure;
    private Double currentStock;
    private Double averageDailyConsumption;
    private Double estimatedDaysOfAutonomy;
    private LocalDate projectedStockoutDate;
    private boolean isRiskOfStockout;
}
