package com.tcgm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ressources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ressource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeRessource type;

    private Integer quantite;

    private String unite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutRessource statut = StatutRessource.DISPONIBLE;

    // ⚠️ NOUVEAU : circuit de validation (§3 cahier des charges)
    // Statut proposé par le Magasinier, en attente de validation
    // par le Chef de Chantier (ou Chef de Projet en cas de recours).
    @Enumerated(EnumType.STRING)
    @Column(name = "pending_statut")
    private StatutRessource pendingStatut;

    @Column(name = "motif_rejet", length = 500)
    private String motifRejet;

    @Column(length = 1000)
    private String description;

    @Column(name = "code_interne", length = 100)
    private String codeInterne;

    @Column(name = "numero_serie", length = 100)
    private String numeroSerie;

    @Column(name = "seuil_alerte")
    private Integer seuilAlerte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(name = "date_ajout", nullable = false, updatable = false)
    private LocalDateTime dateAjout;

    @PrePersist
    protected void onCreate() {
        this.dateAjout = LocalDateTime.now();
    }

    public enum TypeRessource {
        MATERIEL,
        EQUIPEMENT,
        OUTIL,
        CONSOMMABLE
    }

    public enum StatutRessource {
        DISPONIBLE,
        EN_UTILISATION,
        HORS_SERVICE,
        EN_MAINTENANCE
    }
}