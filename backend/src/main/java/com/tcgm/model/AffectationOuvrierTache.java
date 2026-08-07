package com.tcgm.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "affectations_ouvrier_tache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AffectationOuvrierTache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    // Relations
    @ManyToOne
    @JoinColumn(name = "ouvrier_id", nullable = false)
    private Ouvrier ouvrier;

    @ManyToOne
    @JoinColumn(name = "tache_id", nullable = false)
    private Tache tache;

    @PrePersist
    protected void onCreate() {
        assignedAt = LocalDateTime.now();
    }
}