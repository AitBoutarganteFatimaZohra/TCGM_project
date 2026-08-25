package com.tcgm.service.impl;

import com.tcgm.dto.response.AlerteResponse;
import com.tcgm.exception.BadRequestException;
import com.tcgm.exception.ResourceNotFoundException;
import com.tcgm.model.Alerte;
import com.tcgm.model.Site;
import com.tcgm.model.User;
import com.tcgm.model.enums.StatutAlerte;
import com.tcgm.model.enums.StatutSite;
import com.tcgm.model.enums.TypeAlerte;
import com.tcgm.repository.AlerteRepository;
import com.tcgm.repository.SiteRepository;
import com.tcgm.repository.UserRepository;
import com.tcgm.service.AlerteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlerteServiceImpl implements AlerteService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AlerteRepository alerteRepository;
    private final SiteRepository siteRepository;
    private final UserRepository userRepository;

    // =========================================================
    // JOB DE DÉTECTION AUTOMATIQUE
    // Tourne toutes les 5 minutes (300000 ms). Ajuste selon besoin.
    // ⚠️ Nécessite @EnableScheduling sur la classe principale
    // (ex: @SpringBootApplication + @EnableScheduling).
    // =========================================================
    @Override
    @Transactional
    @Scheduled(fixedRate = 300000)
    public void detecterEtResoudreAlertes() {
        log.debug("Lancement de la détection automatique des alertes");

        List<Site> sites = siteRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (Site site : sites) {
            // ---- Alerte : chantier en retard ----
            boolean enRetard = site.getStatus() == StatutSite.EN_COURS
                    && site.getEndDate() != null
                    && site.getEndDate().isBefore(now);

            String messageRetard = site.getEndDate() != null
                    ? "Chantier en retard (fin prévue le " + site.getEndDate().format(DATE_FORMAT) + ")"
                    : "Chantier en retard";

            traiterCondition(site, TypeAlerte.RETARD, enRetard, messageRetard);

            // ---- Alerte : chantier sans Chef de Chantier ----
            boolean sansChefChantier = site.getChefChantier() == null
                    && site.getStatus() != StatutSite.TERMINE;

            traiterCondition(site, TypeAlerte.SANS_CHEF_CHANTIER, sansChefChantier,
                    "Chantier sans Chef de Chantier assigné");
        }

        log.debug("Détection des alertes terminée");
    }

    /**
     * Compare l'état actuel (conditionActive) à l'alerte existante en base :
     * - condition vraie + pas d'alerte active existante  -> on en crée une
     * - condition fausse + alerte active existante       -> on la résout automatiquement
     * - sinon                                             -> rien à faire (déjà à jour)
     */
    private void traiterCondition(Site site, TypeAlerte type, boolean conditionActive, String message) {
        Optional<Alerte> existante =
                alerteRepository.findBySiteIdAndTypeAndStatut(site.getId(), type, StatutAlerte.ACTIVE);

        if (conditionActive && existante.isEmpty()) {
            Alerte alerte = Alerte.builder()
                    .site(site)
                    .type(type)
                    .statut(StatutAlerte.ACTIVE)
                    .message(message)
                    .createdAt(LocalDateTime.now())
                    .build();
            alerteRepository.save(alerte);
            log.info("Nouvelle alerte créée : {} pour le chantier '{}'", type, site.getName());

        } else if (!conditionActive && existante.isPresent()) {
            Alerte alerte = existante.get();
            alerte.setStatut(StatutAlerte.RESOLUE);
            alerte.setResolvedAt(LocalDateTime.now());
            alerte.setResolvedBy(null); // résolution automatique
            alerteRepository.save(alerte);
            log.info("Alerte résolue automatiquement : {} pour le chantier '{}'", type, site.getName());
        }
        // sinon : rien à faire, l'état en base correspond déjà à la réalité
    }

    // =========================================================
    // CONSULTATION
    // =========================================================
    @Override
    public List<AlerteResponse> getAlertesForCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", email));

        List<Alerte> alertes =
                alerteRepository.findBySiteChefProjetIdAndStatut(user.getId(), StatutAlerte.ACTIVE);

        return alertes.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // =========================================================
    // RÉSOLUTION MANUELLE
    // =========================================================
    @Override
    @Transactional
    public AlerteResponse resolveAlerte(Long alerteId, String email) {
        Alerte alerte = alerteRepository.findById(alerteId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerte", alerteId));

        if (alerte.getStatut() == StatutAlerte.RESOLUE) {
            throw new BadRequestException("Cette alerte est déjà résolue");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "email", email));

        alerte.setStatut(StatutAlerte.RESOLUE);
        alerte.setResolvedAt(LocalDateTime.now());
        alerte.setResolvedBy(user);
        alerte = alerteRepository.save(alerte);

        log.info("Alerte {} résolue manuellement par {}", alerteId, email);

        return toResponse(alerte);
    }

    // =========================================================
    // MAPPING
    // =========================================================
    private AlerteResponse toResponse(Alerte alerte) {
        return AlerteResponse.builder()
                .id(alerte.getId())
                .type(alerte.getType())
                .statut(alerte.getStatut())
                .message(alerte.getMessage())
                .siteId(alerte.getSite().getId())
                .siteName(alerte.getSite().getName())
                .createdAt(alerte.getCreatedAt())
                .resolvedAt(alerte.getResolvedAt())
                .resolvedByName(alerte.getResolvedBy() != null
                        ? (alerte.getResolvedBy().getFirstName() + " " + alerte.getResolvedBy().getLastName())
                        : null)
                .build();
    }
}