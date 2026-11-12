package cl.diego.NanoStock.catalog.model;

public enum UnitOfMeasure {
    UNIT("Unidad", "u"),
    KILOGRAM("Kilogramo", "kg"),
    GRAM("Gramo", "g"),
    LITER("Litro", "L"),
    MILLILITER("Mililitro", "mL"),
    PACKAGE("Paquete", "paq"),
    BOX("Caja", "caja"),
    CAN("Lata", "lata"),
    BOTTLE("Botella", "bot");

    private final String displayName;
    private final String symbol;

    UnitOfMeasure(String displayName, String symbol) {
        this.displayName = displayName;
        this.symbol = symbol;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return symbol;
    }

    public boolean isWeight() {
        return this == KILOGRAM || this == GRAM;
    }

    public boolean isVolume() {
        return this == LITER || this == MILLILITER;
    }

    public boolean isDiscrete() {
        return !isWeight() && !isVolume();
    }
}
