package com.rental.management.service;

import com.rental.management.domain.entity.Bail;
import com.rental.management.domain.entity.Paiement;
import com.rental.management.domain.enums.StatutBail;
import com.rental.management.domain.enums.StatutPaiement;
import com.rental.management.dto.response.DashboardResponse;
import com.rental.management.repository.BailRepository;
import com.rental.management.repository.PaiementRepository;
import com.rental.management.security.CurrentProprietaire;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final BailRepository bailRepository;
    private final PaiementRepository paiementRepository;
    private final CurrentProprietaire currentProprietaire;

    private static final DateTimeFormatter FORMAT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public DashboardResponse obtenirSynthese() {
        String periodeCourante = Paiement.periodeCourante();
        int jourCourant = LocalDate.now().getDayOfMonth();

        // La liste des baux actifs est bornée par propriétaire; pour un très grand nombre de
        // baux, cet endpoint pourrait lui aussi être paginé côté API — gardé simple pour le MVP
        // car un dashboard doit rester une vue d'ensemble, mais la pagination des LISTES
        // (locataires, paiements, historique) reste appliquée partout ailleurs comme demandé.
        List<Bail> baux = bailRepository.findByLocataireProprietaireId(currentProprietaire.id(), PageRequest.of(0, 500))
                .getContent()
                .stream()
                .filter(b -> b.getStatut() == StatutBail.ACTIF)
                .toList();

        List<DashboardResponse.SituationLocataire> situations = new ArrayList<>();
        long enRetard = 0;
        long montantEnAttente = 0;

        for (Bail bail : baux) {
            var paiementReussi = paiementRepository
                    .findByBailIdAndPeriodeCouverteAndStatut(bail.getId(), periodeCourante, StatutPaiement.REUSSI);

            String statut;
            Long montantDu = null;
            String dateDernierPaiement = paiementReussi.map(p -> p.getDatePaiement()
                            .atZone(ZoneId.of("Africa/Douala")).toLocalDate().format(FORMAT_DATE))
                    .orElse(null);

            if (paiementReussi.isPresent()) {
                statut = "PAYE";
            } else if (jourCourant > bail.getJourEcheance()) {
                statut = "EN_RETARD";
                montantDu = bail.getMontantLoyer();
                enRetard++;
                montantEnAttente += bail.getMontantLoyer();
            } else {
                statut = "EN_ATTENTE";
                montantDu = bail.getMontantLoyer();
                montantEnAttente += bail.getMontantLoyer();
            }

            situations.add(new DashboardResponse.SituationLocataire(
                    bail.getId(),
                    bail.getLocataire().getNom(),
                    bail.getUnite().getBien().getAdresse(),
                    statut,
                    montantDu,
                    dateDernierPaiement
            ));
        }

        return new DashboardResponse(enRetard, montantEnAttente, situations);
    }
}
