-- =============================================================
-- V11__create_affectations_ouvrier_tache_table.sql
-- Description: Table de liaison ouvriers ↔ tâches
-- =============================================================

CREATE TABLE IF NOT EXISTS affectations_ouvrier_tache (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ouvrier_id BIGINT NOT NULL,
    tache_id BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ouvrier_id) REFERENCES ouvriers(id) ON DELETE CASCADE,
    FOREIGN KEY (tache_id) REFERENCES taches(id) ON DELETE CASCADE,
    UNIQUE KEY unique_ouvrier_tache (ouvrier_id, tache_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_affectations_ouvrier_tache ON affectations_ouvrier_tache(ouvrier_id);
CREATE INDEX idx_affectations_tache_ouvrier ON affectations_ouvrier_tache(tache_id);