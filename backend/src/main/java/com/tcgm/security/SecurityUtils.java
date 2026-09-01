package com.tcgm.security;

import com.tcgm.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final SiteRepository siteRepository;

    public UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new IllegalStateException("Aucun utilisateur authentifié");
        }
        return (UserPrincipal) authentication.getPrincipal();
    }

    public boolean hasRole(String roleName) {
        return getCurrentUser().hasRole(roleName);
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public boolean isChefProjet() {
        return hasRole("CHEF_PROJET");
    }

    public boolean isChefChantier() {
        return hasRole("CHEF_CHANTIER");
    }

    public boolean isMagasinier() {
        return hasRole("MAGASINIER");
    }

    public boolean isAgentSaisie() {
        return hasRole("AGENT_SAISIE");
    }

    /**
     * IDs des chantiers dont l'utilisateur courant est Chef de Chantier.
     */
    public List<Long> getChantierIdsAsChefChantier() {
        Long userId = getCurrentUser().getId();
        return siteRepository.findIdsByChefChantierId(userId);
    }

    /**
     * ⚠️ NOUVEAU : IDs des chantiers dont l'utilisateur courant est Chef de Projet.
     */
    public List<Long> getChantierIdsAsChefProjet() {
        Long userId = getCurrentUser().getId();
        return siteRepository.findIdsByChefProjetId(userId);
    }

    /**
     * IDs des chantiers dont l'utilisateur courant est Magasinier.
     */
    public List<Long> getChantierIdsAsMagasinier() {
        Long userId = getCurrentUser().getId();
        return siteRepository.findIdsByMagasinierId(userId);
    }

    /**
     * ID du chantier unique auquel l'Agent de Saisie courant est affecté.
     * Retourne null s'il n'est affecté à aucun site.
     */
    public Long getSiteIdAsAgentSaisie() {
        Long userId = getCurrentUser().getId();
        List<Long> ids = siteRepository.findIdsByAgentSaisieId(userId);
        return ids.isEmpty() ? null : ids.get(0);
    }

    // =========================================================
    // ⚠️ NOUVEAU : scoping unifié, utilisé par Tâches / Affectations /
    // Pointage pour restreindre visibilité + actions à "ses" chantiers.
    // =========================================================

    /**
     * IDs des chantiers auxquels l'utilisateur courant est limité :
     * - ADMIN : null (aucune restriction — accès à tout)
     * - CHEF_PROJET : ses chantiers en tant que chef de projet
     * - CHEF_CHANTIER : ses chantiers en tant que chef de chantier
     * - MAGASINIER : ses chantiers en tant que magasinier
     * - AGENT_SAISIE : son unique chantier (liste à 0 ou 1 élément)
     * - tout autre cas : liste vide (aucun accès)
     */
    public List<Long> getScopedChantierIds() {
        if (isAdmin()) {
            return null;
        }
        if (isChefProjet()) {
            return getChantierIdsAsChefProjet();
        }
        if (isChefChantier()) {
            return getChantierIdsAsChefChantier();
        }
        if (isMagasinier()) {
            return getChantierIdsAsMagasinier();
        }
        if (isAgentSaisie()) {
            Long siteId = getSiteIdAsAgentSaisie();
            return siteId == null ? List.of() : List.of(siteId);
        }
        return List.of();
    }

    /**
     * true si le chantier donné est dans le périmètre de l'utilisateur
     * courant (ou si celui-ci n'a aucune restriction — Admin).
     */
    public boolean isChantierInScope(Long chantierId) {
        List<Long> scoped = getScopedChantierIds();
        if (scoped == null) return true;
        return chantierId != null && scoped.contains(chantierId);
    }
}