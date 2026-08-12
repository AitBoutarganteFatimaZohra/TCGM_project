-- =============================================================
-- V15__create_travaux_and_affectations_tables.sql
-- =============================================================

-- 1. TABLE : travaux
CREATE TABLE IF NOT EXISTS travaux (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    intitule VARCHAR(255) NOT NULL,
    description TEXT,
    date_debut_prevue TIMESTAMP,
    date_fin_prevue TIMESTAMP,
    date_debut_reelle TIMESTAMP,
    date_fin_reelle TIMESTAMP,
    priorite INT DEFAULT 1,
    statut VARCHAR(20) DEFAULT 'PLANIFIE',
    budget_estime DECIMAL(15,2),
    chantier_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (chantier_id) REFERENCES sites(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_travaux_code ON travaux(code);
CREATE INDEX idx_travaux_statut ON travaux(statut);
CREATE INDEX idx_travaux_chantier ON travaux(chantier_id);

-- 2. TABLE : affectations
CREATE TABLE IF NOT EXISTS affectations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date_debut DATE NOT NULL,
    date_fin DATE,
    statut VARCHAR(20) DEFAULT 'PLANIFIEE',
    chantier_id BIGINT NOT NULL,
    ouvrier_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (chantier_id) REFERENCES sites(id) ON DELETE CASCADE,
    FOREIGN KEY (ouvrier_id) REFERENCES ouvriers(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_affectations_chantier ON affectations(chantier_id);
CREATE INDEX idx_affectations_ouvrier ON affectations(ouvrier_id);
CREATE INDEX idx_affectations_statut ON affectations(statut);

-- 3. TABLE : responsables_rh
CREATE TABLE IF NOT EXISTS responsables_rh (
    id BIGINT PRIMARY KEY,
    departement VARCHAR(100),
    fonction VARCHAR(100),
    date_embauche DATE,
    FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. MODIFICATION DE LA TABLE taches
ALTER TABLE taches ADD COLUMN travaux_id BIGINT NULL;
ALTER TABLE taches ADD FOREIGN KEY (travaux_id) REFERENCES travaux(id) ON DELETE CASCADE;
ALTER TABLE taches MODIFY site_id BIGINT NULL;