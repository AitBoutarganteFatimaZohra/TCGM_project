-- =============================================================
-- V19 : Corrige le statut des entrées de journal existantes,
-- créées avant la règle d'auto-validation (connexions/déconnexions
-- et actions de l'Admin n'ont pas besoin de validation par un
-- Chef de Projet).
-- =============================================================

UPDATE journal_operations jo
SET jo.status = 'VALIDE'
WHERE jo.action_type IN ('CONNEXION', 'DECONNEXION');

UPDATE journal_operations jo
JOIN users u ON u.id = jo.user_id
JOIN user_roles ur ON ur.user_id = u.id
JOIN roles r ON r.id = ur.role_id
SET jo.status = 'VALIDE'
WHERE r.name = 'ADMIN';