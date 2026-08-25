package com.tcgm.model;

import com.tcgm.model.enums.StatutValidationRessource;
import com.tcgm.model.enums.TypeActionRessource;
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

    @Column(length = 1000)
    private String description;

    @Column(name = "code_interne", length = 100)
    private String codeInterne;

    @Column(name = "numero_serie", length = 100)
    private String numeroSerie;

    // Seuil en dessous duquel la ressource est considérée en "stock critique"
    @Column(name = "seuil_alerte")
    private Integer seuilAlerte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(name = "date_ajout", nullable = false, updatable = false)
    private LocalDateTime dateAjout;

    // =========================================================
    // CIRCUIT DE VALIDATION (Magasinier -> Chef de Chantier -> Chef de Projet)
    // =========================================================

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_status", length = 30)
    @Builder.Default
    private StatutValidationRessource validationStatus = StatutValidationRessource.VALIDEE;

    /** Type d'action en attente de validation (null si aucune). */
    @Enumerated(EnumType.STRING)
    @Column(name = "pending_action", length = 30)
    private TypeActionRessource pendingAction;

    /** Motif du dernier rejet (niveau 1 ou niveau 2), pour affichage/traçabilité. */
    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    // ----- Instantané "avant action", pour pouvoir tout annuler en cas de
    // ----- rejet définitif (niveau 2). Non utilisé pour une CREATION.
    @Column(name = "avant_nom")
    private String avantNom;

    @Enumerated(EnumType.STRING)
    @Column(name = "avant_type")
    private TypeRessource avantType;

    @Column(name = "avant_quantite")
    private Integer avantQuantite;

    @Column(name = "avant_unite")
    private String avantUnite;

    @Enumerated(EnumType.STRING)
    @Column(name = "avant_statut")
    private StatutRessource avantStatut;

    @Column(name = "avant_description", length = 1000)
    private String avantDescription;

    @Column(name = "avant_code_interne", length = 100)
    private String avantCodeInterne;

    @Column(name = "avant_numero_serie", length = 100)
    private String avantNumeroSerie;

    @Column(name = "avant_seuil_alerte")
    private Integer avantSeuilAlerte;

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

    // =========================================================
    // UTILITAIRES
    // =========================================================

    /** Capture l'état actuel dans les champs "avant", avant de le modifier. */
    public void snapshotAvant() {
        this.avantNom = this.nom;
        this.avantType = this.type;
        this.avantQuantite = this.quantite;
        this.avantUnite = this.unite;
        this.avantStatut = this.statut;
        this.avantDescription = this.description;
        this.avantCodeInterne = this.codeInterne;
        this.avantNumeroSerie = this.numeroSerie;
        this.avantSeuilAlerte = this.seuilAlerte;
    }

    /** Restaure l'état "avant" (rejet définitif d'une MODIFICATION/CHANGEMENT_STATUT). */
    public void restaurerAvant() {
        this.nom = this.avantNom;
        this.type = this.avantType;
        this.quantite = this.avantQuantite;
        this.unite = this.avantUnite;
        this.statut = this.avantStatut;
        this.description = this.avantDescription;
        this.codeInterne = this.avantCodeInterne;
        this.numeroSerie = this.avantNumeroSerie;
        this.seuilAlerte = this.avantSeuilAlerte;
    }

    /** Efface l'instantané "avant" une fois qu'il n'est plus nécessaire. */
    public void clearAvant() {
        this.avantNom = null;
        this.avantType = null;
        this.avantQuantite = null;
        this.avantUnite = null;
        this.avantStatut = null;
        this.avantDescription = null;
        this.avantCodeInterne = null;
        this.avantNumeroSerie = null;
        this.avantSeuilAlerte = null;
    }
}