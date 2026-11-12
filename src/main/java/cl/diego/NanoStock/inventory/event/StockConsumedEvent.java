package cl.diego.NanoStock.inventory.event;

public record StockConsumedEvent(
    Long productId,
    Long batchId,
    Double quantity
) {}
