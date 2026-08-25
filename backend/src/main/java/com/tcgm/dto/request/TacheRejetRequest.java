package com.tcgm.dto.request;

import lombok.Data;

/**
 * Requête envoyée par le Chef de Projet pour rejeter une demande de
 * validation en attente sur une tâche.
 */
@Data
public class TacheRejetRequest {

    /**
     * Motif du rejet (facultatif mais recommandé pour la traçabilité).
     */
    private String motif;
}