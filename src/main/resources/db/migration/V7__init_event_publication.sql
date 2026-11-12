-- V7: Tabla para registro de eventos de dominio de Spring Modulith
CREATE TABLE IF NOT EXISTS event_publication
(
  id                     BLOB NOT NULL,
  completion_date        TIMESTAMP,
  event_type             VARCHAR(512) NOT NULL,
  listener_id            VARCHAR(512) NOT NULL,
  publication_date       TIMESTAMP NOT NULL,
  serialized_event       TEXT NOT NULL,
  status                 VARCHAR(50),
  completion_attempts    INTEGER DEFAULT 0,
  last_resubmission_date TIMESTAMP,
  PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_event_pub_listener_serialized 
  ON event_publication (listener_id, serialized_event);

CREATE INDEX IF NOT EXISTS idx_event_pub_completion_date 
  ON event_publication (completion_date);
