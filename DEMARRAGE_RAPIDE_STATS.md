# 🚀 DÉMARRAGE RAPIDE - Statistiques & PDF

## 5 Minutes pour Commencer

### ✅ Étape 1: Vérifier les Fichiers (1 min)

Assurez-vous que ces fichiers existent:

```
✓ src/main/java/service/StatisticsService.java
✓ src/main/java/service/PdfService.java
✓ src/main/java/controller/StatisticsController.java
✓ src/main/resources/view/statistiques.fxml
✓ src/main/java/controller/ProduitController.java (modifié)
✓ src/main/resources/view/produit_list.fxml (modifié)
✓ pom.xml (modifié - dépendances iText ajoutées)
```

### ✅ Étape 2: Rechargez Maven (2 min)

Dans IntelliJ IDEA:
```
1. Clic droit sur pom.xml
2. Sélectionner "Maven"
3. Cliquer "Reload Projects"
4. ⏳ Attendre le téléchargement (~30-60s)
```

Vous devez voir:
```
✓ itext-core 8.0.4 téléchargé
✓ javafx-swing 17.0.2 téléchargé
✓ Pas d'erreur Maven
```

### ✅ Étape 3: Lancer l'Application (2 min)

```
1. Clic sur le triangle vert (Run)
2. ⏳ Attendre le démarrage (30-60s)
3. Vérifier: Pas d'erreur d'initialisation
```

### ✅ Étape 4: Tester les Statistiques (1 min)

```
1. Cliquer sur [📊 Statistiques]
2. ✓ Page se charge
3. ✓ Vous voyez 8 cartes KPI colorées
4. ✓ Vous voyez 2 graphiques en barres
5. ✓ Boutons présents: Actualiser, Exporter, Retour
```

### ✅ Étape 5: Exporter un PDF (2 min)

```
1. Sur la page Statistiques
2. Cliquer [📥 Exporter en PDF]
3. Sélectionner dossier (ex: Bureau)
4. Nommer fichier (ex: rapport_test.pdf)
5. Cliquer Enregistrer
6. ✓ Fichier PDF créé
7. ✓ Ouvrir le PDF pour vérifier
```

---

## ⚡ Commandes Rapides

### Recharger Maven
```bash
Clic droit pom.xml → Maven → Reload Projects
```

### Compiler Uniquement
```bash
Clic droit projet → Maven → Compile
```

### Exécuter l'App
```bash
Clic sur Run (triangle vert)
Ou: Ctrl + Shift + F10 (Windows)
```

### Voir les Erreurs
```bash
View → Tool Windows → Build (onglet Error)
```

---

## 🔍 Vérification Rapide des Erreurs

### Erreur: "Cannot find symbol"

**Cause**: Maven n'a pas téléchargé les dépendances

**Solution**:
```
Clic droit pom.xml → Maven → Reload Projects
Attendre 60 secondes
Ctrl + Shift + F9 (Recomplile)
```

### Erreur: "FXML file not found"

**Cause**: Fichier `statistiques.fxml` manquant

**Solution**:
```
Vérifier que le fichier existe à:
src/main/resources/view/statistiques.fxml

Si absent, recréer le fichier en copiant le contenu
```

### Erreur: "Controller not found"

**Cause**: Le controller n'est pas accessible

**Solution**:
```
1. Vérifier: fx:controller="controller.StatisticsController"
2. Vérifier: le fichier StatisticsController.java existe
3. Recompiler: Ctrl + Shift + F9
```

### Erreur: "PDF not generated"

**Cause**: Permissions ou chemin incorrect

**Solution**:
```
1. Sélectionner un dossier où vous avez les droits (Bureau, Documents)
2. Vérifier que le dossier existe
3. Essayer un chemin plus court
Exemple: C:\temp\rapport.pdf
```

---

## 📊 Tester les Statistiques

### Avec Données Complètes

Supposons que vous ayez:
- 25 produits
- Stock total: 450 unités
- 2 produits expirés
- 5 produits expirant bientôt

**Résultat attendu**:
```
Total Produits: 25
Stock Total: 450
Stock Moyen: 18.0
Score Santé: 72.0%
Expirés: 2
Expirant: 5
Faible Stock: 3
Valeur: 1250.00€
```

### Avec Données Minimales

Même avec 1 seul produit:
```
Total Produits: 1
Stock Total: 10
Stock Moyen: 10.0
Score Santé: 100.0%
Expirés: 0
Expirant: 0
Faible Stock: 0
Valeur: 50.00€
```

---

## 🎯 Navigation Complète

### Flux Normal
```
Menu Principal
  ↓
[Cliquer 📊 Statistiques]
  ↓
Page Statistiques
  ├─ Voir KPIs
  ├─ Voir Graphiques
  ├─ [Actualiser] → Mise à jour données
  ├─ [Exporter PDF] → Créer rapport
  └─ [Retour] → Menu Principal
```

### Touches Clavier Utiles
```
Ctrl + Shift + F10    → Exécuter l'application
Ctrl + Shift + F9     → Recompiler
F5                    → Actualiser (peut varier)
Ctrl + S              → Sauvegarder
```

---

## 💡 Conseils Pro

### 1. Avant de Commencer
- ✅ Assurez-vous d'avoir des produits en BDD
- ✅ Vérifiez que la BDD est accessible
- ✅ Rechargez Maven en cas de doute

### 2. Lors de l'Exécution
- ✅ Consultez les logs (View → Tool Windows → Run)
- ✅ Notez les messages d'erreur exacts
- ✅ Cherchez le numéro de ligne d'erreur

### 3. Pour Exporter un PDF
- ✅ Utilisez un chemin court et simple
- ✅ Évitez les caractères spéciaux dans le nom
- ✅ Vérifiez les permissions d'écriture

### 4. Pour Déboguer
- ✅ Ajoutez des `System.out.println()` si nécessaire
- ✅ Consultez les logs console pour les stacktraces
- ✅ Activez le mode debug (Shift + F9 au lieu de F10)

---

## ❌ Si Ça Ne Marche Pas

### Étape 1: Nettoyer et Recompiler
```
Clic droit projet
→ Maven
→ Clean
→ Compiler
```

### Étape 2: Invalider le Cache
```
File → Invalidate Caches
→ Checkboxes
→ Invalidate and Restart
```

### Étape 3: Vérifier les Erreurs
```
View → Tool Windows → Problems
ou
View → Tool Windows → Build
```

### Étape 4: Consulter les Logs
```
View → Tool Windows → Run
→ Chercher les messages d'erreur en rouge
```

### Étape 5: Demander de l'Aide
- Notez l'erreur exacte
- Regardez le numéro de ligne
- Consultez les fichiers documentation

---

## ✨ Fonctionnalités Disponibles

Après succès de la configuration:

| Fonctionnalité | Accès | Résultat |
|---|---|---|
| KPIs | Page Statistiques | 8 cartes colorées |
| Graphiques | Page Statistiques | 2 BarCharts |
| Actualiser | Bouton Actualiser | Données fraîches |
| Export PDF | Bouton Exporter | Rapport téléchargé |
| Navigation | Bouton Retour | Écran Produits |

---

## 📋 Checklist de Vérification

- [ ] Fichiers Java existent (4 fichiers)
- [ ] Fichier FXML existe (statistiques.fxml)
- [ ] pom.xml modifié (iText ajouté)
- [ ] Maven rechargé (dépendances téléchargées)
- [ ] Application lance sans erreur
- [ ] Bouton "📊 Statistiques" visible
- [ ] Page Statistiques s'ouvre au clic
- [ ] KPIs affichés
- [ ] Graphiques affichés
- [ ] Export PDF fonctionne
- [ ] Fichier PDF créé et lisible

---

## 🎓 Prochaines Étapes

Une fois fonctionnel:

1. **Explorez les Statistiques**
   - Consultez les KPIs
   - Analysez les graphiques
   - Comprenez votre inventaire

2. **Utilisez l'Export PDF**
   - Générez des rapports
   - Archivez les données
   - Partagez les analyses

3. **Personnalisez** (Optionnel)
   - Modifiez les seuils d'alerte
   - Ajoutez des graphiques
   - Créez des nouveaux rapports

4. **Intégrez avec d'Autres Modules**
   - Email + Statistiques
   - QR Code + Historique
   - Marketplace + Analyse

---

## 📞 Support Rapide

### Question: Pourquoi pas de données?
**Réponse**: Vérifiez que la BDD contient des produits

### Question: Le PDF ne se crée pas?
**Réponse**: Utilisez un chemin simple (Bureau, Documents)

### Question: Les graphiques sont vides?
**Réponse**: Cliquez sur "Actualiser"

### Question: Erreur "Cannot find symbol"?
**Réponse**: Rechargez Maven (Reload Projects)

### Question: Comment modifier les seuils?
**Réponse**: Éditez `StatisticsService.java`, ligne ~20

---

**Prêt? Lancez l'application et testez! 🚀**

