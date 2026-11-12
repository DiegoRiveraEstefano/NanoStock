-- V2: Esquema para catálogo maestro de productos, categorías y marcas
CREATE TABLE IF NOT EXISTS nanostock_categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    parent_id INTEGER,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES nanostock_categories(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS nanostock_brands (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS nanostock_products (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sku VARCHAR(100) NOT NULL UNIQUE,
    bar_code VARCHAR(100) NOT NULL,
    category_id INTEGER,
    brand_id INTEGER,
    unit_of_measure VARCHAR(30) DEFAULT 'UNIT',
    min_stock_threshold REAL DEFAULT 1.0,
    target_stock_quantity REAL DEFAULT 3.0,
    is_perishable INTEGER DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES nanostock_categories(id) ON DELETE SET NULL,
    FOREIGN KEY (brand_id) REFERENCES nanostock_brands(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_products_barcode ON nanostock_products(bar_code);
CREATE INDEX IF NOT EXISTS idx_products_sku ON nanostock_products(sku);
CREATE INDEX IF NOT EXISTS idx_products_name ON nanostock_products(name);
