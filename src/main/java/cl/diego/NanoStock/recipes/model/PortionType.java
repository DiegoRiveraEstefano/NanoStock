package cl.diego.NanoStock.recipes.model;

public enum PortionType {
    SCALABLE("Porción Escalable (Ajuste dinámico según comensales)"),
    FIXED_SINGLE("Porción Única (Plato individual o preparación fija)");

    private final String description;

    PortionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
