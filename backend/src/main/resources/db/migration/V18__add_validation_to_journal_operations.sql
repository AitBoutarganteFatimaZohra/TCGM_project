-- =============================================================
-- V18 : Ajout de la validation au journal de traçabilité
-- (cahier des charges §6.7 : statut en attente / validé / rejeté)
-- =============================================================

ALTER TABLE journal_operations
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE',
    ADD COLUMN validated_by BIGINT NULL,
    ADD COLUMN validated_at DATETIME NULL;

ALTER TABLE journal_operations
    ADD CONSTRAINT fk_journal_validated_by
    FOREIGN KEY (validated_by) REFERENCES users(id);