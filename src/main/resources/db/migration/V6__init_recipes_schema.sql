-- V6: Esquema para recetario familiar e ingredientes vinculados a productos
CREATE TABLE IF NOT EXISTS nanostock_recipes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    instructions TEXT NOT NULL,
    servings INTEGER NOT NULL DEFAULT 4,
    prep_time_minutes INTEGER,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS nanostock_recipe_ingredients (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    recipe_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    quantity REAL NOT NULL,
    unit_of_measure VARCHAR(30) NOT NULL DEFAULT 'UNIT',
    is_optional INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (recipe_id) REFERENCES nanostock_recipes(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES nanostock_products(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_recipe_ingredients_recipe ON nanostock_recipe_ingredients(recipe_id);
CREATE INDEX IF NOT EXISTS idx_recipe_ingredients_product ON nanostock_recipe_ingredients(product_id);
