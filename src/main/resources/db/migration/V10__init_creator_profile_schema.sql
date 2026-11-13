-- V10: Perfil público del creador del proyecto y enlaces a redes sociales
CREATE TABLE IF NOT EXISTS nanostock_creator_profile (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name VARCHAR(150) NOT NULL,
    headline VARCHAR(200) NOT NULL,
    bio TEXT NOT NULL,
    avatar_url VARCHAR(500),
    location VARCHAR(120),
    tech_stack VARCHAR(500),
    github_url VARCHAR(300),
    linkedin_url VARCHAR(300),
    website_url VARCHAR(300),
    x_url VARCHAR(300),
    contact_email VARCHAR(200),
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
