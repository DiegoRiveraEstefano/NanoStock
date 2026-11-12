package cl.diego.NanoStock.analytics.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceTrendResponse {

    private Long productId;
    private String productName;
    private BigDecimal currentPrice;
    private BigDecimal minHistoricalPrice;
    private BigDecimal maxHistoricalPrice;
    private String cheapestStoreName;
    private Double inflationRatePercentage;
    private List<PricePointDto> priceHistory;
}
