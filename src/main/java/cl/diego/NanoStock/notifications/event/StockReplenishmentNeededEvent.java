package cl.diego.NanoStock.notifications.event;

public record StockReplenishmentNeededEvent(
    Long productId,
    String productName,
    Double currentStock,
    Double minThreshold,
    Double targetQuantity
) {}
