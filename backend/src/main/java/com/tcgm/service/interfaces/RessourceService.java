package com.tcgm.service;

import com.tcgm.dto.request.RessourceCreateRequest;
import com.tcgm.dto.request.RessourceUpdateRequest;
import com.tcgm.dto.response.RessourceResponse;
import com.tcgm.exception.BadRequestException;
import com.tcgm.model.Ressource;
import com.tcgm.model.Site;
import com.tcgm.model.enums.TypeAction;
import com.tcgm.repository.RessourceRepository;
import com.tcgm.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RessourceService {

    private final RessourceRepository ressourceRepository;
    private final SiteRepository siteRepository;
    private final JournalService journalService;

    public List<RessourceResponse> getAll() {
        return ressourceRepository.findAll()
                .stream()
                .map(RessourceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public RessourceResponse getById(Long id) {
        Ressource r = ressourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource introuvable avec l'id " + id));
        return RessourceResponse.fromEntity(r);
    }

    public List<RessourceResponse> getBySite(Long siteId, String statut, String type, String search) {
        List<Ressource> ressources;

        if (search != null && !search.isBlank()) {
            ressources = ressourceRepository.findBySiteIdAndNomContainingIgnoreCase(siteId, search);
        } else if (statut != null && !statut.isBlank()) {
            ressources = ressourceRepository.findBySiteIdAndStatut(siteId, Ressource.StatutRessource.valueOf(statut));
        } else if (type != null && !type.isBlank()) {
            ressources = ressourceRepository.findBySiteIdAndType(siteId, Ressource.TypeRessource.valueOf(type));
        } else {
            ressources = ressourceRepository.findBySiteId(siteId);
        }

        return ressources.stream()
                .map(RessourceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public RessourceResponse create(RessourceCreateRequest request) {
        Site site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new RuntimeException("Site introuvable avec l'id " + request.getSiteId()));

        Ressource r = Ressource.builder()
                .nom(request.getNom())
                .type(request.getType())
                .quantite(request.getQuantite())
                .unite(request.getUnite())
                .statut(request.getStatut() != null ? request.getStatut() : Ressource.StatutRessource.DISPONIBLE)
                .description(request.getDescription())
                .codeInterne(request.getCodeInterne())
                .numeroSerie(request.getNumeroSerie())
                .seuilAlerte(request.getSeuilAlerte())
                .site(site)
                .build();

        Ressource saved = ressourceRepository.save(r);

        journalService.logAction(
                TypeAction.CREATION,
                "RESSOURCE",
                saved.getId(),
                "Création de la ressource: " + saved.getNom(),
                null
        );

        return RessourceResponse.fromEntity(saved);
    }

    public RessourceResponse update(Long id, RessourceUpdateRequest request) {
        Ressource r = ressourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource introuvable avec l'id " + id));

        Site site = siteRepository.findById(request.getSiteId())
                .orElseThrow(() -> new RuntimeException("Site introuvable avec l'id " + request.getSiteId()));

        r.setNom(request.getNom());
        r.setType(request.getType());
        r.setQuantite(request.getQuantite());
        r.setUnite(request.getUnite());
        if (request.getStatut() != null) {
            r.setStatut(request.getStatut());
        }
        r.setDescription(request.getDescription());
        r.setCodeInterne(request.getCodeInterne());
        r.setNumeroSerie(request.getNumeroSerie());
        r.setSeuilAlerte(request.getSeuilAlerte());
        r.setSite(site);

        Ressource saved = ressourceRepository.save(r);

        journalService.logAction(
                TypeAction.MODIFICATION,
                "RESSOURCE",
                saved.getId(),
                "Modification de la ressource: " + saved.getNom(),
                null
        );

        return RessourceResponse.fromEntity(saved);
    }

    // Réservé à l'Administrateur désormais (override direct, hors circuit) — voir Controller
    public RessourceResponse updateStatut(Long id, Ressource.StatutRessource statut) {
        Ressource r = ressourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource introuvable avec l'id " + id));
        r.setStatut(statut);
        r.setPendingStatut(null);
        r.setMotifRejet(null);
        Ressource saved = ressourceRepository.save(r);

        journalService.logAction(
                TypeAction.MODIFICATION,
                "RESSOURCE",
                saved.getId(),
                "Changement de statut (override Admin) de la ressource " + saved.getNom() + " -> " + statut,
                null
        );

        return RessourceResponse.fromEntity(saved);
    }

    // ⚠️ NOUVEAU — Étape 1 du circuit (§3) : le Magasinier propose un changement de statut
    public RessourceResponse proposerStatut(Long id, Ressource.StatutRessource statut) {
        Ressource r = ressourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource introuvable avec l'id " + id));

        r.setPendingStatut(statut);
        r.setMotifRejet(null);
        Ressource saved = ressourceRepository.save(r);

        journalService.logAction(
                TypeAction.MODIFICATION,
                "RESSOURCE",
                saved.getId(),
                "Proposition de changement de statut pour " + saved.getNom() + " -> " + statut,
                null
        );

        return RessourceResponse.fromEntity(saved);
    }

    // ⚠️ NOUVEAU — Étape 2 (niveau 1 ou 2) : Chef de Chantier ou Chef de Projet valide
    public RessourceResponse validerStatut(Long id) {
        Ressource r = ressourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource introuvable avec l'id " + id));

        if (r.getPendingStatut() == null) {
            throw new BadRequestException("Aucun changement de statut en attente pour cette ressource");
        }

        r.setStatut(r.getPendingStatut());
        r.setPendingStatut(null);
        r.setMotifRejet(null);
        Ressource saved = ressourceRepository.save(r);

        journalService.logAction(
                TypeAction.MODIFICATION,
                "RESSOURCE",
                saved.getId(),
                "Validation du changement de statut de " + saved.getNom() + " -> " + saved.getStatut(),
                null
        );

        return RessourceResponse.fromEntity(saved);
    }

    // ⚠️ NOUVEAU — Chef de Chantier ou Chef de Projet rejette
    public RessourceResponse rejeterStatut(Long id, String motif) {
        Ressource r = ressourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource introuvable avec l'id " + id));

        if (r.getPendingStatut() == null) {
            throw new BadRequestException("Aucun changement de statut en attente pour cette ressource");
        }

        r.setPendingStatut(null);
        r.setMotifRejet(motif);
        Ressource saved = ressourceRepository.save(r);

        journalService.logAction(
                TypeAction.MODIFICATION,
                "RESSOURCE",
                saved.getId(),
                "Rejet du changement de statut proposé pour " + saved.getNom()
                        + (motif != null && !motif.isBlank() ? " — motif: " + motif : ""),
                null
        );

        return RessourceResponse.fromEntity(saved);
    }

    public void delete(Long id) {
        Ressource r = ressourceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ressource introuvable avec l'id " + id));

        journalService.logAction(
                TypeAction.SUPPRESSION,
                "RESSOURCE",
                id,
                "Suppression de la ressource: " + r.getNom(),
                null
        );

        ressourceRepository.deleteById(id);
    }
}