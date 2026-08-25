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

    public boolean isChefChantier() {
        return hasRole("CHEF_CHANTIER");
    }

    /**
     * IDs des chantiers dont l'utilisateur courant est Chef de Chantier.
     */
    public List<Long> getChantierIdsAsChefChantier() {
        Long userId = getCurrentUser().getId();
        return siteRepository.findIdsByChefChantierId(userId);
    }

    // =========================================================
    // ✅ NOUVEAU : Agent de Saisie (§1 cahier des charges — dashboard
    // scopé sur son unique chantier)
    // =========================================================

    public boolean isAgentSaisie() {
        return hasRole("AGENT_SAISIE");
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
}