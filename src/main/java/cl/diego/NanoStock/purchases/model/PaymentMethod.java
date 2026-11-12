package cl.diego.NanoStock.purchases.model;

public enum PaymentMethod {
    DEBIT_CARD("Tarjeta de Débito"),
    CREDIT_CARD("Tarjeta de Crédito"),
    CASH("Efectivo"),
    TRANSFER("Transferencia"),
    OTHER("Otro");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
