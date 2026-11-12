-- V5: Esquema para histórico de precios y métricas de consumo doméstico
CREATE TABLE IF NOT EXISTS nanostock_price_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    product_id INTEGER NOT NULL,
    store_id INTEGER NOT NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    recorded_at DATE NOT NULL,
    receipt_id INTEGER,
    FOREIGN KEY (product_id) REFERENCES nanostock_products(id) ON DELETE CASCADE,
    FOREIGN KEY (store_id) REFERENCES nanostock_stores(id) ON DELETE CASCADE,
    FOREIGN KEY (receipt_id) REFERENCES nanostock_purchase_receipts(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS nanostock_product_consumption_metrics (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    product_id INTEGER NOT NULL UNIQUE,
    average_daily_consumption REAL NOT NULL DEFAULT 0.0,
    estimated_days_of_autonomy REAL NOT NULL DEFAULT 0.0,
    last_recalculated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES nanostock_products(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_price_history_product_date ON nanostock_price_history(product_id, recorded_at);
