package cl.diego.NanoStock.inventory.event;

public record StockLevelChangedEvent(
    Long productId,
    Double newTotalQuantity
) {}
