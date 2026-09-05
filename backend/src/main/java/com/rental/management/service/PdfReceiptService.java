package com.rental.management.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.rental.management.domain.entity.Paiement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Génère le reçu PDF dès qu'un paiement est confirmé (voir PaiementService).
 * Le PDF est écrit sur disque (dossier configurable) et son chemin relatif est
 * stocké sur l'entité Paiement pour être servi via un lien à token unique.
 */
@Service
@Slf4j
public class PdfReceiptService {

    @Value("${app.receipts.dossier:/tmp/receipts}")
    private String dossierReceipts;

    public String genererRecu(Paiement paiement) {
        try {
            Files.createDirectories(Path.of(dossierReceipts));

            String html = construireHtml(paiement);

            String nomFichier = "recu-" + paiement.getId() + ".pdf";
            File fichier = new File(dossierReceipts, nomFichier);

            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.withHtmlContent(html, null);
                builder.toStream(os);
                builder.run();

                try (FileOutputStream fos = new FileOutputStream(fichier)) {
                    fos.write(os.toByteArray());
                }
            }

            return fichier.getAbsolutePath();
        } catch (Exception e) {
            log.error("Échec de génération du reçu PDF pour le paiement {}", paiement.getId(), e);
            throw new RuntimeException("Impossible de générer le reçu PDF", e);
        }
    }

    private String construireHtml(Paiement paiement) {
        var bail = paiement.getBail();
        var locataire = bail.getLocataire();
        var unite = bail.getUnite();
        var bien = unite.getBien();
        var proprietaire = bien.getProprietaire();

        String dateFormatee = paiement.getDatePaiement()
                .atZone(ZoneId.of("Africa/Douala"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        return """
            <html>
            <head><style>
                body { font-family: Arial, sans-serif; margin: 40px; color: #1a1a1a; }
                h1 { font-size: 20px; border-bottom: 2px solid #333; padding-bottom: 8px; }
                table { width: 100%%; border-collapse: collapse; margin-top: 20px; }
                td { padding: 6px 0; }
                td.label { color: #555; width: 40%%; }
                .montant { font-size: 22px; font-weight: bold; margin-top: 20px; }
            </style></head>
            <body>
                <h1>Reçu de paiement de loyer</h1>
                <table>
                    <tr><td class="label">Propriétaire</td><td>%s</td></tr>
                    <tr><td class="label">Locataire</td><td>%s</td></tr>
                    <tr><td class="label">Bien</td><td>%s</td></tr>
                    <tr><td class="label">Période couverte</td><td>%s</td></tr>
                    <tr><td class="label">Mode de paiement</td><td>%s</td></tr>
                    <tr><td class="label">Référence de transaction</td><td>%s</td></tr>
                    <tr><td class="label">Date</td><td>%s</td></tr>
                </table>
                <div class="montant">Montant payé : %d XAF</div>
            </body>
            </html>
            """.formatted(
                proprietaire.getNom(),
                locataire.getNom(),
                bien.getAdresse(),
                paiement.getPeriodeCouverte(),
                paiement.getMode(),
                paiement.getReferenceTransaction(),
                dateFormatee,
                paiement.getMontant()
        );
    }
}
