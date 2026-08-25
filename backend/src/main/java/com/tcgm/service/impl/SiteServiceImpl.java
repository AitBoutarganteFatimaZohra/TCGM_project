package com.tcgm.service.impl;

import com.tcgm.dto.request.SiteCreateRequest;
import com.tcgm.dto.request.SiteUpdateRequest;
import com.tcgm.dto.response.SiteResponse;
import com.tcgm.dto.response.SiteDetailResponse;
import com.tcgm.exception.BadRequestException;
import com.tcgm.exception.ResourceNotFoundException;
import com.tcgm.mapper.SiteMapper;
import com.tcgm.model.Site;
import com.tcgm.model.Client;
import com.tcgm.model.User;
import com.tcgm.model.enums.RoleName;
import com.tcgm.model.enums.StatutSite;
import com.tcgm.repository.SiteRepository;
import com.tcgm.repository.ClientRepository;
import com.tcgm.repository.UserRepository;
import com.tcgm.service.SiteService;
import com.tcgm.service.JournalService;
import com.tcgm.model.enums.TypeAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SiteServiceImpl implements SiteService {

    private final SiteRepository siteRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final SiteMapper siteMapper;
    private final JournalService journalService;

    // =========================================================
    // ⚠️ NOUVEAU : rôle de l'utilisateur courant
    // =========================================================

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
    @Transactional

    public SiteResponse createSite(SiteCreateRequest request) {
        log.info("Création d'un nouveau site: {}", request.getName());

        if (request.getReference() != null &&
            siteRepository.existsByReference(request.getReference())) {
            throw new BadRequestException("Cette référence de site existe déjà");
        }

        Client client = clientRepository.findById(request.getClientId())
            .orElseThrow(() -> new ResourceNotFoundException("Client", request.getClientId()));

        User chefProjet = userRepository.findById(request.getChefProjetId())
            .orElseThrow(() -> new ResourceNotFoundException("Chef de projet", request.getChefProjetId()));

        Site site = siteMapper.toEntity(request);
        site.setClient(client);
        site.setChefProjet(chefProjet);

        if (request.getMagasinierId() != null) {
            User magasinier = userRepository.findById(request.getMagasinierId())
                .orElseThrow(() -> new ResourceNotFoundException("Magasinier", request.getMagasinierId()));
            site.setMagasinier(magasinier);
        }

        if (request.getAgentSaisieId() != null) {
            User agentSaisie = userRepository.findById(request.getAgentSaisieId())
                .orElseThrow(() -> new ResourceNotFoundException("Agent de saisie", request.getAgentSaisieId()));
            site.setAgentSaisie(agentSaisie);
        }

        if (request.getChefChantierId() != null) {
            User chefChantier = userRepository.findById(request.getChefChantierId())

                .orElseThrow(() -> new ResourceNotFoundException("Chef de chantier", request.getChefChantierId()));
            site.setChefChantier(chefChantier);
        }

        site = siteRepository.save(site);

        journalService.logAction(
            TypeAction.CREATION,
            "SITE",
            site.getId(),
            "Création du site: " + site.getName(),
            null
        );

        log.info("Site créé avec succès: {}", site.getName());
        return siteMapper.toResponse(site);
    }

    @Override
    @Transactional
    public SiteResponse updateSite(Long id, SiteUpdateRequest request) {
        log.info("Mise à jour du site ID: {}", id);

        Site site = siteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Site", id));

        if (request.getReference() != null &&
            !site.getReference().equals(request.getReference()) &&
            siteRepository.existsByReference(request.getReference())) {
            throw new BadRequestException("Cette référence de site existe déjà");
        }


        if (request.getClientId() != null) {
            Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", request.getClientId()));
            site.setClient(client);
        }

        if (request.getChefProjetId() != null) {
            User chefProjet = userRepository.findById(request.getChefProjetId())
                .orElseThrow(() -> new ResourceNotFoundException("Chef de projet", request.getChefProjetId()));
            site.setChefProjet(chefProjet);
        }

        if (request.getMagasinierId() != null) {
            User magasinier = userRepository.findById(request.getMagasinierId())
                .orElseThrow(() -> new ResourceNotFoundException("Magasinier", request.getMagasinierId()));
            site.setMagasinier(magasinier);
        }

        if (request.getAgentSaisieId() != null) {
            User agentSaisie = userRepository.findById(request.getAgentSaisieId())
                .orElseThrow(() -> new ResourceNotFoundException("Agent de saisie", request.getAgentSaisieId()));
            site.setAgentSaisie(agentSaisie);
        }

        if (request.getChefChantierId() != null) {
            User chefChantier = userRepository.findById(request.getChefChantierId())
                .orElseThrow(() -> new ResourceNotFoundException("Chef de chantier", request.getChefChantierId()));
            site.setChefChantier(chefChantier);
        }

        // =========================================================
        // ⚠️ NOUVEAU — Étape 1 (§5) : si c'est un Chef de Projet (pas

        // Admin) qui change le statut et/ou les dates, on stocke la
        // proposition dans les champs pending* au lieu de l'appliquer.
        // On remet dans `request` la valeur ACTUELLE (pas null) pour ces
        // 3 champs, afin que siteMapper.updateEntity ne les écrase pas.
        // =========================================================

        User currentUser = getCurrentUserOrNull();
        boolean isChefProjetOnly = hasRole(currentUser, RoleName.CHEF_PROJET)
            && !hasRole(currentUser, RoleName.ADMIN);

        boolean statusChanged = request.getStatus() != null
            && (site.getStatus() == null || !site.getStatus().name().equalsIgnoreCase(request.getStatus()));
        boolean startDateChanged = request.getStartDate() != null
            && !request.getStartDate().equals(site.getStartDate());
        boolean endDateChanged = request.getEndDate() != null
            && !request.getEndDate().equals(site.getEndDate());
        boolean isMajorChange = statusChanged || startDateChanged || endDateChanged;

        if (isChefProjetOnly && isMajorChange) {
            if (statusChanged) {
                site.setPendingStatus(StatutSite.valueOf(request.getStatus().toUpperCase()));
                request.setStatus(site.getStatus() != null ? site.getStatus().name() : null);
            }
            if (startDateChanged) {
                site.setPendingStartDate(request.getStartDate());
                request.setStartDate(site.getStartDate());
            }
            if (endDateChanged) {
                site.setPendingEndDate(request.getEndDate());
                request.setEndDate(site.getEndDate());
            }
            site.setMotifRejet(null);

        }

        siteMapper.updateEntity(site, request);
        site = siteRepository.save(site);

        journalService.logAction(
            TypeAction.MODIFICATION,
            "SITE",
            site.getId(),
            "Mise à jour du site: " + site.getName()
                + (isChefProjetOnly && isMajorChange ? " (modification majeure en attente de validation)" : ""),
            null
        );

        log.info("Site mis à jour avec succès: {}", site.getName());
        return siteMapper.toResponse(site);
    }

    @Override
    public SiteDetailResponse getSiteById(Long id) {
        log.debug("Récupération du site ID: {}", id);
        Site site = siteRepository.findByIdWithRelations(id)
            .orElseThrow(() -> new ResourceNotFoundException("Site", id));
        return siteMapper.toDetailResponse(site);
    }

    @Override
    public Page<SiteResponse> getAllSites(String status, Long clientId, String search,
                                           LocalDateTime periodStart, LocalDateTime periodEnd,
                                           Long responsableId, Pageable pageable) {
        log.debug("Récupération de tous les sites");


        StatutSite statut = null;
        if (status != null) {
            try {
                statut = StatutSite.valueOf(status);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Statut invalide: " + status);
            }
        }

        Page<Site> sites = siteRepository.findSitesWithFilters(
            statut, clientId, search, periodStart, periodEnd, responsableId, pageable);
        return sites.map(siteMapper::toResponse);
    }

    @Override
    @Transactional
    public void deleteSite(Long id) {
        log.info("Suppression du site ID: {}", id);

        Site site = siteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Site", id));

        journalService.logAction(
            TypeAction.SUPPRESSION,
            "SITE",
            site.getId(),
            "Suppression du site: " + site.getName(),
            null
        );

        siteRepository.delete(site);
        log.info("Site supprimé avec succès: {}", site.getName());

    }

    @Override
    @Transactional
    public SiteResponse updateSiteStatus(Long id, String status) {
        log.info("Mise à jour du statut du site ID: {} vers {}", id, status);

        Site site = siteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Site", id));

        StatutSite newStatus;
        try {
            newStatus = StatutSite.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Statut invalide: " + status);
        }

        // ⚠️ NOUVEAU : même logique que updateSite — un Chef de Projet
        // (pas Admin) passe par la proposition en attente.
        User currentUser = getCurrentUserOrNull();
        boolean isChefProjetOnly = hasRole(currentUser, RoleName.CHEF_PROJET)
            && !hasRole(currentUser, RoleName.ADMIN);

        if (isChefProjetOnly) {
            site.setPendingStatus(newStatus);
            site.setMotifRejet(null);
        } else {
            site.setStatus(newStatus);
        }

        site = siteRepository.save(site);


        journalService.logAction(
            TypeAction.MODIFICATION,
            "SITE",
            site.getId(),
            "Changement de statut du site: " + site.getName() + " -> " + status
                + (isChefProjetOnly ? " (en attente de validation)" : ""),
            null
        );

        log.info("Statut du site mis à jour avec succès: {}", site.getName());
        return siteMapper.toResponse(site);
    }

    // =========================================================
    // ⚠️ NOUVEAU — Étape 2 (§5) : Administrateur valide
    // =========================================================

    @Override
    @Transactional
    public SiteResponse validerModificationSite(Long id) {
        Site site = siteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Site", id));

        boolean hasPending = site.getPendingStatus() != null
            || site.getPendingStartDate() != null
            || site.getPendingEndDate() != null;

        if (!hasPending) {
            throw new BadRequestException("Aucune modification majeure en attente pour ce site.");
        }

        if (site.getPendingStatus() != null) {

            site.setStatus(site.getPendingStatus());
            site.setPendingStatus(null);
        }
        if (site.getPendingStartDate() != null) {
            site.setStartDate(site.getPendingStartDate());
            site.setPendingStartDate(null);
        }
        if (site.getPendingEndDate() != null) {
            site.setEndDate(site.getPendingEndDate());
            site.setPendingEndDate(null);
        }
        site.setMotifRejet(null);

        site = siteRepository.save(site);

        journalService.logAction(
            TypeAction.MODIFICATION,
            "SITE",
            site.getId(),
            "Validation de la modification majeure du site: " + site.getName(),
            null
        );

        return siteMapper.toResponse(site);
    }

    // =========================================================
    // ⚠️ NOUVEAU — Administrateur rejette
    // =========================================================

    @Override
    @Transactional

    public SiteResponse rejeterModificationSite(Long id, String motif) {
        Site site = siteRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Site", id));

        boolean hasPending = site.getPendingStatus() != null
            || site.getPendingStartDate() != null
            || site.getPendingEndDate() != null;

        if (!hasPending) {
            throw new BadRequestException("Aucune modification majeure en attente pour ce site.");
        }

        site.setPendingStatus(null);
        site.setPendingStartDate(null);
        site.setPendingEndDate(null);
        site.setMotifRejet(motif);

        site = siteRepository.save(site);

        journalService.logAction(
            TypeAction.MODIFICATION,
            "SITE",
            site.getId(),
            "Rejet de la modification majeure du site: " + site.getName()
                + (motif != null && !motif.isBlank() ? " — motif: " + motif : ""),
            null
        );

        return siteMapper.toResponse(site);
    }

    @Override

    public Page<SiteResponse> getMySites(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", email));

        log.debug("Récupération des sites de l'utilisateur: {} (id={})", email, user.getId());

        List<Site> sites = siteRepository.findSitesByUserId(user.getId());
        List<SiteResponse> content = siteMapper.toResponseList(sites);

        return new PageImpl<>(content, pageable, content.size());
    }

    @Override
    public Map<String, Object> getGlobalStatistiques() {
        log.debug("Récupération des statistiques globales des sites");

        Map<String, Object> stats = new HashMap<>();

        long totalSites = siteRepository.count();
        long sitesEnCours = siteRepository.countByStatus(StatutSite.EN_COURS);
        long sitesTermines = siteRepository.countByStatus(StatutSite.TERMINE);
        long sitesPlanifies = siteRepository.countByStatus(StatutSite.PLANIFIE);
        long sitesSuspendus = siteRepository.countByStatus(StatutSite.SUSPENDU);

        stats.put("totalSites", totalSites);
        stats.put("sitesEnCours", sitesEnCours);
        stats.put("sitesTermines", sitesTermines);
        stats.put("sitesPlanifies", sitesPlanifies);
        stats.put("sitesSuspendus", sitesSuspendus);

        return stats;

    }
}