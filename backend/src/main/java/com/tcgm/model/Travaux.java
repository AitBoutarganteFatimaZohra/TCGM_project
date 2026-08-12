package com.tcgm.model;

import com.tcgm.model.enums.StatutTravaux;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "travaux")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Travaux {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String intitule;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "date_debut_prevue")
    private LocalDateTime dateDebutPrevue;

    @Column(name = "date_fin_prevue")
    private LocalDateTime dateFinPrevue;

    @Column(name = "date_debut_reelle")
    private LocalDateTime dateDebutReelle;

    @Column(name = "date_fin_reelle")
    private LocalDateTime dateFinReelle;

    @Column(name = "priorite")
    private Integer priorite;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StatutTravaux statut;

    @Column(name = "budget_estime", precision = 15, scale = 2)
    private BigDecimal budgetEstime;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // =========================================================
    // RELATIONS
    // =========================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chantier_id", nullable = false)
    private Site chantier;

    @OneToMany(mappedBy = "travaux", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tache> taches = new ArrayList<>();

    // =========================================================
    // MÉTHODES UTILITAIRES
    // =========================================================

    public void addTache(Tache tache) {
        taches.add(tache);
        tache.setTravaux(this);
    }

    public void removeTache(Tache tache) {
        taches.remove(tache);
        tache.setTravaux(null);
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}