-- V8__init_notifications_schema.sql
-- Alertas, Notificaciones internas del hogar y registro de tareas programadas

CREATE TABLE IF NOT EXISTS nanostock_notifications (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    severity VARCHAR(30) NOT NULL,
    is_read INTEGER NOT NULL DEFAULT 0,
    reference_id INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notifications_type ON nanostock_notifications(notification_type);
CREATE INDEX IF NOT EXISTS idx_notifications_is_read ON nanostock_notifications(is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON nanostock_notifications(created_at);
