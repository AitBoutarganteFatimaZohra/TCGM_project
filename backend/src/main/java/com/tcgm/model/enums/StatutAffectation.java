package com.tcgm.model.enums;

/**
 * ⚠️ Reconstitué à partir des valeurs utilisées côté frontend
 * (AffectationsPage.jsx / AffectationDetailPage.jsx) — comparez avec votre
 * fichier réel et ajustez si les valeurs d'origine diffèrent.
 *
 * Circuit de validation (Chef de Chantier -> Chef de Projet, un seul
 * niveau) : la création d'une affectation par le Chef de Chantier passe
 * par EN_ATTENTE_VALIDATION jusqu'à la décision du Chef de Projet.
 */
public enum StatutAffectation {
    PLANIFIEE,
    EN_COURS,
    TERMINEE,
    ANNULEE,

    /** En attente de la décision du Chef de Projet. */
    EN_ATTENTE_VALIDATION,

    /** Rejetée par le Chef de Projet (définitif). */
    REJETEE
}