package cl.diego.NanoStock.purchases.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private String productBarCode;
    private Double quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountAmount;
    private BigDecimal totalLineAmount;
}
