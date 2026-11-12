-- V1: Esquema inicial para gestión de identidades y accesos familiares (IAM)
CREATE TABLE IF NOT EXISTS nanostock_iam_users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    hashed_password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_MEMBER',
    enabled INTEGER NOT NULL DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_username ON nanostock_iam_users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON nanostock_iam_users(email);
