package com.tcgm.dto.request;

import lombok.Getter;
import lombok.Setter;

/**
 * Requête envoyée par le Chef de Chantier (niveau 1) ou le Chef de Projet
 * (niveau 2, recours) pour rejeter l'action en attente sur une ressource.
 */
@Getter
@Setter
public class RessourceRejetRequest {

    /** Motif du rejet (facultatif mais recommandé). */
    private String motif;
}