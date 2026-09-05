package com.rental.management.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rental.management.domain.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "proprietaires")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proprietaire {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String telephone;

    @JsonIgnore
    @Column(nullable = false)
    private String motDePasseHash;

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "proprietaire", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Bien> biens = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false)
    private boolean actif = true;

    @Builder.Default
    private Instant creeLe = Instant.now();
}
