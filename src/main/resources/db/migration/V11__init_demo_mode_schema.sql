-- V11: Esquema para modo demo de portafolio y limpieza automática cada 7 días

CREATE TABLE IF NOT EXISTS nanostock_demo_state (
    id INTEGER PRIMARY KEY CHECK (id = 1),
    last_reset_at DATETIME NOT NULL,
    next_reset_at DATETIME NOT NULL,
    last_cleanup_deleted_rows INTEGER NOT NULL DEFAULT 0,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Marcar registros semilla (is_seed = 1) vs datos creados por visitantes (is_seed = 0)
ALTER TABLE nanostock_iam_users ADD COLUMN is_seed INTEGER NOT NULL DEFAULT 0;
ALTER TABLE nanostock_categories ADD COLUMN is_seed INTEGER NOT NULL DEFAULT 0;
ALTER TABLE nanostock_brands ADD COLUMN is_seed INTEGER NOT NULL DEFAULT 0;
ALTER TABLE nanostock_products ADD COLUMN is_seed INTEGER NOT NULL DEFAULT 0;
ALTER TABLE nanostock_locations ADD COLUMN is_seed INTEGER NOT NULL DEFAULT 0;
ALTER TABLE nanostock_stock_batches ADD COLUMN is_seed INTEGER NOT NULL DEFAULT 0;
ALTER TABLE nanostock_inventory_movements ADD COLUMN is_seed INTEGER NOT NULL DEFAULT 0;
ALTER TABLE nanostock_stores ADD COLUMN is_seed INTEGER NOT NULL DEFAULT 0;
ALTER TABLE nanostock_purchase_receipts ADD COLUMN is_seed INTEGER NOT NULL DEFAULT 0;
ALTER TABLE nanostock_purchase_receipt_items ADD COLUMN is_seed INTEGER NOT NULL DEFAULT 0;
ALTER TABLE nanostock_shopping_lists ADD COLUMN is_seed INTEGER NOT NULL DEFAULT 0;
ALTER TABLE nanostock_shopping_list_items ADD COLUMN is_seed INTEGER NOT NULL DEFAULT 0;
ALTER TABLE nanostock_recipes ADD COLUMN is_seed INTEGER NOT NULL DEFAULT 0;
ALTER TABLE nanostock_recipe_ingredients ADD COLUMN is_seed INTEGER NOT NULL DEFAULT 0;
ALTER TABLE nanostock_notifications ADD COLUMN is_seed INTEGER NOT NULL DEFAULT 0;
