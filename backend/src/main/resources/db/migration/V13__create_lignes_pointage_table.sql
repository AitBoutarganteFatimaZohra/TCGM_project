-- =============================================================
-- V13__create_lignes_pointage_table.sql
-- Description: Table des lignes de pointage
-- =============================================================

CREATE TABLE IF NOT EXISTS lignes_pointage (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dossier_id BIGINT NOT NULL,
    ouvrier_id BIGINT NOT NULL,
    tache_id BIGINT NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    half_day BOOLEAN DEFAULT FALSE,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (dossier_id) REFERENCES dossiers_pointage(id) ON DELETE CASCADE,
    FOREIGN KEY (ouvrier_id) REFERENCES ouvriers(id) ON DELETE RESTRICT,
    FOREIGN KEY (tache_id) REFERENCES taches(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_lignes_dossier ON lignes_pointage(dossier_id);
CREATE INDEX idx_lignes_ouvrier ON lignes_pointage(ouvrier_id);
CREATE INDEX idx_lignes_tache ON lignes_pointage(tache_id);