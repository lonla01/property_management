package com.rental.management.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "locataires")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Locataire {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String nom;

    // Format international ex. +237...
    @Column(nullable = false)
    private String telephone;

    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unite_id")
    private Unite unite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proprietaire_id", nullable = false)
    private Proprietaire proprietaire;

    @Builder.Default
    @Column(nullable = false)
    private boolean archive = false;
}
