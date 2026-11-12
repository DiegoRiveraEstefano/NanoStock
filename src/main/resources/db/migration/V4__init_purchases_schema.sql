-- V4: Esquema para comercios, boletas de compra y listas de compras
CREATE TABLE IF NOT EXISTS nanostock_stores (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(150) NOT NULL,
    branch_name VARCHAR(150),
    store_type VARCHAR(50) DEFAULT 'SUPERMARKET',
    address TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS nanostock_purchase_receipts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    store_id INTEGER,
    purchase_date DATE NOT NULL,
    total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    payment_method VARCHAR(50),
    notes TEXT,
    user_id INTEGER,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (store_id) REFERENCES nanostock_stores(id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES nanostock_iam_users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS nanostock_purchase_receipt_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    receipt_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    quantity REAL NOT NULL,
    unit_price DECIMAL(12, 2) NOT NULL,
    discount_amount DECIMAL(12, 2) DEFAULT 0.00,
    total_line_amount DECIMAL(12, 2) NOT NULL,
    FOREIGN KEY (receipt_id) REFERENCES nanostock_purchase_receipts(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES nanostock_products(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS nanostock_shopping_lists (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title VARCHAR(150) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    target_store_id INTEGER,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (target_store_id) REFERENCES nanostock_stores(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS nanostock_shopping_list_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    shopping_list_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    suggested_quantity REAL NOT NULL,
    actual_quantity REAL,
    is_auto_suggested INTEGER NOT NULL DEFAULT 1,
    item_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    unit_price_estimate DECIMAL(12, 2),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (shopping_list_id) REFERENCES nanostock_shopping_lists(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES nanostock_products(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_receipts_date ON nanostock_purchase_receipts(purchase_date);
CREATE INDEX IF NOT EXISTS idx_shopping_items_list ON nanostock_shopping_list_items(shopping_list_id);
