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
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
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

    @Override
    @Transactional
    public SiteResponse createSite(SiteCreateRequest request) {
        log.info("Création d'un nouveau site: {}", request.getName());

        // Vérifier si la référence existe déjà
        if (request.getReference() != null && 
            siteRepository.existsByReference(request.getReference())) {
            throw new BadRequestException("Cette référence de site existe déjà");
        }

        // Vérifier que le client existe
        Client client = clientRepository.findById(request.getClientId())
            .orElseThrow(() -> new ResourceNotFoundException("Client", request.getClientId()));

        // Vérifier que le chef de projet existe
        User chefProjet = userRepository.findById(request.getChefProjetId())
            .orElseThrow(() -> new ResourceNotFoundException("Chef de projet", request.getChefProjetId()));

        // Créer le site
        Site site = siteMapper.toEntity(request);
        site.setClient(client);
        site.setChefProjet(chefProjet);

        // Ajouter les autres responsables si présents
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

        // Vérifier si la référence n'est pas déjà prise
        if (request.getReference() != null && 
            !site.getReference().equals(request.getReference()) &&
            siteRepository.existsByReference(request.getReference())) {
            throw new BadRequestException("Cette référence de site existe déjà");
        }

        // Mettre à jour les relations si nécessaire
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

        siteMapper.updateEntity(site, request);
        site = siteRepository.save(site);

        journalService.logAction(
            TypeAction.MODIFICATION,
            "SITE",
            site.getId(),
            "Mise à jour du site: " + site.getName(),
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
    public Page<SiteResponse> getAllSites(String status, Long clientId, String search, Pageable pageable) {
        log.debug("Récupération de tous les sites");
        
        StatutSite statut = null;
        if (status != null) {
            try {
                statut = StatutSite.valueOf(status);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Statut invalide: " + status);
            }
        }

        Page<Site> sites = siteRepository.findSitesWithFilters(statut, clientId, search, pageable);
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

        site.setStatus(newStatus);
        site = siteRepository.save(site);

        journalService.logAction(
            TypeAction.MODIFICATION,
            "SITE",
            site.getId(),
            "Changement de statut du site: " + site.getName() + " -> " + status,
            null
        );

        log.info("Statut du site mis à jour avec succès: {}", site.getName());
        return siteMapper.toResponse(site);
    }

    @Override
    public Page<SiteResponse> getMySites(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", email));

        log.debug("Récupération des sites de l'utilisateur: {}", email);
        
        // Récupérer les sites où l'utilisateur est impliqué
        // Note: Cette méthode serait plus complexe en réalité
        return siteRepository.findAll(pageable)
            .map(siteMapper::toResponse);
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