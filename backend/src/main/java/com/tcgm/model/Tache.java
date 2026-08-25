package com.tcgm.model;

import com.tcgm.model.enums.StatutTache;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "taches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "planned_date")
    private LocalDateTime plannedDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 25)
    private StatutTache status = StatutTache.PLANIFIEE;

    private Integer priority = 1;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // =========================================================
    // CIRCUIT DE VALIDATION (Chef de Chantier -> Chef de Projet)
    // =========================================================

    /**
     * Statut dans lequel se trouvait la tâche avant la soumission d'une
     * demande de validation. Utilisé pour restaurer l'état d'origine en
     * cas de rejet. Null tant qu'aucune demande n'est en attente.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 25)
    private StatutTache previousStatus;

    /**
     * Statut cible proposé par le Chef de Chantier, en attente de
     * validation par le Chef de Projet. Null tant qu'aucune demande n'est
     * en attente.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "proposed_status", length = 25)
    private StatutTache proposedStatus;

    /**
     * Nouvelle date prévue proposée (optionnelle) en attente de validation.
     */
    @Column(name = "proposed_planned_date")
    private LocalDateTime proposedPlannedDate;

    /**
     * Motif du dernier rejet (facultatif), conservé pour affichage/traçabilité.
     */
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    // =========================================================
    // RELATIONS
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travaux_id", nullable = false)
    private Travaux travaux;

    @OneToMany(mappedBy = "tache", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AffectationOuvrierTache> affectationsOuvriers = new ArrayList<>();

    @OneToMany(mappedBy = "tache")
    private List<LignePointage> lignesPointage = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}