package com.tcgm.model.enums;

/**
 * Statut du circuit de validation à deux niveaux d'une action Magasinier
 * sur une Ressource : Chef de Chantier (niveau 1) puis Chef de Projet
 * (niveau 2, uniquement en cas de recours après un rejet au niveau 1).
 */
public enum StatutValidationRessource {
    /** Aucune action en attente : la ressource est dans un état stable. */
    VALIDEE,

    /** Niveau 1 : en attente de décision du Chef de Chantier. */
    EN_ATTENTE_CHEF_CHANTIER,

    /** Niveau 2 (recours) : en attente de décision du Chef de Projet. */
    EN_ATTENTE_CHEF_PROJET,

    /** Rejet définitif prononcé par le Chef de Projet (niveau 2). */
    REJETEE
}