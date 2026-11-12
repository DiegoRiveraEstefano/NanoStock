package cl.diego.NanoStock.catalog.event;

public record ProductUpdatedEvent(
    Long productId,
    String name,
    String barCode,
    String sku
) {}
