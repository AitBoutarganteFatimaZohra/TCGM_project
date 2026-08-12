-- =============================================================
-- V16__migrate_affectation_data.sql
-- Migration des données d'affectation
-- =============================================================

INSERT INTO affectations (date_debut, date_fin, statut, chantier_id, ouvrier_id, created_at, updated_at)
SELECT 
    a.start_date,
    a.end_date,
    CASE 
        WHEN a.active = 1 THEN 'EN_COURS'
        ELSE 'TERMINEE'
    END AS statut,
    a.site_id,
    a.ouvrier_id,
    a.created_at,
    a.updated_at
FROM affectations_ouvrier_site a;