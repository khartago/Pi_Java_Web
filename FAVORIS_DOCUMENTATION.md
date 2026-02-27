╔═════════════════════════════════════════════════════════════════════════════╗
║                                                                             ║
║              ❤️ FONCTIONNALITÉ FAVORIS/WISHLIST INTÉGRÉE ✅                 ║
║                                                                             ║
║                     Page "Mes Favoris" dans Marketplace                     ║
║                                                                             ║
╚═════════════════════════════════════════════════════════════════════════════╝


🎉 FONCTIONNALITÉS AJOUTÉES
═════════════════════════════════════════════════════════════════════════════════

✅ Bouton "❤️ Ajouter aux favoris" sur chaque produit du Marketplace
✅ Page dédiée "Mes Favoris" (Wishlist)
✅ Gestion des favoris: ajouter/retirer facilement
✅ Compteur de favoris
✅ Vider tous les favoris en un clic
✅ Voir les détails des produits favoris
✅ Symétrie avec les couleurs du design (coeur rouge ❤️)


═════════════════════════════════════════════════════════════════════════════════
📋 FICHIERS CRÉÉS ET MODIFIÉS
═════════════════════════════════════════════════════════════════════════════════

CRÉÉS:
  ✅ model/FavorisDAO.java                    - Gestion BDD des favoris
  ✅ controller/MesFavorisController.java     - Logique page favoris
  ✅ view/mes_favoris.fxml                    - Interface page favoris
  ✅ create_favoris_table.sql                 - Script création table BD

MODIFIÉS:
  ✅ controller/MarketplaceController.java    - Bouton + méthodes favoris
  ✅ view/marketplace.fxml                    - Bouton "Mes Favoris"


═════════════════════════════════════════════════════════════════════════════════
🚀 INSTALLATION & CONFIGURATION
═════════════════════════════════════════════════════════════════════════════════

ÉTAPE 1: Créer la table dans la base de données
────────────────────────────────────────────────

1. Ouvrez phpMyAdmin
2. Sélectionnez votre base "stockdb"
3. Allez à l'onglet SQL
4. Copiez-collez le contenu de: create_favoris_table.sql
5. Exécutez

Ou en ligne de commande MySQL:
  mysql -u root -p stockdb < create_favoris_table.sql

ÉTAPE 2: Compiler le projet
────────────────────────────

  mvn clean compile


ÉTAPE 3: Exécuter l'application
────────────────────────────────

  mvn javafx:run


═════════════════════════════════════════════════════════════════════════════════
💡 COMMENT UTILISER
═════════════════════════════════════════════════════════════════════════════════

AJOUTER AUX FAVORIS:
  1. Allez à "Marketplace - Catalogue"
  2. Parcourez les produits
  3. Cliquez sur "🤍 Ajouter" (bouton bleu)
  4. Le bouton devient "❤️ Favori" (rouge)

ACCÉDER AUX FAVORIS:
  1. Cliquez sur "❤️ Mes Favoris" (bouton bleu dans la barre d'outils)
  2. La page affiche tous vos produits favoris
  3. Titre affiche: "❤️ Mes Favoris (5)" (par exemple)

RETIRER DES FAVORIS:
  Option 1: Dans le Marketplace
  - Cliquez sur le bouton "❤️ Favori" pour retirer

  Option 2: Dans "Mes Favoris"
  - Cliquez sur "💔 Retirer des favoris"
  - Confirmez

VIDER TOUS LES FAVORIS:
  1. Allez à "Mes Favoris"
  2. Cliquez sur "🗑️ Vider tous les favoris"
  3. Confirmez la suppression


═════════════════════════════════════════════════════════════════════════════════
📊 STRUCTURE DE LA BASE DE DONNÉES
═════════════════════════════════════════════════════════════════════════════════

TABLE: favoris

Colonnes:
  - idFavoris (INT, PRIMARY KEY, AUTO_INCREMENT)
    Identifiant unique du favoris

  - idProduit (INT, UNIQUE, FOREIGN KEY)
    Référence au produit (chaque produit ne peut être ajouté qu'une fois)

  - dateAjout (TIMESTAMP)
    Date et heure d'ajout (défaut: CURRENT_TIMESTAMP)
    Utilisé pour trier les favoris par ordre décroissant

Contraintes:
  - UNIQUE sur idProduit: un produit ne peut être favori qu'une seule fois
  - FOREIGN KEY: intégrité référentielle avec la table produit
  - ON DELETE CASCADE: supprime les favoris si le produit est supprimé


═════════════════════════════════════════════════════════════════════════════════
🔍 DÉTAILS TECHNIQUES
═════════════════════════════════════════════════════════════════════════════════

CLASSE FavorisDAO:
  ✓ addFavoris(int idProduit)           - Ajoute un produit aux favoris
  ✓ removeFavoris(int idProduit)        - Retire un produit des favoris
  ✓ isFavoris(int idProduit)            - Vérifie si un produit est favori
  ✓ getAllFavoris()                     - Récupère tous les favoris
  ✓ countFavoris()                      - Compte le nombre de favoris
  ✓ clearAllFavoris()                   - Vide tous les favoris

CLASSE MesFavorisController:
  ✓ loadFavoris()                       - Charge les favoris
  ✓ renderCards(List<Produit>)         - Affiche les cartes
  ✓ createCard(Produit)                 - Crée une carte produit
  ✓ removeFavoris(Produit)              - Retire un favori
  ✓ clearAllFavoris()                   - Vide tous les favoris
  ✓ handleBack()                        - Retour au Marketplace
  ✓ openDetails(Produit)                - Voir détails du produit

CLASSE MarketplaceController (modifié):
  ✓ updateFavorisButton(Button, Produit) - Met à jour l'état du bouton
  ✓ toggleFavoris(Button, Produit)       - Active/désactive favori
  ✓ handleOpenFavoris()                  - Ouvre page "Mes Favoris"


═════════════════════════════════════════════════════════════════════════════════
🎨 DESIGN & UX
═════════════════════════════════════════════════════════════════════════════════

BOUTONS:
  ✓ "🤍 Ajouter"      - Bouton info (bleu), non favori
  ✓ "❤️ Favori"       - Bouton danger (rouge), favori
  ✓ "❤️ Mes Favoris"  - Bouton info (bleu) dans toolbar Marketplace
  ✓ "💔 Retirer"      - Bouton danger (rouge) dans page Favoris
  ✓ "🗑️ Vider tous"    - Bouton danger (rouge) dans page Favoris

PAGE MES FAVORIS:
  ✓ Header avec titre "❤️ FARMTECH"
  ✓ Titre: "❤️ Mes Favoris (5)" - compteur dynamique
  ✓ Grille de cartes produits (design identique au Marketplace)
  ✓ Message vide: "Aucun favori pour le moment"
  ✓ Bouton retour: "◀ Retour au Marketplace"
  ✓ Bouton vider: "🗑️ Vider tous les favoris"

CARTES PRODUITS:
  ✓ Image du produit
  ✓ Nom du produit
  ✓ Stock (quantité + unité)
  ✓ Date expiration (avec badge si expire bientôt)
  ✓ Boutons: Détails, Retirer des favoris


═════════════════════════════════════════════════════════════════════════════════
🔒 SÉCURITÉ & INTÉGRITÉ
═════════════════════════════════════════════════════════════════════════════════

✓ Contrainte UNIQUE sur idProduit
  → Empêche les doublons
  → Utilise DUPLICATE KEY UPDATE pour idempotence

✓ Foreign Key CASCADE
  → Si un produit est supprimé, ses favoris sont automatiquement supprimés

✓ Validation des données
  → Vérification des IDs avant opération
  → Gestion des exceptions SQL

✓ Confirmations avant suppression
  → Dialog de confirmation avant retirer/vider
  → Prévient les suppressions accidentelles


═════════════════════════════════════════════════════════════════════════════════
📈 PERFORMANCE
═════════════════════════════════════════════════════════════════════════════════

INDICES:
  ✓ idx_favoris_date     - Sur dateAjout (tri)
  ✓ idx_favoris_produit  - Sur idProduit (recherche)

REQUÊTES OPTIMISÉES:
  ✓ getAllFavoris() utilise INNER JOIN pour récupérer tous les produits favoris
  ✓ Tri par dateAjout DESC pour afficher les plus récents en premier
  ✓ Pas de requête N+1

CACHING (FUTUR):
  Pour optimiser davantage, vous pourriez ajouter:
  - Cache des favoris en mémoire
  - Invalidation au changement


═════════════════════════════════════════════════════════════════════════════════
🧪 TESTS RECOMMANDÉS
═════════════════════════════════════════════════════════════════════════════════

Test Manuel:
  ✓ Ajouter un produit aux favoris
  ✓ Vérifier que le bouton change en "❤️ Favori"
  ✓ Ouvrir "Mes Favoris" et vérifier que le produit y apparaît
  ✓ Retirer le produit des favoris
  ✓ Vérifier que le bouton revient à "🤍 Ajouter"
  ✓ Vérifier que le produit disparaît de "Mes Favoris"
  ✓ Vider tous les favoris
  ✓ Vérifier le message "Aucun favori pour le moment"
  ✓ Naviguer: Marketplace → Mes Favoris → Marketplace

Test de Données Incohérentes:
  ✓ Supprimer un produit favorisé → favori doit être supprimé automatiquement
  ✓ Ajouter plusieurs fois le même produit → pas de doublon


═════════════════════════════════════════════════════════════════════════════════
📝 NOTES & AMÉLIORATIONS FUTURES
═════════════════════════════════════════════════════════════════════════════════

Améliorations Possibles:
  ✓ Notifications toast au ajout/retrait
  ✓ Partage de favoris (liste publique)
  ✓ Export des favoris (CSV, PDF)
  ✓ Tri/filtrage dans "Mes Favoris" (par date, prix, expiration)
  ✓ Alertes price drop pour favoris
  ✓ Synchronisation cloud (multi-device)
  ✓ Favoris partagés (wishlist collaborative)
  ✓ Notes personnelles sur favoris


═════════════════════════════════════════════════════════════════════════════════
❓ DÉPANNAGE
═════════════════════════════════════════════════════════════════════════════════

PROBLÈME: Table favoris n'existe pas
SOLUTION: Exécutez le script create_favoris_table.sql

PROBLÈME: Bouton "Mes Favoris" ne s'affiche pas
SOLUTION: Vérifiez que marketplace.fxml a bien le bouton ajouté

PROBLÈME: Erreur de compilation "FavorisDAO not found"
SOLUTION: Vérifiez que FavorisDAO.java est bien dans model/

PROBLÈME: Exception SQL quand on ajoute aux favoris
SOLUTION: Vérifiez que la table favoris existe et que la structure est correcte

PROBLÈME: La page "Mes Favoris" n'affiche pas les produits
SOLUTION: Vérifiez que getAllFavoris() retourne bien les données


═════════════════════════════════════════════════════════════════════════════════

✨ C'EST PRÊT! Vous avez maintenant une fonctionnalité de Favoris complète! ✨

═════════════════════════════════════════════════════════════════════════════════

