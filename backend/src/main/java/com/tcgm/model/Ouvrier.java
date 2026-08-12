package com.tcgm.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ouvriers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ouvrier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(unique = true, nullable = false, length = 50)
    private String cin;

    @Column(length = 100)
    private String specialite;

    @Column(length = 20)
    private String phone;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // =========================================================
    // RELATIONS MODIFIÉES
    // =========================================================

    @OneToMany(mappedBy = "ouvrier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Affectation> affectations = new ArrayList<>();

    @OneToMany(mappedBy = "ouvrier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AffectationOuvrierTache> affectationsTaches = new ArrayList<>();

    @OneToMany(mappedBy = "ouvrier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LignePointage> pointages = new ArrayList<>();

    // =========================================================
    // RELATION À SUPPRIMER PROGRESSIVEMENT
    // =========================================================

    // @OneToMany(mappedBy = "ouvrier") private List<AffectationOuvrierSite> affectationsSites;

    // =========================================================
    // MÉTHODES UTILITAIRES
    // =========================================================

    public void addAffectation(Affectation affectation) {
        this.affectations.add(affectation);
        affectation.setOuvrier(this);
    }

    public void removeAffectation(Affectation affectation) {
        this.affectations.remove(affectation);
        affectation.setOuvrier(null);
    }

    public Affectation getAffectationEnCours() {
        return affectations.stream()
            .filter(Affectation::isEnCours)
            .findFirst()
            .orElse(null);
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