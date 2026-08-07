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

    // Relations
    @OneToMany(mappedBy = "ouvrier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AffectationOuvrierSite> affectationsSites = new ArrayList<>();

    @OneToMany(mappedBy = "ouvrier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AffectationOuvrierTache> affectationsTaches = new ArrayList<>();

    @OneToMany(mappedBy = "ouvrier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LignePointage> pointages = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}