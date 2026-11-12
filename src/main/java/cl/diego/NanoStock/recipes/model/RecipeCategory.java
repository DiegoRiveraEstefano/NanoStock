package cl.diego.NanoStock.recipes.model;

public enum RecipeCategory {
    BREAKFAST("Desayuno"),
    LUNCH("Almuerzo"),
    DINNER("Cena"),
    DESSERT("Postre"),
    SNACK("Colación / Merienda"),
    BEVERAGE("Bebida"),
    OTHER("Otro");

    private final String displayName;

    RecipeCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
