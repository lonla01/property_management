package com.rental.management.domain.entity;

import com.rental.management.domain.enums.StatutBail;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "baux")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bail {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locataire_id", nullable = false)
    private Locataire locataire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unite_id", nullable = false)
    private Unite unite;

    // FCFA, sans décimales
    @Column(nullable = false)
    private Long montantLoyer;

    @Builder.Default
    @Column(nullable = false)
    private String devise = "XAF";

    // Ex: "MENSUELLE" — MVP: uniquement mensuelle
    @Builder.Default
    @Column(nullable = false)
    private String periodicite = "MENSUELLE";

    @Column(nullable = false)
    private LocalDate dateDebut;

    // Jour du mois (1-28 recommandé pour éviter les soucis de fin de mois)
    @Column(nullable = false)
    private Integer jourEcheance;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private StatutBail statut = StatutBail.ACTIF;
}
