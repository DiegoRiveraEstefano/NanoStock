package cl.diego.NanoStock.recipes.model;

public enum RecipeDifficulty {
    EASY("Fácil"),
    MEDIUM("Media"),
    HARD("Avanzada / Difícil");

    private final String displayName;

    RecipeDifficulty(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
