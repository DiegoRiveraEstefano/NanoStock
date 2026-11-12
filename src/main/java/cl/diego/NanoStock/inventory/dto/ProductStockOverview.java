package cl.diego.NanoStock.inventory.dto;

import java.util.List;

import cl.diego.NanoStock.catalog.model.UnitOfMeasure;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStockOverview {

    private Long productId;
    private String productName;
    private String sku;
    private String barCode;
    private UnitOfMeasure unitOfMeasure;
    private Double totalQuantity;
    private Double minStockThreshold;
    private Double targetStockQuantity;
    private boolean isLowStock;
    private List<StockBatchResponse> activeBatches;
}
