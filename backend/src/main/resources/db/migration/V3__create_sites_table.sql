-- =============================================================
-- V7__create_sites_table.sql
-- Description: Table des sites/chantiers
-- =============================================================

CREATE TABLE IF NOT EXISTS sites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    reference VARCHAR(100) UNIQUE,
    address TEXT,
    start_date DATE,
    end_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANIFIE',
    client_id BIGINT NOT NULL,
    chef_projet_id BIGINT NOT NULL,
    magasinier_id BIGINT,
    agent_saisie_id BIGINT,
    chef_chantier_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE RESTRICT,
    FOREIGN KEY (chef_projet_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (magasinier_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (agent_saisie_id) REFERENCES users(id) ON DELETE SET NULL,
    FOREIGN KEY (chef_chantier_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_sites_status ON sites(status);
CREATE INDEX idx_sites_client ON sites(client_id);
CREATE INDEX idx_sites_chef_projet ON sites(chef_projet_id);
CREATE INDEX idx_sites_dates ON sites(start_date, end_date);