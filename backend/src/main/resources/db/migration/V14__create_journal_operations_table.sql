-- =============================================================
-- V14__create_journal_operations_table.sql
-- Description: Table du journal de traçabilité
-- =============================================================

CREATE TABLE IF NOT EXISTS journal_operations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    action_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    details TEXT,
    ip_address VARCHAR(45),
    user_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_journal_action ON journal_operations(action_type);
CREATE INDEX idx_journal_entity ON journal_operations(entity_type, entity_id);
CREATE INDEX idx_journal_user ON journal_operations(user_id);
CREATE INDEX idx_journal_created ON journal_operations(created_at);