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
    ResponseEntity<?> exportJournal(String format, String startDate, String endDate, String entityType);
    Map<String, Object> getJournalStatistiques();
    Page<JournalResponse> getJournalEntriesByEntity(String entityType, Long entityId, Pageable pageable);
}