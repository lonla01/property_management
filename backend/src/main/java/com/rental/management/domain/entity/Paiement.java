package com.rental.management.domain.entity;

import com.rental.management.domain.enums.ModePaiement;
import com.rental.management.domain.enums.StatutPaiement;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.YearMonth;

@Entity
@Table(name = "paiements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Paiement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bail_id", nullable = false)
    private Bail bail;

    @Column(nullable = false)
    private Long montant;

    private Instant datePaiement;

    @Enumerated(EnumType.STRING)
    private ModePaiement mode;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private StatutPaiement statut = StatutPaiement.EN_ATTENTE;

    // Référence de transaction CinetPay (transaction_id envoyé, cpm_trans_id reçu au webhook)
    private String referenceTransaction;

    // Période couverte, ex. "2026-03"
    @Column(nullable = false)
    private String periodeCouverte;

    // Token unique et non-devinable pour le lien de paiement public
    @Column(nullable = false, unique = true)
    private String tokenLienPaiement;

    // Token unique pour le lien de reçu public (une fois le paiement confirmé)
    private String tokenRecu;

    private String cheminRecuPdf;

    @Builder.Default
    private Instant creeLe = Instant.now();

    public static String periodeCourante() {
        return YearMonth.now().toString(); // ex. "2026-09"
    }
}
