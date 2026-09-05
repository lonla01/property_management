package com.rental.management.service;

import com.rental.management.domain.entity.Bail;
import com.rental.management.domain.entity.Paiement;
import com.rental.management.domain.enums.ModePaiement;
import com.rental.management.domain.enums.StatutPaiement;
import com.rental.management.dto.request.InitierPaiementRequest;
import com.rental.management.dto.response.LienPaiementResponse;
import com.rental.management.exception.RessourceIntrouvableException;
import com.rental.management.repository.PaiementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaiementService {

    private final PaiementRepository paiementRepository;
    private final BailService bailService;
    private final CinetPayClient cinetPayClient;
    private final PdfReceiptService pdfReceiptService;
    private final com.rental.management.security.CurrentProprietaire currentProprietaire;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    private final SecureRandom random = new SecureRandom();

    // Pagination obligatoire.
    public Page<Paiement> lister(Pageable pageable) {
        return paiementRepository.findByBailLocataireProprietaireId(currentProprietaire.id(), pageable);
    }

    public LienPaiementResponse initierPaiement(InitierPaiementRequest request) {
        Bail bail = bailService.obtenir(request.getBailId());
        String periode = request.getPeriode() != null ? request.getPeriode() : Paiement.periodeCourante();

        // Évite de générer un doublon si un paiement réussi existe déjà pour cette période.
        paiementRepository.findByBailIdAndPeriodeCouverteAndStatut(bail.getId(), periode, StatutPaiement.REUSSI)
                .ifPresent(p -> { throw new IllegalStateException("Le loyer de " + periode + " est déjà payé"); });

        String transactionId = "LOY-" + bail.getId().substring(0, 8) + "-" + periode + "-" + System.currentTimeMillis();
        String token = genererTokenSecurise();

        Paiement paiement = Paiement.builder()
                .bail(bail)
                .montant(bail.getMontantLoyer())
                .periodeCouverte(periode)
                .statut(StatutPaiement.EN_ATTENTE)
                .tokenLienPaiement(token)
                .referenceTransaction(transactionId)
                .build();
        paiementRepository.save(paiement);

        CinetPayClient.InitiationResultat resultat = cinetPayClient.initierPaiement(
                transactionId, bail.getMontantLoyer(),
                "Loyer " + periode + " - " + bail.getLocataire().getNom());

        if (!resultat.isSucces()) {
            paiement.setStatut(StatutPaiement.ECHOUE);
            paiementRepository.save(paiement);
            return new LienPaiementResponse(paiement.getId(), null, "ECHOUE: " + resultat.getMessage());
        }

        // En sandbox, aucun vrai CinetPay n'appellera jamais notre webhook — on confirme donc
        // immédiatement le paiement pour permettre de tester tout le flux (reçu PDF inclus)
        // sans dépendance externe. En production (sandbox=false), on attend le vrai webhook.
        if (cinetPayClient.isSandbox()) {
            confirmerPaiement(paiement, "ORANGE_MONEY");
        }

        // Le lien renvoyé au locataire passe par notre propre page (token), qui redirige ensuite
        // vers la page de paiement CinetPay — ça permet de suivre le paiement même en cas de coupure.
        String urlPublique = frontendBaseUrl + "/paiement/" + token;
        return new LienPaiementResponse(paiement.getId(), urlPublique, paiement.getStatut().name());
    }

    public Paiement obtenirParToken(String token) {
        return paiementRepository.findByTokenLienPaiement(token)
                .orElseThrow(() -> new RessourceIntrouvableException("Lien de paiement invalide ou expiré"));
    }

    /**
     * Traite le webhook CinetPay. Revérifie systématiquement le statut auprès de l'API
     * CinetPay (verifierTransaction) avant de faire confiance au contenu du webhook,
     * pour éviter qu'un faux webhook ne valide un paiement.
     */
    public void traiterWebhook(String transactionId, String modePaiementBrut) {
        Paiement paiement = paiementRepository.findByReferenceTransaction(transactionId)
                .orElseThrow(() -> new RessourceIntrouvableException("Transaction inconnue: " + transactionId));

        boolean confirme = cinetPayClient.verifierTransaction(transactionId);

        if (!confirme) {
            paiement.setStatut(StatutPaiement.ECHOUE);
            paiementRepository.save(paiement);
            log.warn("Webhook reçu pour {} mais vérification CinetPay négative", transactionId);
            return;
        }

        confirmerPaiement(paiement, modePaiementBrut);
    }

    private void confirmerPaiement(Paiement paiement, String modePaiementBrut) {
        paiement.setStatut(StatutPaiement.REUSSI);
        paiement.setDatePaiement(Instant.now());
        paiement.setMode(mapperMode(modePaiementBrut));
        paiement.setTokenRecu(genererTokenSecurise());
        paiementRepository.save(paiement);

        String cheminPdf = pdfReceiptService.genererRecu(paiement);
        paiement.setCheminRecuPdf(cheminPdf);
        paiementRepository.save(paiement);

        log.info("Paiement {} confirmé pour la période {}", paiement.getId(), paiement.getPeriodeCouverte());
    }

    private ModePaiement mapperMode(String brut) {
        if (brut == null) return null;
        String v = brut.toUpperCase();
        if (v.contains("MTN")) return ModePaiement.MTN_MOMO;
        if (v.contains("ORANGE")) return ModePaiement.ORANGE_MONEY;
        return null;
    }

    private String genererTokenSecurise() {
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}