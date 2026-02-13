# AgriAssist — PIDEV 3A (2025–2026)

AgriAssist est une solution **bi-client** avec **une seule base MySQL partagée** :
- **Sprint 1 : JavaFX Desktop**
- **Sprint 2 : Web Symfony 6.4**

## Contraintes imposées (à respecter)
- ✅ Chaque module = **min 2 entités + 1 relation**
- ✅ **Une seule BD partagée** entre JavaFX & Symfony
- ✅ Stockage médias **en fichiers** + **URL en BD** (pas de BLOB)
- ✅ **GitHub Project / Trello** obligatoire (suivi + communication)
- ❌ Pas de **FOSUserBundle** (web)
- ❌ Pas de **adminBundle** (backoffice web)
- ✅ Chaque sprint contient **FrontOffice + BackOffice** pour chaque étudiant

---

## 1) Équipe & Répartition
On est **6 étudiants** :
- Étudiant 1 = **Module 1**
- Étudiant 2 = **Module 2**
- Étudiant 3 = **Module 3**
- Étudiant 4 = **Module 4**
- Étudiant 5 = **Module 5**
- Étudiant 6 = **Module 6**

Chaque module doit livrer à chaque sprint :
- **FO (FrontOffice)** + **BO (BackOffice)**

---

## 2) Modules (résumé)
- **Module 0 (commun)** : Users / Auth / Roles (ADMIN, AGRICULTEUR, FOURNISSEUR)
- **M1** : Agriculteur ↔ Terrain
- **M2** : SaisonAgricole ↔ Culture
- **M3** : Fournisseur ↔ Ressource (catalogue intrants)
- **M4** : Livraison ↔ PaiementSaisonnier (prototype)
- **M5** : ImagePlante ↔ Diagnostic (mock IA)
- **M6** : DossierAgricole ↔ RapportSaison (PDF)

---

## 3) Stack Technique

### Sprint 1 — JavaFX Desktop
- Java 17+ (recommandé)
- JavaFX (FXML + Controllers)
- JDBC (MySQL)

### Sprint 2 — Symfony Web
- PHP 8.2+
- Symfony 6.4
- Doctrine ORM
- Twig

### Base de données
- MySQL 8+
- Schéma partagé unique : `db/schema.sql`

### Stockage fichiers
- Images : `web-symfony/public/uploads/images/`
- Rapports PDF : `web-symfony/public/uploads/reports/`
- En BD : uniquement des champs `*_url`

---

## 4) Structure du projet (monorepo)
Respect strict des dossiers pour éviter les conflits d’intégration.

```text
AgriAssist-PIDEV/
  db/
    schema.sql
    seed.sql
  docs/
    diagrammes/
    conventions.md
  javafx-desktop/           # Sprint 1
    src/main/java/agriassist/
      models/
      dao/
      services/
      controllers/front/
      controllers/back/
      utils/
    src/main/resources/
      fxml/front/
      fxml/back/
      assets/
  web-symfony/              # Sprint 2
    public/uploads/images/
    public/uploads/reports/
    src/Controller/Front/
    src/Controller/Back/
    src/Entity/
    src/Repository/
    src/Service/
    src/Security/
    templates/front/
    templates/back/
```

---

## 5) Installation & Lancement

### 5.1 Base MySQL (commune)
1) Créer la base :
```sql
CREATE DATABASE agriassist CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

2) Importer le schéma :
```bash
mysql -u root -p agriassist < db/schema.sql
```

3) (Optionnel) importer seed :
```bash
mysql -u root -p agriassist < db/seed.sql
```

Règle d’or : **toute modification DB passe par PR** (sinon conflit garanti).

---

### 5.2 Sprint 1 — JavaFX
- Configurer la connexion MySQL dans un fichier unique (ex: `DbConfig.java`)
- Lancer depuis IntelliJ / Eclipse

Conventions :
- FO = `controllers/front/` + `fxml/front/`
- BO = `controllers/back/` + `fxml/back/`

---

### 5.3 Sprint 2 — Symfony
1) Installer dépendances :
```bash
cd web-symfony
composer install
```

2) Créer `.env.local` :
```env
DATABASE_URL="mysql://root:password@127.0.0.1:3306/agriassist?serverVersion=8.0"
APP_ENV=dev
APP_SECRET=change_me
```

3) Lancer le serveur :
```bash
symfony server:start
```
ou
```bash
php -S localhost:8000 -t public
```

4) Vérifier les dossiers upload :
```bash
mkdir -p public/uploads/images public/uploads/reports
```

---

## 6) Auth / Rôles / Sécurité

### Rôles
- `ROLE_ADMIN`
- `ROLE_AGRICULTEUR`
- `ROLE_FOURNISSEUR`

### Règles
- L’admin accède à `/back/*`
- Les pages BO doivent être **bloquées** pour les autres rôles
- Un user ne voit que **ses données** (FO)

Interdit :
- FOSUserBundle
- AdminBundle (générateurs BO)

---

## 7) Règle Images/PDF (IMPORTANT)
Process obligatoire :
1) Upload fichier sur disque (images / pdf)
2) Générer une URL publique (ex: `/uploads/images/xyz.jpg`)
3) Sauvegarder uniquement l’URL dans la BD

Interdit :
- Stocker l’image en BLOB dans MySQL

---

## 8) Convention UI (anti-overflow)
Obligatoire dans JavaFX & Symfony :
- Table longue = **pagination** (ou scroll vertical propre)
- Zéro texte coupé / zéro overflow
- Pages BO : listing + filtres + actions

---

## 9) Workflow Git (anti-chaos)

### Branching minimal
- `main` : stable
- `dev` : intégration
- `feature/Mx-nom-feature` : dev par module

### Règles PR
- 1 PR = 1 feature claire
- Review obligatoire (au moins 1 personne)
- Pas de push direct sur `main`

### Checklist avant merge
- Build OK
- Connexion DB OK
- FO + BO testés
- 0 overflow UI
- Upload URL OK
- Schéma DB non cassé

---

## 10) Checklist Sprint

### Sprint 1 (JavaFX)
Pour chaque module :
- FO pages (liste + détail + form)
- BO pages (CRUD + filtres)
- DAO + Service + Model
- Validation champs

### Sprint 2 (Symfony)
Pour chaque module :
- Front Twig (listing + détail)
- Back Twig (CRUD custom)
- Controllers Front/Back séparés
- Entities + Repositories + Services
- Upload URL OK
- Security `/back/*`

---

## 11) Démo Finale (recommandée)
1) Login 3 rôles  
2) Agriculteur : terrain → culture → catalogue → livraison → paiement (prototype)  
3) Diagnostic : upload photo → résultat → historique  
4) Admin BO : users + statuts + génération PDF dossier  
5) Download rapport PDF via URL  

---

## 12) Troubleshooting
- Erreur DB : vérifier `schema.sql` et les FK
- Conflit Git : ne jamais modifier le schéma DB sans PR
- UI overflow : ajouter pagination/scroll immédiatement
- Accès BO non sécurisé : vérifier `access_control` et la sécurité Symfony

---

## 13) Liens (à compléter)
- GitHub Project Board : (mettre lien)
- Trello : (mettre lien)
- Rapport CDC PDF : (mettre lien / fichier)

Objectif : un projet organisé, intégrable, livrable sans surprise.
