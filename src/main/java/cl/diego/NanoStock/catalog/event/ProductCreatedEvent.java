package cl.diego.NanoStock.catalog.event;

public record ProductCreatedEvent(
    Long productId,
    String name,
    String barCode,
    String sku,
    Double minStockThreshold,
    Double targetStockQuantity
) {}
