package com.tcgm.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lignes_pointage")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LignePointage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "half_day")
    private Boolean halfDay = false;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Relations
    @ManyToOne
    @JoinColumn(name = "dossier_id", nullable = false)
    private DossierPointage dossier;

    @ManyToOne
    @JoinColumn(name = "ouvrier_id", nullable = false)
    private Ouvrier ouvrier;

    @ManyToOne
    @JoinColumn(name = "tache_id", nullable = false)
    private Tache tache;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}