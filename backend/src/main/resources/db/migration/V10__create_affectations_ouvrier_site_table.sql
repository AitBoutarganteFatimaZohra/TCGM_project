-- =============================================================
-- V9__create_affectations_ouvrier_site_table.sql
-- Description: Table de liaison ouvriers ↔ sites
-- =============================================================

CREATE TABLE IF NOT EXISTS affectations_ouvrier_site (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ouvrier_id BIGINT NOT NULL,
    site_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (ouvrier_id) REFERENCES ouvriers(id) ON DELETE CASCADE,
    FOREIGN KEY (site_id) REFERENCES sites(id) ON DELETE CASCADE,
    UNIQUE KEY unique_ouvrier_site_active (ouvrier_id, site_id, active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_affectations_ouvrier ON affectations_ouvrier_site(ouvrier_id);
CREATE INDEX idx_affectations_site ON affectations_ouvrier_site(site_id);
CREATE INDEX idx_affectations_active ON affectations_ouvrier_site(active);