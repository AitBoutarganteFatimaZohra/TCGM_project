-- =============================================================
-- V17__create_responsables_rh_table.sql
-- Description: Création de la table responsables_rh
-- =============================================================

CREATE TABLE IF NOT EXISTS responsables_rh (
    id BIGINT PRIMARY KEY,
    departement VARCHAR(100),
    fonction VARCHAR(100),
    date_embauche DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_responsables_rh_departement ON responsables_rh(departement);
CREATE INDEX idx_responsables_rh_fonction ON responsables_rh(fonction);