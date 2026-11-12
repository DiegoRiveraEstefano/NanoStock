package cl.diego.NanoStock.purchases.model;

public enum ShoppingItemStatus {
    PENDING("Pendiente"),
    IN_CART("En el Carro"),
    BOUGHT("Comprado"),
    OUT_OF_STOCK("Agotado en Comercio");

    private final String displayName;

    ShoppingItemStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
