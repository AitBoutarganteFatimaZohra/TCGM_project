package com.tcgm.service.impl;

import com.tcgm.dto.response.NotificationCountsResponse;
import com.tcgm.model.Site;
import com.tcgm.model.User;
import com.tcgm.model.enums.RoleName;
import com.tcgm.repository.AffectationRepository;
import com.tcgm.repository.RessourceRepository;
import com.tcgm.repository.SiteRepository;
import com.tcgm.repository.TacheRepository;
import com.tcgm.repository.UserRepository;
import com.tcgm.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final TacheRepository tacheRepository;
    private final RessourceRepository ressourceRepository;
    private final AffectationRepository affectationRepository;
    private final SiteRepository siteRepository;
    private final UserRepository userRepository;

    private User getCurrentUserOrNull() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return null;

        String email = authentication.getName();
        if (email == null || "SYSTEM".equals(email) || "anonymousUser".equals(email)) return null;
        return userRepository.findByEmail(email).orElse(null);
    }

    private boolean hasRole(User user, RoleName roleName) {
        return user != null && user.getRoles() != null &&
            user.getRoles().stream().anyMatch(role -> role.getName() == roleName);
    }

    @Override
    public NotificationCountsResponse getPendingCounts() {
        User currentUser = getCurrentUserOrNull();

        long taches = 0;
        long affectations = 0;
        long ressources = 0;
        long sites = 0;

        if (currentUser != null) {
            boolean isAdmin = hasRole(currentUser, RoleName.ADMIN);

            if (isAdmin) {
                // Vision globale : Administrateur peut valider les 4 circuits.
                sites = siteRepository.countPendingModifications();

                List<Long> allSiteIds = siteRepository.findAll().stream()
                    .map(Site::getId)
                    .collect(Collectors.toList());

                if (!allSiteIds.isEmpty()) {
                    taches = tacheRepository.countPendingByChantierIds(allSiteIds);

                    affectations = affectationRepository.countPendingByChantierIds(allSiteIds);
                    ressources = ressourceRepository.countPendingBySiteIds(allSiteIds);
                }
            } else if (hasRole(currentUser, RoleName.CHEF_PROJET)) {
                // Valide : Tâches (§2) et Affectations (§4), sur ses sites.
                List<Long> siteIds = siteRepository.findByChefProjetId(currentUser.getId()).stream()
                    .map(Site::getId)
                    .collect(Collectors.toList());

                if (!siteIds.isEmpty()) {
                    taches = tacheRepository.countPendingByChantierIds(siteIds);
                    affectations = affectationRepository.countPendingByChantierIds(siteIds);
                }
            } else if (hasRole(currentUser, RoleName.CHEF_CHANTIER)) {
                // Valide : Ressources (§3), sur ses sites.
                List<Long> siteIds = siteRepository.findByChefChantierId(currentUser.getId()).stream()
                    .map(Site::getId)
                    .collect(Collectors.toList());

                if (!siteIds.isEmpty()) {
                    ressources = ressourceRepository.countPendingBySiteIds(siteIds);
                }
            }
            // MAGASINIER, AGENT_SAISIE : aucune notification (ils soumettent,
            // ils ne valident jamais).
        }

        long total = taches + affectations + ressources + sites;

        return NotificationCountsResponse.builder()
            .tachesEnAttente(taches)
            .affectationsEnAttente(affectations)

            .ressourcesEnAttente(ressources)
            .sitesEnAttente(sites)
            .total(total)
            .build();
    }
}