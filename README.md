# Gestion Locative — MVP

Application de gestion locative pour propriétaires bailleurs, avec encaissement
des loyers via Mobile Money (Orange Money / MTN MoMo, Cameroun) au travers de
CinetPay, reçus PDF automatiques, dashboard de synthèse, et rappels/relances
par SMS et WhatsApp.

## Structure du projet

```
backend/    Spring Boot 3 / Java 17 / PostgreSQL / API REST
frontend/   React 18 + Vite, PWA (installable, consultation basique hors-ligne)
```

## État du MVP

Implémenté :
- Authentification propriétaire (JWT, inscription/connexion)
- CRUD Biens / Unités / Locataires / Baux, avec isolation stricte des données
  par compte propriétaire
- Pagination sur toutes les listes (locataires, paiements, baux, historique
  des rappels) — jamais de chargement de collection entière en un appel
- Génération de lien de paiement CinetPay + webhook de confirmation, avec
  **mode sandbox actif par défaut** (aucun compte CinetPay requis pour
  développer/tester — voir `CINETPAY_SANDBOX=true` dans `.env`)
- Génération automatique du reçu PDF dès qu'un paiement est confirmé
- Dashboard de synthèse par locataire (payé / en attente / en retard)
- Rappels avant échéance + relances de retard, envoyés par SMS et WhatsApp,
  avec **envoi par lots obligatoire** (jamais toute la liste d'un coup),
  pause entre chaque lot, journalisation de chaque tentative, et mécanisme
  de reprise des envois échoués
- Fournisseur de notification **simulé par défaut** (aucun compte Twilio ou
  autre requis pour développer/tester) — bascule vers Twilio en changeant
  une seule variable d'environnement (`NOTIFICATION_PROVIDER=twilio`)

Pas encore implémenté (pistes pour la suite) :
- Rôle "Gestionnaire délégué" (prévu dans les specs comme V2)
- Tests automatisés (unitaires/intégration)
- Interface d'administration des paramètres (CinetPay, Twilio) — actuellement
  tout passe par variables d'environnement

## Démarrage en local

### Backend

```bash
cd backend
cp .env.example .env   # puis éditer .env si besoin
docker compose up -d   # démarre Postgres en local
# Exporter les variables de .env dans le shell, ou utiliser un plugin
# (ex. spring-dotenv) — non ajouté par défaut pour rester au plus proche
# d'un Spring Boot standard.
mvn spring-boot:run
```

L'API écoute sur http://localhost:8080.

### Frontend

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
```

L'app est servie sur http://localhost:5173.

## Mettre le projet sur GitHub

```bash
cd rental-app
git init
git add .
git commit -m "MVP initial: gestion locative + paiements Mobile Money"
git branch -M main
git remote add origin https://github.com/<votre-compte>/<votre-repo>.git
git push -u origin main
```

## Déploiement sur Railway (une fois le repo sur GitHub)

1. Créer un projet Railway, ajouter un service PostgreSQL (plugin Railway).
2. Créer un service pour le backend, source = votre repo GitHub, dossier
   racine `backend/` (Railway détecte le Dockerfile automatiquement).
3. Configurer les variables d'environnement du service backend à partir de
   `.env.example` (au minimum : `DATABASE_URL`, `JWT_SECRET`, `SCHEDULER_KEY`,
   `CORS_ALLOWED_ORIGINS` = URL du frontend déployé).
4. Créer un second service pour le frontend (dossier racine `frontend/`,
   build `npm run build`, serveur statique sur `dist/`), avec
   `VITE_API_BASE_URL` pointant vers l'URL publique du backend.
5. Pour le planificateur (rappels/relances quotidiens) : créer un troisième
   service "worker" (ou un Cron Job Railway) qui appelle chaque jour :
   `POST /api/rappels/executer-du-jour` avec le header
   `X-Scheduler-Key: <valeur de SCHEDULER_KEY>`.

## Prochaines étapes CinetPay / Twilio

Aucun compte n'est requis pour développer et tester (sandbox + simulation
actifs par défaut). Une fois les comptes créés :
- CinetPay : renseigner `CINETPAY_API_KEY`, `CINETPAY_SITE_ID`, et passer
  `CINETPAY_SANDBOX=false`.
- SMS/WhatsApp : renseigner les variables `TWILIO_*` et passer
  `NOTIFICATION_PROVIDER=twilio`.
