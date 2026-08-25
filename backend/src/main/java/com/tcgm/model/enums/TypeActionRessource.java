package com.tcgm.model.enums;

/**
 * Type d'action effectuée par le Magasinier et actuellement en attente de
 * validation (Ressource.pendingAction). Détermine comment annuler l'action
 * en cas de rejet définitif (niveau 2).
 */
public enum TypeActionRessource {
    CREATION,
    MODIFICATION,
    CHANGEMENT_STATUT,
    SUPPRESSION
}