package cl.diego.NanoStock.purchases.model;

public enum ShoppingListStatus {
    ACTIVE("Activa"),
    COMPLETED("Completada"),
    ARCHIVED("Archivada");

    private final String displayName;

    ShoppingListStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
