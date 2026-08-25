package com.tcgm.service.impl;

import com.tcgm.dto.request.JournalFilterRequest;
import com.tcgm.dto.response.JournalResponse;
import com.tcgm.exception.ResourceNotFoundException;
import com.tcgm.mapper.JournalMapper;
import com.tcgm.model.JournalOperation;
import com.tcgm.model.Site;
import com.tcgm.model.User;
import com.tcgm.model.enums.RoleName;
import com.tcgm.model.enums.StatutValidation;
import com.tcgm.model.enums.TypeAction;
import com.tcgm.repository.JournalOperationRepository;
import com.tcgm.repository.SiteRepository;
import com.tcgm.repository.UserRepository;
import com.tcgm.service.JournalService;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JournalServiceImpl implements JournalService {

    private final JournalOperationRepository journalRepository;
    private final UserRepository userRepository;
    private final SiteRepository siteRepository;
    private final JournalMapper journalMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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
                .status(computeInitialStatus(actionType, user))
                .build();

            journalRepository.save(operation);
            log.debug("Action journalisée: {} - {} - {}", actionType, entityType, details);
        } catch (Exception e) {
            log.error("Erreur lors de la journalisation de l'action: {}", e.getMessage());
        }
    }

    private StatutValidation computeInitialStatus(TypeAction actionType, User user) {
        boolean isAuthEvent = actionType == TypeAction.CONNEXION || actionType == TypeAction.DECONNEXION;
        boolean isAdmin = hasRole(user, RoleName.ADMIN);

        if (isAuthEvent || isAdmin) {
            return StatutValidation.VALIDE;
        }
        return StatutValidation.EN_ATTENTE;
    }

    // =========================================================
    // PÉRIMÈTRE DU JOURNAL PAR RÔLE
    // =========================================================

    private boolean hasRole(User user, RoleName roleName) {
        return user != null && user.getRoles() != null &&
            user.getRoles().stream().anyMatch(role -> role.getName() == roleName);
    }

    private User getCurrentUserOrNull() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return null;
        String email = authentication.getName();
        if (email == null || "SYSTEM".equals(email) || "anonymousUser".equals(email)) return null;
        return userRepository.findByEmail(email).orElse(null);
    }

    private void addUserIdIfPresent(Set<Long> ids, User user) {
        if (user != null && user.getId() != null) {
            ids.add(user.getId());
        }
    }

    /**
     * Calcule la liste des utilisateurs dont les actions sont visibles pour
     * currentUser, selon son rôle (§ périmètre du journal) :
     * - Chef de Projet : lui-même + tous les responsables (chef de chantier,
     *   magasinier, agent de saisie) affectés à SES sites.
     * - Chef de Chantier : lui-même + le Magasinier et l'Agent de Saisie de
     *   SON site (🔧 CORRIGÉ : Magasinier ajouté — le Chef de Chantier est
     *   le validateur niveau 1 de ses actions sur les ressources, il doit
     *   voir ses opérations dans le journal pour les confirmer).
     * - Agent de Saisie : lui-même + le Chef de Chantier et le Chef de
     *   Projet de SON site (ce sont les deux rôles habilités à
     *   valider/rejeter ses dossiers de pointage).
     * - Magasinier : lui-même uniquement.
     * L'ADMIN n'appelle jamais cette méthode (vision globale non filtrée).
     */
    private List<Long> computeAllowedUserIds(User currentUser) {
        Set<Long> ids = new HashSet<>();
        addUserIdIfPresent(ids, currentUser);

        if (hasRole(currentUser, RoleName.CHEF_PROJET)) {
            List<Site> sites = siteRepository.findByChefProjetId(currentUser.getId());
            for (Site site : sites) {
                addUserIdIfPresent(ids, site.getChefChantier());
                addUserIdIfPresent(ids, site.getMagasinier());
                addUserIdIfPresent(ids, site.getAgentSaisie());
            }
        } else if (hasRole(currentUser, RoleName.CHEF_CHANTIER)) {
            List<Site> sites = siteRepository.findByChefChantierId(currentUser.getId());
            for (Site site : sites) {
                addUserIdIfPresent(ids, site.getMagasinier());
                addUserIdIfPresent(ids, site.getAgentSaisie());
            }
        } else if (hasRole(currentUser, RoleName.AGENT_SAISIE)) {
            List<Long> siteIds = siteRepository.findIdsByAgentSaisieId(currentUser.getId());
            for (Long siteId : siteIds) {
                siteRepository.findById(siteId).ifPresent(site -> {
                    addUserIdIfPresent(ids, site.getChefChantier());
                    addUserIdIfPresent(ids, site.getChefProjet());
                });
            }
        }
        // MAGASINIER : seulement lui-même (déjà ajouté ci-dessus).

        return new ArrayList<>(ids);
    }

    /**
     * Même logique que computeAllowedUserIds, mais restreinte à un site
     * précis — utilisée pour l'export quand un siteId est fourni.
     * Si le site demandé n'appartient pas au périmètre de currentUser,
     * on renvoie une liste vide (aucune fuite de données).
     */
    private List<Long> computeAllowedUserIdsForSite(User currentUser, Long siteId) {
        Site site = siteRepository.findById(siteId).orElse(null);
        if (site == null) {
            return new ArrayList<>();
        }

        Set<Long> ids = new HashSet<>();

        boolean isChefProjetDuSite = site.getChefProjet() != null
            && currentUser.getId().equals(site.getChefProjet().getId());
        boolean isChefChantierDuSite = site.getChefChantier() != null
            && currentUser.getId().equals(site.getChefChantier().getId());
        boolean isMagasinierDuSite = site.getMagasinier() != null
            && currentUser.getId().equals(site.getMagasinier().getId());
        boolean isAgentSaisieDuSite = site.getAgentSaisie() != null
            && currentUser.getId().equals(site.getAgentSaisie().getId());

        if (hasRole(currentUser, RoleName.CHEF_PROJET) && isChefProjetDuSite) {
            addUserIdIfPresent(ids, currentUser);
            addUserIdIfPresent(ids, site.getChefChantier());
            addUserIdIfPresent(ids, site.getMagasinier());
            addUserIdIfPresent(ids, site.getAgentSaisie());
        } else if (hasRole(currentUser, RoleName.CHEF_CHANTIER) && isChefChantierDuSite) {
            addUserIdIfPresent(ids, currentUser);
            // 🔧 CORRIGÉ : Magasinier ajouté (voir computeAllowedUserIds)
            addUserIdIfPresent(ids, site.getMagasinier());
            addUserIdIfPresent(ids, site.getAgentSaisie());
        } else if (hasRole(currentUser, RoleName.AGENT_SAISIE) && isAgentSaisieDuSite) {
            addUserIdIfPresent(ids, currentUser);
            addUserIdIfPresent(ids, site.getChefChantier());
            addUserIdIfPresent(ids, site.getChefProjet());
        } else if (isMagasinierDuSite) {
            addUserIdIfPresent(ids, currentUser);
        }
        // Sinon : site hors périmètre de currentUser -> liste vide.

        return new ArrayList<>(ids);
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

        User currentUser = getCurrentUserOrNull();
        boolean isAdmin = hasRole(currentUser, RoleName.ADMIN);

        Page<JournalOperation> operations;

        if (currentUser == null || isAdmin) {
            if (filter.getSiteId() != null) {
                operations = journalRepository.findOperationsWithFiltersAndSite(
                    actionType, filter.getEntityType(), filter.getEntityId(), filter.getUserId(),
                    filter.getSearch(), filter.getSiteId(), pageable);
            } else {
                operations = journalRepository.findOperationsWithFilters(
                    actionType, filter.getEntityType(), filter.getEntityId(), filter.getUserId(),
                    filter.getSearch(), pageable);
            }
        } else {
            List<Long> allowedUserIds = filter.getSiteId() != null
                ? computeAllowedUserIdsForSite(currentUser, filter.getSiteId())
                : computeAllowedUserIds(currentUser);

            if (filter.getUserId() != null && !allowedUserIds.contains(filter.getUserId())) {
                return Page.empty(pageable);
            }

            if (allowedUserIds.isEmpty()) {
                return Page.empty(pageable);
            }

            operations = journalRepository.findOperationsWithFiltersForUsers(
                actionType, filter.getEntityType(), filter.getEntityId(), filter.getUserId(),
                filter.getSearch(), allowedUserIds, pageable);
        }

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
    public ResponseEntity<?> exportJournal(String format, String startDate, String endDate, String entityType, Long siteId) {
        log.info("Export du journal au format {} pour la période du {} au {} (siteId={})", format, startDate, endDate, siteId);

        LocalDateTime start = parseStartOfDay(startDate);
        LocalDateTime end = parseEndOfDay(endDate);

        List<JournalOperation> operations = journalRepository.findForExport(entityType, start, end);

        User currentUser = getCurrentUserOrNull();
        if (currentUser != null && !hasRole(currentUser, RoleName.ADMIN)) {
            List<Long> allowedUserIds = siteId != null
                ? computeAllowedUserIdsForSite(currentUser, siteId)
                : computeAllowedUserIds(currentUser);
            operations = operations.stream()
                .filter(op -> op.getUser() != null && allowedUserIds.contains(op.getUser().getId()))
                .collect(Collectors.toList());
        }

        if ("excel".equalsIgnoreCase(format)) {
            return exportToExcel(operations);
        } else if ("pdf".equalsIgnoreCase(format)) {
            return exportToPdf(operations);
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Format non supporté : " + format + " (utilisez 'pdf' ou 'excel')");
            error.put("status", "ERROR");
            return ResponseEntity.badRequest().body(error);
        }
    }

    private LocalDateTime parseStartOfDay(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        return LocalDate.parse(dateStr).atStartOfDay();
    }

    private LocalDateTime parseEndOfDay(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        return LocalDate.parse(dateStr).atTime(23, 59, 59);
    }

    private ResponseEntity<byte[]> exportToExcel(List<JournalOperation> operations) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Journal");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[] headers = {"Date", "Utilisateur", "Action", "Entité", "ID Entité", "Détails", "Statut", "IP"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (JournalOperation op : operations) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(op.getCreatedAt() != null ? op.getCreatedAt().format(DATE_FORMATTER) : "");
                row.createCell(1).setCellValue(op.getUser() != null
                    ? op.getUser().getFirstName() + " " + op.getUser().getLastName() : "Système");
                row.createCell(2).setCellValue(op.getActionType() != null ? op.getActionType().name() : "");
                row.createCell(3).setCellValue(op.getEntityType() != null ? op.getEntityType() : "");
                row.createCell(4).setCellValue(op.getEntityId() != null ? op.getEntityId() : 0);
                row.createCell(5).setCellValue(op.getDetails() != null ? op.getDetails() : "");
                row.createCell(6).setCellValue(op.getStatus() != null ? op.getStatus().name() : "");
                row.createCell(7).setCellValue(op.getIpAddress() != null ? op.getIpAddress() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentDisposition(
                ContentDisposition.attachment().filename("journal_tcgm.xlsx").build());
            httpHeaders.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

            return ResponseEntity.ok().headers(httpHeaders).body(out.toByteArray());
        } catch (Exception e) {
            log.error("Erreur lors de l'export Excel: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la génération du fichier Excel", e);
        }
    }

    private ResponseEntity<byte[]> exportToPdf(List<JournalOperation> operations) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("Journal de traçabilité — TCGM").setBold().setFontSize(16));
            document.add(new Paragraph("Généré le " + LocalDateTime.now().format(DATE_FORMATTER))
                .setFontSize(9).setMarginBottom(15));

            float[] columnWidths = {90f, 90f, 60f, 70f, 150f, 60f};
            Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();

            String[] headers = {"Date", "Utilisateur", "Action", "Entité", "Détails", "Statut"};
            for (String h : headers) {
                table.addHeaderCell(new Cell().add(new Paragraph(h).setBold())
                    .setBackgroundColor(new DeviceRgb(230, 126, 34)));
            }

            for (JournalOperation op : operations) {
                table.addCell(new Cell().add(new Paragraph(
                    op.getCreatedAt() != null ? op.getCreatedAt().format(DATE_FORMATTER) : "")).setFontSize(8));
                table.addCell(new Cell().add(new Paragraph(op.getUser() != null
                    ? op.getUser().getFirstName() + " " + op.getUser().getLastName() : "Système")).setFontSize(8));
                table.addCell(new Cell().add(new Paragraph(
                    op.getActionType() != null ? op.getActionType().name() : "")).setFontSize(8));
                table.addCell(new Cell().add(new Paragraph(
                    (op.getEntityType() != null ? op.getEntityType() : "") +
                    (op.getEntityId() != null ? " #" + op.getEntityId() : ""))).setFontSize(8));
                table.addCell(new Cell().add(new Paragraph(
                    op.getDetails() != null ? op.getDetails() : "")).setFontSize(8));
                table.addCell(new Cell().add(new Paragraph(
                    op.getStatus() != null ? op.getStatus().name() : "")).setFontSize(8));
            }

            document.add(table);
            document.close();

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentDisposition(
                ContentDisposition.attachment().filename("journal_tcgm.pdf").build());
            httpHeaders.setContentType(MediaType.APPLICATION_PDF);

            return ResponseEntity.ok().headers(httpHeaders).body(out.toByteArray());
        } catch (Exception e) {
            log.error("Erreur lors de l'export PDF: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la génération du fichier PDF", e);
        }
    }

    @Override
    public Map<String, Object> getJournalStatistiques() {
        log.debug("Récupération des statistiques du journal");

        Map<String, Object> stats = new HashMap<>();
        long totalOperations = journalRepository.count();

        var actionsCount = journalRepository.countOperationsByActionType();
        Map<String, Long> actionsStats = new HashMap<>();
        for (Object[] row : actionsCount) {
            actionsStats.put(row[0].toString(), (Long) row[1]);
        }

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
        Page<JournalOperation> operations = journalRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId, pageable);
        return operations.map(journalMapper::toResponse);
    }

    @Override
    @Transactional
    public JournalResponse validateEntry(Long id, String validatorEmail) {
        JournalOperation operation = journalRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Entrée du journal", id));

        User validator = userRepository.findByEmail(validatorEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + validatorEmail));

        operation.setStatus(StatutValidation.VALIDE);
        operation.setValidatedBy(validator);
        operation.setValidatedAt(LocalDateTime.now());

        JournalOperation saved = journalRepository.save(operation);
        log.debug("Entrée du journal {} validée par {}", id, validatorEmail);

        return journalMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public JournalResponse rejectEntry(Long id, String validatorEmail) {
        JournalOperation operation = journalRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Entrée du journal", id));

        User validator = userRepository.findByEmail(validatorEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + validatorEmail));

        operation.setStatus(StatutValidation.REJETE);
        operation.setValidatedBy(validator);
        operation.setValidatedAt(LocalDateTime.now());

        JournalOperation saved = journalRepository.save(operation);
        log.debug("Entrée du journal {} rejetée par {}", id, validatorEmail);

        return journalMapper.toResponse(saved);
    }
}