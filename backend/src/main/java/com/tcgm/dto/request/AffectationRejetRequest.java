package com.tcgm.dto.request;

import lombok.Data;

/**
 * Requête envoyée par le Chef de Projet pour rejeter une affectation en
 * attente de validation.
 */
@Data
public class AffectationRejetRequest {

    /** Motif du rejet (facultatif mais recommandé). */
    private String motif;
}