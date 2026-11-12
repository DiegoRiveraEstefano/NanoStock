-- V9__enhance_recipes_schema.sql
-- Enriquecimiento de recetas: tipos de porción (escalable vs porción única), notas culinarias, dificultad y categoría

ALTER TABLE nanostock_recipes ADD COLUMN portion_type VARCHAR(30) NOT NULL DEFAULT 'SCALABLE';
ALTER TABLE nanostock_recipes ADD COLUMN notes TEXT;
ALTER TABLE nanostock_recipes ADD COLUMN difficulty VARCHAR(20) DEFAULT 'MEDIUM';
ALTER TABLE nanostock_recipes ADD COLUMN category VARCHAR(50) DEFAULT 'LUNCH';

ALTER TABLE nanostock_recipe_ingredients ADD COLUMN notes VARCHAR(255);
