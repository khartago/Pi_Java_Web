# Web (Symfony) et JavaFX — parité et vérification

Les deux clients partagent **une seule base MySQL** : pas d’API HTTP entre Symfony et le bureau. Les actions faites sur le web sont visibles dans JavaFX après **rechargement des données** (navigation, filtres, bouton **Actualiser**, ou retour de fenêtre avec focus).

**Audit complet entités ↔ JDBC :** voir [`ENTITY_PARITY_AUDIT.md`](c:/pi_me/docs/ENTITY_PARITY_AUDIT.md) (schéma aligné sur les migrations Symfony ; exécuter `php bin/console doctrine:migrations:migrate` après mise à jour du dépôt).

## Configuration base de données

| Client | Fichier / emplacement | Exemple |
|--------|------------------------|---------|
| Symfony | `farmtech-web/.env` ou `.env.local` | `DATABASE_URL="mysql://USER:PASS@127.0.0.1:3306/3a19?serverVersion=...&charset=utf8mb4"` |
| JavaFX | `pidev-3a19/src/main/resources/database.properties` (optionnel) | Copier depuis `database.properties.example` |

Si `database.properties` est absent, JavaFX utilise les valeurs par défaut : `jdbc:mysql://127.0.0.1:3306/3a19`, utilisateur `root`, mot de passe vide — alignées sur `.env.example` du web.

**À faire une fois par environnement :** appliquer les migrations Doctrine sur la base utilisée par les deux clients :

```bash
cd farmtech-web && php bin/console doctrine:migrations:migrate
```

## Mots de passe (bcrypt + migration)

- Symfony utilise **bcrypt** avec migration depuis les anciens mots de passe **en clair** : à la première connexion web réussie, le hash est mis à jour en base.
- JavaFX : à l’inscription ou à la modification utilisateur, le mot de passe est stocké en **bcrypt**. La connexion accepte encore un ancien mot de passe en clair le temps de la transition.

## Checklist manuelle E2E (web → JavaFX)

Pour chaque ligne : effectuer l’action sur le **web**, puis dans **JavaFX** ouvrir l’écran correspondant et **Actualiser** (ou rouvrir la vue), puis vérifier id / libellés / état.

### 1. Comptes (`utilisateur`)

| Étape | Web | JavaFX |
|-------|-----|--------|
| Création | `/register` — créer un fermier | Connexion avec le même email / mot de passe |
| Admin (si applicable) | Création / édition utilisateur admin | Connexion rôle admin, liste utilisateurs |

**Colonne `role` :** même valeurs que Doctrine — `ADMIN` ou `FARMER` uniquement (`Utilisateur::ROLE_ADMIN_DB` / `ROLE_FARMER_DB`). Le web et JavaFX doivent écrire la même chaîne.

### 2. Problèmes fermier (`probleme`)

| Étape | Web | JavaFX |
|-------|-----|--------|
| Création | Espace fermier — nouveau problème | Tableau de bord fermier — **Actualiser** — la carte apparaît |
| Même utilisateur | Se connecter avec le même compte sur les deux clients | Les `id_utilisateur` doivent correspondre |

### 3. Support admin (`probleme`, `diagnostique`)

| Étape | Web | JavaFX |
|-------|-----|--------|
| Liste / état | Admin — problèmes | Menu admin Support — **Actualiser** |
| Diagnostic | Créer ou modifier un diagnostic côté web (ou JavaFX) | Vérifier cohérence des états : `EN_ATTENTE`, `DIAGNOSTIQUE_DISPONIBLE`, `REOUVERT`, `CLOTURE`, etc. |
| Acceptation diagnostic | Workflow web | Bouton « Accepter le diagnostic » côté JavaFX si applicable |

### 4. Photos problèmes

| Étape | Web | JavaFX |
|-------|-----|--------|
| Chemins | Upload via Symfony (`public/uploads/...`) | Détail problème JavaFX : mêmes chemins ou URLs selon convention déployée |

### 5. Modules non partagés

Marketplace, blog, production, etc. : ne tester la parité **que** si les deux applications lisent les **mêmes tables**. Sinon la fonctionnalité est propre à un client.

## Critères de succès

- Un seul schéma MySQL documenté pour dev et CI.
- Checklist ci-dessus exécutée sur les flux prioritaires sans divergence de schéma ni d’`etat`.
- Compréhension partagée : **web → base → JavaFX au rechargement**, pas de miroir temps réel sans mécanisme additionnel (polling, WebSocket, etc.).
