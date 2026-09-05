package com.rental.management.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "unites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Unite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    // Ex: "Appartement 2A", ou null si le bien n'est pas subdivisé
    private String libelle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bien_id", nullable = false)
    private Bien bien;

    @Builder.Default
    @Column(nullable = false)
    private boolean archive = false;
}
