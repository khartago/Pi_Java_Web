# 📊 Fonctionnalités Statistiques et PDF

## Vue d'ensemble

Votre application dispose maintenant de deux nouvelles fonctionnalités majeures:

### 1. 📊 Tableau de Bord Statistiques
Une page complète dédiée à l'analyse de votre inventaire avec:
- **KPIs en temps réel**: Total produits, stock, santé générale
- **Alertes visuelles**: Produits expirés, stock faible
- **Graphiques interactifs**: Distribution par unité, top 10 produits
- **Actualisation instantanée**: Données fraîches à chaque consultation

### 2. 📄 Génération de Rapports PDF
Créez des rapports professionnels pour:
- **Archivage**: Conservez des traces écrites de votre inventaire
- **Analyses**: Export des statistiques pour traitement externe
- **Rapports exécutifs**: Résumés pour la direction
- **Suivi dans le temps**: Comparez les inventaires d'une période à l'autre

---

## 🎯 Accès aux Fonctionnalités

### Depuis le Tableau Principal (Produits)
```
Cliquez sur: [📊 Statistiques]
```

La page de statistiques s'ouvrira avec:
- 8 cartes de KPIs colorées
- 2 graphiques en barres
- Options d'export PDF
- Bouton retour

### Depuis la Page de Statistiques
```
Cliquez sur: [📥 Exporter en PDF]
```

Un dialogue de sauvegarde fichier apparaîtra pour choisir:
- L'emplacement du fichier
- Le nom du rapport

---

## 📋 Types de Rapports PDF

### 1. Rapport Complet (Recommandé)
**Contenu:**
- Page 1: Statistiques résumées + KPIs
- Page 2: Produits expirés et expirant bientôt
- Page 3: Produits en rupture de stock
- Page 4+: Inventaire complet

**Commande:**
```java
pdfService.generateComprehensiveReport("rapport_complet.pdf");
```

### 2. Rapport d'Expiration
**Contenu:**
- Produits expirés (en rouge)
- Produits expirant dans 7 jours (en orange)
- Dates exactes d'expiration

**Commande:**
```java
pdfService.generateExpirationReport("rapport_expiration.pdf");
```

### 3. Rapport de Stock
**Contenu:**
- KPIs de stock
- Produits en rupture
- Top 10 par quantité
- Analyse par unité

**Commande:**
```java
pdfService.generateStockReport("rapport_stock.pdf");
```

### 4. Rapport d'Inventaire
**Contenu:**
- Liste complète des produits
- Statistiques générales
- Détails prix unitaire

**Commande:**
```java
pdfService.generateProductListReport("rapport_inventaire.pdf");
```

---

## 🔢 Indicateurs Clés (KPIs)

| Indicateur | Description | Formule |
|-----------|-------------|---------|
| **Total Produits** | Nombre de références différentes | COUNT(produits) |
| **Stock Total** | Quantité cumulée de tous les produits | SUM(quantité) |
| **Stock Moyen** | Quantité moyenne par produit | SUM(quantité) / COUNT(produits) |
| **Score Santé** | État général de l'inventaire (%) | 100 - (problèmes / total × 100) |
| **Produits Expirés** | Nombre de produits avec dateExp < aujourd'hui | COUNT(exp < now) |
| **Expirant Bientôt** | Nombre de produits expirant dans 7 jours | COUNT(exp <= now+7j) |
| **Faible Stock** | Nombre de produits avec quantité ≤ 10 | COUNT(qty ≤ 10) |
| **Valeur Stock** | Valeur financière totale | SUM(quantité × prix) |

---

## 📈 Graphiques Disponibles

### Graphique 1: Top 10 Produits par Quantité
- **Type**: BarChart horizontal
- **Axes**: Produits (X) vs Quantités (Y)
- **Utilité**: Identifier les produits les plus stockés
- **Mise à jour**: En temps réel

### Graphique 2: Distribution par Unité
- **Type**: BarChart
- **Axes**: Unités (X) vs Nombre de produits (Y)
- **Utilité**: Voir la répartition des unités (kg, l, m², etc.)
- **Mise à jour**: En temps réel

---

## 🎨 Codage Couleur

### Cartes KPI
| Couleur | Signification |
|---------|---------------|
| 🔵 Bleu | Informations générales |
| 🟢 Vert | Données positives |
| 🟠 Orange | Données à surveiller |
| 🟣 Violet | Indicateur de santé |
| 🔴 Rouge | Alertes critiques |

### Rapports PDF
```
En-têtes:    Bleu foncé (#2196F3)
Sous-titres: Bleu clair (#4075B0)
Cellules:    Alternance blanc/gris
Texte:       Noir standard
```

---

## ⚙️ Configuration des Seuils

Les seuils suivants peuvent être personnalisés dans `StatisticsService`:

```java
// Construction par défaut (7 jours, stock min 10)
StatisticsService stats = new StatisticsService(produitDAO);

// Construction personnalisée (14 jours, stock min 5)
StatisticsService stats = new StatisticsService(produitDAO, 14, 5);
```

### Paramètres
| Paramètre | Défaut | Description |
|-----------|--------|-------------|
| `daysBeforeExpiration` | 7 | Jours avant expiration pour une alerte |
| `minStockQuantity` | 10 | Quantité minimale avant alerte stock faible |

---

## 🚀 Cas d'Usage Pratiques

### Cas 1: Audit Hebdomadaire
```
1. Chaque lundi, ouvrir "Statistiques"
2. Vérifier le "Score Santé"
3. Exporter le PDF pour les archives
4. Notifier si "Produits Expirés" > 0
```

### Cas 2: Prévention d'Expiration
```
1. Consulter "Expirant Bientôt"
2. Si > 0, déclencher une alerte
3. Exporter le rapport d'expiration
4. Contactez le fournisseur pour replacement
```

### Cas 3: Gestion du Stock
```
1. Vérifier "Faible Stock"
2. Identifier les produits critiques
3. Commander immédiatement si < 5 unités
4. Exporter le rapport de stock
```

### Cas 4: Rapport pour la Direction
```
1. Ouvrir "Statistiques"
2. Exporter le "Rapport Complet"
3. Inclure le PDF dans le mail mensuel
4. Ajouter analyse personnelle
```

---

## 📊 Exemples de Statistiques

### Scénario 1: Inventaire Sain
```
Total Produits:      25
Stock Total:        450
Stock Moyen:        18.0
Score Santé:      100.0%
Produits Expirés:    0
Expirant Bientôt:    1
Faible Stock:        0
Valeur Stock:     1250.00€
```

### Scénario 2: Inventaire à Risque
```
Total Produits:      25
Stock Total:        120
Stock Moyen:         4.8
Score Santé:       64.0%
Produits Expirés:    2
Expirant Bientôt:    5
Faible Stock:        8
Valeur Stock:      350.00€
```

---

## 🔧 Intégration avec Autres Modules

### Avec Module Email
```java
// Les statistiques alimentent les notifications email
ExpirationNotifierService notifier = ...
List<Produit> expiring = statisticsService.getExpiringProducts();
notifier.notifyByEmail("admin@company.com", 7);
```

### Avec Module QR Code
```java
// Chaque scan met à jour les statistiques
StatisticsService.getTotalProducts(); // Inclut produits scannés
```

### Avec Module Traçabilité
```java
// Historique dans ProduitHistorique
// Statistiques basées sur l'état actuel
statisticsService.getHealthScore(); // État snapshot
```

---

## ❓ FAQ

**Q: Comment actualiser les statistiques?**
A: Cliquez sur le bouton "🔄 Actualiser" en bas de page.

**Q: Les rapports PDF sont-ils sécurisés?**
A: Oui, ils sont stockés localement sur votre ordinateur. Aucun envoi externe.

**Q: Puis-je exporter en Excel?**
A: Pas encore. Pour l'instant, seul le PDF est supporté.

**Q: Le score santé peut-il être négatif?**
A: Non, le minimum est 0%. Le maximum est 100%.

**Q: Les graphiques se mettent à jour automatiquement?**
A: Oui, cliquez sur "Actualiser" pour forcer la mise à jour.

**Q: Puis-je modifier les seuils d'alerte?**
A: Oui, modifiez les paramètres dans `StatisticsService` (code source).

---

## 📞 Support

Pour toute question ou problème:
1. Consultez le fichier `PDF_ET_STATISTIQUES_GUIDE.md`
2. Vérifiez les logs d'erreur dans la console
3. Assurez-vous que votre base de données contient des produits
4. Redémarrez l'application

---

## 📝 Changelog

### Version 1.0.0 (Actuelle)
- ✅ Service de statistiques complet
- ✅ Génération de rapports PDF
- ✅ Interface statistiques avec graphiques
- ✅ 4 types de rapports différents
- ✅ KPIs en temps réel
- ✅ Export PDF intégré

### Prévisions Futures
- 📋 Export Excel
- 📊 Graphiques en camembert
- 📈 Tendances temporelles
- 🔔 Alertes système
- 📱 API REST statistiques

---

## 🎓 Ressources d'Apprentissage

- **iText 7 Documentation**: https://itextpdf.com/en
- **JavaFX Charts**: https://docs.oracle.com/javase/8/javafx/api/javafx/scene/chart/package-summary.html
- **SQL Agregation**: https://dev.mysql.com/doc/refman/8.0/en/aggregate-functions.html

---

**Enjoy votre nouveau système de statistiques! 📊✨**

