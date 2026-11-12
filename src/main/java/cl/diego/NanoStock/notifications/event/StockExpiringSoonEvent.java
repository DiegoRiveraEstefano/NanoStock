package cl.diego.NanoStock.notifications.event;

import java.time.LocalDate;

public record StockExpiringSoonEvent(
    Long productId,
    String productName,
    Long batchId,
    String locationName,
    Double quantity,
    LocalDate expirationDate,
    long daysRemaining
) {}
