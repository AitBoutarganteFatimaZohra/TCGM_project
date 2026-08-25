package com.tcgm.service;

import com.tcgm.dto.response.AlerteResponse;

import java.util.List;

public interface AlerteService {

    /**
     * Scanne tous les chantiers, crée les nouvelles alertes nécessaires,
     * et résout automatiquement celles dont la condition a disparu.
     * Appelé périodiquement par le scheduler (voir @Scheduled).
     */
    void detecterEtResoudreAlertes();

    /**
     * Alertes actives pour les chantiers dont l'utilisateur (identifié par
     * son email, via le token d'authentification) est Chef de Projet.
     */
    List<AlerteResponse> getAlertesForCurrentUser(String email);

    /**
     * Marque une alerte comme résolue manuellement par l'utilisateur donné.
     */
    AlerteResponse resolveAlerte(Long alerteId, String email);
}