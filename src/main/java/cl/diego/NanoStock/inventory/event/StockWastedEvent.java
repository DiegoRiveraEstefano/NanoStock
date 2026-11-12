package cl.diego.NanoStock.inventory.event;

public record StockWastedEvent(
    Long productId,
    Long batchId,
    Double quantity,
    String reason
) {}
