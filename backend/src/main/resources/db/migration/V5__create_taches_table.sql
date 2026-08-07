-- =============================================================
-- V10__create_taches_table.sql
-- Description: Table des tâches
-- =============================================================

CREATE TABLE IF NOT EXISTS taches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    planned_date TIMESTAMP,
    completed_date TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANIFIEE',
    priority INT DEFAULT 1,
    site_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (site_id) REFERENCES sites(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_taches_status ON taches(status);
CREATE INDEX idx_taches_site ON taches(site_id);
CREATE INDEX idx_taches_priority ON taches(priority);
CREATE INDEX idx_taches_dates ON taches(planned_date, completed_date);