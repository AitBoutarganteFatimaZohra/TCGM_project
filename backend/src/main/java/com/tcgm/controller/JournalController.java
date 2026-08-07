package com.tcgm.controller;

import com.tcgm.dto.request.JournalFilterRequest;
import com.tcgm.dto.response.JournalResponse;
import com.tcgm.service.JournalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/journal")
@RequiredArgsConstructor
public class JournalController {

    private final JournalService journalService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<Page<JournalResponse>> getJournalEntries(
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        JournalFilterRequest filter = JournalFilterRequest.builder()
            .actionType(actionType)
            .entityType(entityType)
            .entityId(entityId)
            .userId(userId)
            .startDate(startDate)
            .endDate(endDate)
            .search(search)
            .build();
        return ResponseEntity.ok(journalService.getJournalEntries(filter, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<JournalResponse> getJournalEntryById(@PathVariable Long id) {
        return ResponseEntity.ok(journalService.getJournalEntryById(id));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET')")
    public ResponseEntity<?> exportJournal(
            @RequestParam String format,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String entityType) {
        return journalService.exportJournal(format, startDate, endDate, entityType);
    }

    @GetMapping("/statistiques")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getJournalStatistiques() {
        return ResponseEntity.ok(journalService.getJournalStatistiques());
    }

    @GetMapping("/entities/{entityType}/{entityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF_PROJET', 'CHEF_CHANTIER')")
    public ResponseEntity<Page<JournalResponse>> getJournalEntriesByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            Pageable pageable) {
        return ResponseEntity.ok(journalService.getJournalEntriesByEntity(entityType, entityId, pageable));
    }
}