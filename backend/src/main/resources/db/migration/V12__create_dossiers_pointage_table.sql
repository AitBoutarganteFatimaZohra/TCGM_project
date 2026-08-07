-- =============================================================
-- V12__create_dossiers_pointage_table.sql
-- Description: Table des dossiers de pointage
-- =============================================================

CREATE TABLE IF NOT EXISTS dossiers_pointage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE',
    site_id BIGINT NOT NULL,
    created_by_id BIGINT NOT NULL,
    validated_by_id BIGINT,
    validated_at TIMESTAMP,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (site_id) REFERENCES sites(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (validated_by_id) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE KEY unique_pointage_site_date (site_id, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_pointage_date ON dossiers_pointage(date);
CREATE INDEX idx_pointage_status ON dossiers_pointage(status);
CREATE INDEX idx_pointage_site ON dossiers_pointage(site_id);