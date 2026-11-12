package cl.diego.NanoStock.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WasteReportResponse {

    private LocalDate fromDate;
    private LocalDate toDate;
    private long totalIncidents;
    private Double totalQuantityWasted;
    private BigDecimal totalEstimatedFinancialLoss;
    private Map<String, BigDecimal> lossByCategory;
    private List<WasteIncidentDetail> incidentDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WasteIncidentDetail {
        private Long productId;
        private String productName;
        private String categoryName;
        private Double quantity;
        private String reason;
        private BigDecimal estimatedLoss;
        private LocalDate date;
    }
}
