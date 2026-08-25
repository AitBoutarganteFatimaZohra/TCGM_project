package com.tcgm.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Requête envoyée par le Chef de Chantier pour soumettre un changement de
 * statut et/ou de date prévue d'une tâche à la validation du Chef de Projet.
 *
 * Au moins un des deux champs doit être renseigné.
 */
@Data
public class TacheSoumissionRequest {

    /**
     * Nouveau statut souhaité (ex: TERMINEE, EN_COURS). Optionnel si seule
     * la date change.
     */
    private String proposedStatus;

    /**
     * Nouvelle date prévue souhaitée. Optionnelle si seul le statut change.
     */
    private LocalDateTime proposedPlannedDate;
}