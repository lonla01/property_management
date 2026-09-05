package com.rental.management.domain.entity;

import com.rental.management.domain.enums.CanalEnvoi;
import com.rental.management.domain.enums.StatutEnvoi;
import com.rental.management.domain.enums.TypeRappel;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "rappels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rappel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeRappel type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bail_id", nullable = false)
    private Bail bail;

    // Période concernée, ex. "2026-09"
    @Column(nullable = false)
    private String periodeConcernee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CanalEnvoi canal;

    private Instant dateEnvoi;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private StatutEnvoi statutEnvoi = StatutEnvoi.EN_ATTENTE;

    // Nombre de tentatives, pour le mécanisme de reprise
    @Builder.Default
    private int tentatives = 0;

    private String messageErreur;
}
