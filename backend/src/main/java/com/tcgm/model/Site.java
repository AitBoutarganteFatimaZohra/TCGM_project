package com.tcgm.model;

import com.tcgm.model.enums.StatutSite;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 100, unique = true)
    private String reference;

    @Column(columnDefinition = "TEXT")
    private String address;


    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StatutSite status = StatutSite.PLANIFIE;

    // =========================================================
    // ⚠️ NOUVEAU : circuit de validation des modifications majeures
    // (§5 cahier des charges) — Chef de Projet propose, Administrateur
    // valide ou rejette.
    // =========================================================

    @Enumerated(EnumType.STRING)
    @Column(name = "pending_status", length = 20)
    private StatutSite pendingStatus;

    @Column(name = "pending_start_date")
    private LocalDateTime pendingStartDate;


    @Column(name = "pending_end_date")
    private LocalDateTime pendingEndDate;

    @Column(name = "motif_rejet", length = 500)
    private String motifRejet;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // =========================================================
    // RELATIONS EXISTANTES
    // =========================================================

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "chef_projet_id", nullable = false)
    private User chefProjet;

    @ManyToOne
    @JoinColumn(name = "magasinier_id")
    private User magasinier;

    @ManyToOne
    @JoinColumn(name = "agent_saisie_id")
    private User agentSaisie;


    @ManyToOne
    @JoinColumn(name = "chef_chantier_id")
    private User chefChantier;

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DossierPointage> dossiersPointage = new ArrayList<>();

    @OneToMany(mappedBy = "chantier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Travaux> travaux = new ArrayList<>();

    @OneToMany(mappedBy = "chantier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Affectation> affectations = new ArrayList<>();

    public void addTravaux(Travaux travaux) {
        this.travaux.add(travaux);
        travaux.setChantier(this);
    }

    public void removeTravaux(Travaux travaux) {
        this.travaux.remove(travaux);
        travaux.setChantier(null);
    }

    public void addAffectation(Affectation affectation) {
        this.affectations.add(affectation);
        affectation.setChantier(this);
    }

    public void removeAffectation(Affectation affectation) {
        this.affectations.remove(affectation);
        affectation.setChantier(null);
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