package cl.diego.NanoStock.inventory.model;

public enum SealedStatus {
    SEALED("Sellado / Cerrado"),
    OPEN("Abierto");

    private final String displayName;

    SealedStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
