-- =============================================================
-- V8__create_ouvriers_table.sql
-- Description: Table des ouvriers
-- =============================================================

CREATE TABLE IF NOT EXISTS ouvriers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    cin VARCHAR(50) NOT NULL UNIQUE,
    specialite VARCHAR(100),
    phone VARCHAR(20),
    hire_date DATE,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_ouvriers_cin ON ouvriers(cin);
CREATE INDEX idx_ouvriers_active ON ouvriers(active);
CREATE INDEX idx_ouvriers_specialite ON ouvriers(specialite);   