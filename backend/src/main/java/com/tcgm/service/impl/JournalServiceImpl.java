package com.tcgm.service.impl;

import com.tcgm.dto.request.JournalFilterRequest;
import com.tcgm.dto.response.JournalResponse;
import com.tcgm.exception.ResourceNotFoundException;
import com.tcgm.mapper.JournalMapper;
import com.tcgm.model.JournalOperation;
import com.tcgm.model.User;
import com.tcgm.model.enums.TypeAction;
import com.tcgm.repository.JournalOperationRepository;
import com.tcgm.repository.UserRepository;
import com.tcgm.service.JournalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class JournalServiceImpl implements JournalService {

    private final JournalOperationRepository journalRepository;
    private final UserRepository userRepository;
    private final JournalMapper journalMapper;

    @Override
    @Transactional
    public void logAction(TypeAction actionType, String entityType, Long entityId, String details, String ipAddress) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication() != null ?
                SecurityContextHolder.getContext().getAuthentication().getName() : "SYSTEM";

            User user = null;
            if (!"SYSTEM".equals(email)) {
                user = userRepository.findByEmail(email).orElse(null);
            }

            JournalOperation operation = JournalOperation.builder()
                .actionType(actionType)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .ipAddress(ipAddress != null ? ipAddress : "N/A")
                .user(user)
                .build();

            journalRepository.save(operation);
            log.debug("Action journalisée: {} - {} - {}", actionType, entityType, details);
        } catch (Exception e) {
            log.error("Erreur lors de la journalisation de l'action: {}", e.getMessage());
        }
    }

    @Override
    public Page<JournalResponse> getJournalEntries(JournalFilterRequest filter, Pageable pageable) {
        log.debug("Récupération des entrées du journal avec filtres");

        TypeAction actionType = null;
        if (filter.getActionType() != null) {
            try {
                actionType = TypeAction.valueOf(filter.getActionType());
            } catch (IllegalArgumentException e) {
                // Ignorer si le type d'action n'existe pas
            }
        }

        Page<JournalOperation> operations = journalRepository.findOperationsWithFilters(
            actionType,
            filter.getEntityType(),
            filter.getEntityId(),
            filter.getUserId(),
            filter.getSearch(),
            pageable
        );

        return operations.map(journalMapper::toResponse);
    }

    @Override
    public JournalResponse getJournalEntryById(Long id) {
        log.debug("Récupération de l'entrée du journal ID: {}", id);
        JournalOperation operation = journalRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Entrée du journal", id));
        return journalMapper.toResponse(operation);
    }

    @Override
    public ResponseEntity<?> exportJournal(String format, String startDate, String endDate, String entityType) {
        log.info("Export du journal au format {} pour la période du {} au {}", format, startDate, endDate);

        // TODO: Implémenter l'export PDF/Excel
        // Pour l'instant, retourner un message
        Map<String, String> response = new HashMap<>();
        response.put("message", "Export au format " + format + " - Fonctionnalité en cours de développement");
        response.put("status", "INFO");
        
        return ResponseEntity.ok(response);
    }

    @Override
    public Map<String, Object> getJournalStatistiques() {
        log.debug("Récupération des statistiques du journal");

        Map<String, Object> stats = new HashMap<>();
        
        long totalOperations = journalRepository.count();
        
        // Compter par type d'action
        var actionsCount = journalRepository.countOperationsByActionType();
        Map<String, Long> actionsStats = new HashMap<>();
        for (Object[] row : actionsCount) {
            actionsStats.put(row[0].toString(), (Long) row[1]);
        }

        // Compter par type d'entité
        var entitiesCount = journalRepository.countOperationsByEntityType();
        Map<String, Long> entitiesStats = new HashMap<>();
        for (Object[] row : entitiesCount) {
            entitiesStats.put(row[0].toString(), (Long) row[1]);
        }

        stats.put("totalOperations", totalOperations);
        stats.put("actionsStats", actionsStats);
        stats.put("entitiesStats", entitiesStats);

        return stats;
    }

    @Override
    public Page<JournalResponse> getJournalEntriesByEntity(String entityType, Long entityId, Pageable pageable) {
        log.debug("Récupération des entrées du journal pour l'entité {}: {}", entityType, entityId);

        Page<JournalOperation> operations = journalRepository.findByEntityTypeAndEntityId(
            entityType, entityId, pageable
        );

        return operations.map(journalMapper::toResponse);
    }
}