package cl.diego.NanoStock.purchases.model;

public enum StoreType {
    SUPERMARKET("Supermercado"),
    LOCAL_STORE("Almacén de Barrio / Minimarket"),
    FARMERS_MARKET("Feria Libre / Frutería"),
    WHOLESALE("Mayorista"),
    PHARMACY("Farmacia"),
    OTHER("Otro");

    private final String displayName;

    StoreType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
