package cl.diego.NanoStock.purchases.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import cl.diego.NanoStock.purchases.model.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptResponse {

    private Long id;
    private Long storeId;
    private String storeName;
    private String storeBranch;
    private LocalDate purchaseDate;
    private BigDecimal totalAmount;
    private PaymentMethod paymentMethod;
    private String notes;
    private String username;
    private List<ReceiptItemResponse> items;
    private LocalDateTime createdAt;
}
