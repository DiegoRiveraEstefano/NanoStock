-- V3: Esquema para ubicaciones físicas, lotes de inventario (FEFO) y movimientos
CREATE TABLE IF NOT EXISTS nanostock_locations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    storage_type VARCHAR(50) NOT NULL DEFAULT 'AMBIENT',
    description TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS nanostock_stock_batches (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    product_id INTEGER NOT NULL,
    location_id INTEGER NOT NULL,
    quantity REAL NOT NULL,
    expiration_date DATE,
    sealed_status VARCHAR(20) NOT NULL DEFAULT 'SEALED',
    batch_code VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES nanostock_products(id) ON DELETE CASCADE,
    FOREIGN KEY (location_id) REFERENCES nanostock_locations(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS nanostock_inventory_movements (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    product_id INTEGER NOT NULL,
    batch_id INTEGER,
    location_id INTEGER,
    movement_type VARCHAR(30) NOT NULL,
    quantity REAL NOT NULL,
    waste_reason VARCHAR(100),
    notes TEXT,
    user_id INTEGER,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES nanostock_products(id) ON DELETE CASCADE,
    FOREIGN KEY (batch_id) REFERENCES nanostock_stock_batches(id) ON DELETE SET NULL,
    FOREIGN KEY (location_id) REFERENCES nanostock_locations(id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES nanostock_iam_users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_batches_product ON nanostock_stock_batches(product_id);
CREATE INDEX IF NOT EXISTS idx_batches_expiration ON nanostock_stock_batches(expiration_date);
CREATE INDEX IF NOT EXISTS idx_movements_product ON nanostock_inventory_movements(product_id);
