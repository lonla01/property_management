package com.rental.management.service;

import com.rental.management.domain.entity.Bail;
import com.rental.management.domain.entity.Rappel;
import com.rental.management.domain.enums.CanalEnvoi;
import com.rental.management.domain.enums.StatutBail;
import com.rental.management.domain.enums.StatutEnvoi;
import com.rental.management.domain.enums.TypeRappel;
import com.rental.management.repository.BailRepository;
import com.rental.management.repository.PaiementRepository;
import com.rental.management.repository.RappelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Cœur du système de rappels (avant échéance) et relances (retard).
 *
 * Contraintes respectées scrupuleusement, comme demandé dans les spécifications :
 * - Envoi PAR LOTS obligatoire : jamais toute la liste des locataires en une seule
 *   boucle rapide. Les baux concernés sont traités par petits groupes (taille
 *   configurable, app.reminders.batch-size), avec une pause entre chaque lot
 *   (app.reminders.batch-pause-ms), pour rester raisonnable vis-à-vis des
 *   fournisseurs SMS/WhatsApp et pouvoir tracer précisément chaque envoi.
 * - Journalisation explicite de CHAQUE tentative d'envoi (réussie ou échouée)
 *   dans l'entité Rappel, afin de pouvoir vérifier après coup que chaque
 *   locataire concerné a bien reçu son message.
 * - Mécanisme de reprise (retry) : reessayerEnvoisEchoues() reprend les Rappel
 *   au statut ECHOUE lors d'une exécution précédente.
 *
 * Déclenché via un endpoint dédié appelé par un planificateur externe (voir
 * RappelController + scheduler Railway avec cron), pas par un @Scheduled interne
 * uniquement — ce qui permet de garder le contrôle depuis l'extérieur si besoin.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RappelService {

    private final BailRepository bailRepository;
    private final PaiementRepository paiementRepository;
    private final RappelRepository rappelRepository;
    private final NotificationProvider notificationProvider;
    private final com.rental.management.security.CurrentProprietaire currentProprietaire;

    // Pagination obligatoire sur l'historique des rappels/relances.
    public org.springframework.data.domain.Page<Rappel> listerHistorique(org.springframework.data.domain.Pageable pageable) {
        return rappelRepository.findByBailLocataireProprietaireId(currentProprietaire.id(), pageable);
    }

    @Value("${app.reminders.batch-size}")
    private int tailleLot;

    @Value("${app.reminders.batch-pause-ms}")
    private long pauseEntreLotsMs;

    @Value("${app.reminders.days-before-due}")
    private String joursAvantEcheanceBrut;

    @Value("${app.reminders.overdue-cadence-days}")
    private String cadenceRetardBrute;

    public record ResultatExecution(int traites, int envoyesAvecSucces, int echoues) {}

    /** À appeler une fois par jour par le planificateur externe (cron Railway). */
    public ResultatExecution executerRappelsEtRelancesDuJour() {
        List<Bail> bauxActifs = bailRepository.findByStatut(StatutBail.ACTIF);
        LocalDate aujourdHui = LocalDate.now();

        List<TacheEnvoi> taches = new ArrayList<>();

        Set<Integer> joursAvant = parseListeJours(joursAvantEcheanceBrut);
        Set<Integer> cadenceRetard = parseListeJours(cadenceRetardBrute);

        for (Bail bail : bauxActifs) {
            String periode = com.rental.management.domain.entity.Paiement.periodeCourante();

            boolean dejaPaye = paiementRepository
                    .findByBailIdAndPeriodeCouverteAndStatut(bail.getId(), periode,
                            com.rental.management.domain.enums.StatutPaiement.REUSSI)
                    .isPresent();
            if (dejaPaye) continue;

            int joursRestants = bail.getJourEcheance() - aujourdHui.getDayOfMonth();
            int joursDeRetard = aujourdHui.getDayOfMonth() - bail.getJourEcheance();

            if (joursRestants >= 0 && joursAvant.contains(joursRestants)) {
                taches.add(new TacheEnvoi(bail, TypeRappel.RAPPEL_AVANT_ECHEANCE, periode));
            } else if (joursDeRetard > 0 && cadenceRetard.contains(joursDeRetard)) {
                taches.add(new TacheEnvoi(bail, TypeRappel.RELANCE_RETARD, periode));
            }
        }

        return envoyerParLots(taches);
    }

    /** Reprend les envois qui avaient échoué lors d'une exécution précédente. */
    public ResultatExecution reessayerEnvoisEchoues() {
        List<Rappel> echoues = rappelRepository.findByStatutEnvoi(StatutEnvoi.ECHOUE);
        List<TacheEnvoi> taches = echoues.stream()
                .map(r -> new TacheEnvoi(r.getBail(), r.getType(), r.getPeriodeConcernee(), r))
                .collect(Collectors.toList());
        return envoyerParLots(taches);
    }

    private ResultatExecution envoyerParLots(List<TacheEnvoi> taches) {
        int traites = 0, succes = 0, echecs = 0;

        List<List<TacheEnvoi>> lots = partitionner(taches, tailleLot);
        log.info("Envoi des rappels/relances: {} tâche(s) réparties en {} lot(s) de {} maximum",
                taches.size(), lots.size(), tailleLot);

        for (int i = 0; i < lots.size(); i++) {
            List<TacheEnvoi> lot = lots.get(i);
            log.info("Traitement du lot {}/{} ({} tâche(s))", i + 1, lots.size(), lot.size());

            for (TacheEnvoi tache : lot) {
                for (CanalEnvoi canal : new CanalEnvoi[]{CanalEnvoi.SMS, CanalEnvoi.WHATSAPP}) {
                    traites++;
                    boolean ok = envoyerUneTache(tache, canal);
                    if (ok) succes++; else echecs++;
                }
            }

            if (i < lots.size() - 1) {
                pauser();
            }
        }

        log.info("Fin d'exécution: {} traités, {} réussis, {} échoués", traites, succes, echecs);
        return new ResultatExecution(traites, succes, echecs);
    }

    private boolean envoyerUneTache(TacheEnvoi tache, CanalEnvoi canal) {
        Bail bail = tache.bail;

        // Évite un doublon si un rappel identique (même bail, période, type, canal) a déjà
        // été envoyé avec succès aujourd'hui — sauf si on est explicitement en reprise (rappelExistant fourni).
        if (tache.rappelExistant == null && rappelRepository.existsByBailIdAndPeriodeConcerneeAndTypeAndCanal(
                bail.getId(), tache.periode, tache.type, canal)) {
            return true; // déjà traité, on ne recompte pas comme un échec
        }

        Rappel rappel = tache.rappelExistant != null ? tache.rappelExistant : Rappel.builder()
                .type(tache.type)
                .bail(bail)
                .periodeConcernee(tache.periode)
                .canal(canal)
                .build();

        String telephone = bail.getLocataire().getTelephone();
        String message = construireMessage(tache);

        NotificationProvider.ResultatEnvoi resultat = canal == CanalEnvoi.SMS
                ? notificationProvider.envoyerSms(telephone, message)
                : notificationProvider.envoyerWhatsApp(telephone, message);

        rappel.setTentatives(rappel.getTentatives() + 1);
        rappel.setDateEnvoi(java.time.Instant.now());

        if (resultat.succes) {
            rappel.setStatutEnvoi(StatutEnvoi.ENVOYE);
            rappel.setMessageErreur(null);
        } else {
            rappel.setStatutEnvoi(StatutEnvoi.ECHOUE);
            rappel.setMessageErreur(resultat.erreur);
        }

        rappelRepository.save(rappel);
        return resultat.succes;
    }

    private String construireMessage(TacheEnvoi tache) {
        Bail bail = tache.bail;
        String nomLocataire = bail.getLocataire().getNom();
        long montant = bail.getMontantLoyer();

        if (tache.type == TypeRappel.RAPPEL_AVANT_ECHEANCE) {
            return "Bonjour " + nomLocataire + ", votre loyer de " + montant
                    + " XAF pour " + tache.periode + " arrive bientôt à échéance. Merci de régulariser.";
        }
        return "Bonjour " + nomLocataire + ", votre loyer de " + montant
                + " XAF pour " + tache.periode + " est en retard. Merci de régulariser rapidement.";
    }

    private void pauser() {
        try {
            Thread.sleep(pauseEntreLotsMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private List<List<TacheEnvoi>> partitionner(List<TacheEnvoi> liste, int taille) {
        List<List<TacheEnvoi>> lots = new ArrayList<>();
        for (int i = 0; i < liste.size(); i += taille) {
            lots.add(liste.subList(i, Math.min(i + taille, liste.size())));
        }
        return lots;
    }

    private Set<Integer> parseListeJours(String brut) {
        return Arrays.stream(brut.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
    }

    private static class TacheEnvoi {
        final Bail bail;
        final TypeRappel type;
        final String periode;
        final Rappel rappelExistant;

        TacheEnvoi(Bail bail, TypeRappel type, String periode) {
            this(bail, type, periode, null);
        }

        TacheEnvoi(Bail bail, TypeRappel type, String periode, Rappel rappelExistant) {
            this.bail = bail;
            this.type = type;
            this.periode = periode;
            this.rappelExistant = rappelExistant;
        }
    }
}
