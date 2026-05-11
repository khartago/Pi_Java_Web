# Audit parité entités Doctrine (farmtech-web) ↔ JavaFX JDBC (pidev-3a19)

**Source de vérité : Symfony** (entités + migrations). Après toute mise à jour : `cd farmtech-web && php bin/console doctrine:migrations:migrate`.

| Entité / table | Côté Java | Verdict | Notes |
|----------------|-----------|---------|--------|
| **Utilisateur** / `utilisateur` | `UserService` | Aligné | Rôles `ADMIN` / `FARMER` ; mots de passe bcrypt (voir `WEB_JAVAFX_PARITY.md`). |
| **Probleme** / `probleme` | `ProblemeService` | Aligné | |
| **Diagnostique** / `diagnostique` | `DiagnostiqueService` | Aligné | Lecture `SELECT *` inclut feedback ; écriture feedback via `enregistrerFeedback`. |
| **Produit** / `produit` | `ProduitDAO`, `FavorisDAO`, `MaterielDAO` | Aligné | |
| **Materiel** / `materiel` | `MaterielDAO` | Aligné | |
| **Promotion** / `promotion` + `promotion_produit` | `PromotionDAO` | Aligné | Plus de colonne fantôme `idProduit` sur `promotion` : liaisons N-N via `promotion_produit` (`promotion_id`, `produit_id`), comme Doctrine. |
| **Plantation** / `plantation` | `ProductionService`, `ProblemeService` | Aligné | Colonnes jeu (`stage`, `water_count`, …) réintroduites en BDD **et** dans [`Plantation`](c:/pi_me/farmtech-web/src/Entity/Plantation.php) + migration `Version20260511120000`. |
| **Production** / `production` | `ProductionPlanteService` | Aligné | Insertion avec auto-incrément (`idProduction` généré) ; plus d’insertion de l’id plantation par erreur. |
| **Article** / `article` | `ArticleService`, `ArticleDAO` | Aligné | Table et colonnes = Doctrine / migration blog. |
| **Blog** / `blog` | (résolution `BlogID` côté Java) | Partiel | Java exige au moins une ligne `blog` pour insérer un article. |
| **Commentaire** / `commentaire` | — | Web seul | |
| **Employe** / `employe` | — | Web seul | |
| **Affectation** / `affectation` | — | Web seul | |
| **Recommandation** / `recommandation` | — | Web seul | |
| **ProduitHistorique** / `produit_historique` | `ProduitHistoriqueDAO` | Aligné | Entité [`ProduitHistorique`](c:/pi_me/farmtech-web/src/Entity/ProduitHistorique.php) + migration `Version20260511120000` recréent la table supprimée par `Version20260503190000`. |
| **—** / `personne` | `PersonneService` | Hors projet web | Pas d’entité Symfony équivalente. |

## Synthèse

1. Exécuter les migrations (dont `Version20260511120000`) sur la base partagée `3a19`.
2. Les écrans Java **promotion**, **plantation/jeu**, **production (récolte)**, **historique produit** et **articles** sont alignés sur le schéma web actuel.

Voir [`WEB_JAVAFX_PARITY.md`](c:/pi_me/docs/WEB_JAVAFX_PARITY.md) pour la configuration JDBC / `DATABASE_URL` et les tests manuels.
