package com.tcgm.service;

import com.tcgm.dto.request.JournalFilterRequest;
import com.tcgm.dto.response.JournalResponse;
import com.tcgm.model.enums.TypeAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface JournalService {
    void logAction(TypeAction actionType, String entityType, Long entityId, String details, String ipAddress);
    Page<JournalResponse> getJournalEntries(JournalFilterRequest filter, Pageable pageable);
    JournalResponse getJournalEntryById(Long id);

    // ✅ siteId ajouté : indispensable pour que le Magasinier n'exporte
    // que le journal de son propre site, et pas celui de toute l'entreprise.
    ResponseEntity<?> exportJournal(String format, String startDate, String endDate, String entityType, Long siteId);

    Map<String, Object> getJournalStatistiques();
    Page<JournalResponse> getJournalEntriesByEntity(String entityType, Long entityId, Pageable pageable);

    // =========================================================
    // VALIDATION (cahier des charges §6.7)
    // =========================================================
    // validatorEmail : l'email de l'utilisateur connecté (récupéré depuis
    // Authentication.getName() côté controller), utilisé pour retrouver
    // le User à enregistrer comme validateur.
    JournalResponse validateEntry(Long id, String validatorEmail);
    JournalResponse rejectEntry(Long id, String validatorEmail);
}