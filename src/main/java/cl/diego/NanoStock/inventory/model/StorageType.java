package cl.diego.NanoStock.inventory.model;

public enum StorageType {
    AMBIENT("Ambiente / Despensa"),
    REFRIGERATED("Refrigerado"),
    FROZEN("Congelador / Freezer"),
    OTHER("Otro");

    private final String displayName;

    StorageType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
