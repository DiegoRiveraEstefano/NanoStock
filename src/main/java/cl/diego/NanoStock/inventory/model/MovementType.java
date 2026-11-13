package cl.diego.NanoStock.inventory.model;

public enum MovementType {
    ENTRY("Entrada / Compra"),
    CONSUMPTION("Consumo Doméstico"),
    WASTE("Merma / Desperdicio"),
    ADJUSTMENT("Ajuste de Auditoría"),
    TRANSFER("Transferencia de Ubicación");

    private final String displayName;

    MovementType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
