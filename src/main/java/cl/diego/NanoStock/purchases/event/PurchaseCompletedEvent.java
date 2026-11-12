package cl.diego.NanoStock.purchases.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PurchaseCompletedEvent(
    Long receiptId,
    Long storeId,
    String storeName,
    LocalDate purchaseDate,
    BigDecimal totalAmount,
    List<PurchaseItemDetail> items,
    String username
) {
    public record PurchaseItemDetail(
        Long productId,
        String productName,
        Double quantity,
        BigDecimal unitPrice,
        BigDecimal discountAmount,
        BigDecimal totalLineAmount,
        LocalDate expirationDate,
        Long locationId
    ) {}
}
