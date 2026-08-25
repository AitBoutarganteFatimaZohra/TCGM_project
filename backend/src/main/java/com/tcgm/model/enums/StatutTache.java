package com.tcgm.model.enums;

/**
 * Statut métier d'une Tâche.
 *
 * Circuit de validation (voir Tache.java) :
 *  - PLANIFIEE / EN_COURS / TERMINEE : statuts "normaux"
 *  - EN_ATTENTE_VALIDATION : un changement de statut ou de date prévue a été
 *    soumis par le Chef de Chantier et attend la décision du Chef de Projet.
 *    Le statut "cible" proposé est conservé dans Tache.proposedStatus, et le
 *    statut d'origine (pour un éventuel rejet) dans Tache.previousStatus.
 */
public enum StatutTache {
    PLANIFIEE,
    EN_COURS,
    TERMINEE,
    EN_ATTENTE_VALIDATION
}