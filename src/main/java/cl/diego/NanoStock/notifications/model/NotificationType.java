package cl.diego.NanoStock.notifications.model;

public enum NotificationType {
    EXPIRATION_ALERT("Alerta de Vencimiento"),
    STOCK_LOW_ALERT("Alerta de Stock Crítico"),
    SHOPPING_REMINDER("Recordatorio de Compras"),
    SYSTEM("Sistema");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
