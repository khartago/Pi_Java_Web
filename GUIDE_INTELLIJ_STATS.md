# 🎯 GUIDE IntelliJ IDEA - Statistiques & PDF

## ⚡ Démarrage en 5 Clics dans IntelliJ

### Clic 1: Recharger Maven
```
1. Clic droit sur pom.xml (dans la racine du projet)
2. Sélectionner "Maven"
3. Cliquer "Reload Projects"
4. ⏳ Attendre 30-60 secondes
```

**Vous devez voir**: "Maven project imported successfully" en bas

### Clic 2: Compiler le Projet
```
1. Menu: Build → Rebuild Project
   Ou raccourci: Ctrl + Shift + F9
```

**Vous devez voir**: "Compilation completed successfully" en bas

### Clic 3: Lancer l'Application
```
1. Cliquer le triangle vert (Run)
   Ou appuyer sur: Shift + F10
2. Sélectionner "MainApp.main()" si demandé
```

**Vous devez voir**: L'application JavaFX démarre

### Clic 4: Cliquer le Bouton Statistiques
```
Dans l'application:
1. Cliquer le bouton [📊 Statistiques]
2. La page de statistiques s'ouvre
```

**Vous devez voir**: 8 cartes KPI + 2 graphiques

### Clic 5: Exporter en PDF
```
Sur la page Statistiques:
1. Cliquer [📥 Exporter en PDF]
2. Choisir un dossier (ex: Bureau)
3. Nommer le fichier
4. Cliquer Enregistrer
```

**Vous devez voir**: Fichier PDF créé

---

## 🛠️ Menus Utiles dans IntelliJ

### File (Fichier)
```
File → Open Project
  → Ouvrir un projet existant

File → Project Structure
  → Configurer SDK Java

File → Invalidate Caches
  → Si problèmes de compilation
```

### Build (Construction)
```
Build → Rebuild Project
  → Recompiler le projet

Build → Clean Project
  → Nettoyer les fichiers compilés

Build → Run
  → Exécuter l'application
```

### View (Affichage)
```
View → Tool Windows → Project
  → Voir la structure des fichiers

View → Tool Windows → Run
  → Voir les logs d'exécution

View → Tool Windows → Maven
  → Voir les statuts Maven

View → Tool Windows → Problems
  → Voir les erreurs de compilation
```

### Run (Exécution)
```
Run → Run 'MainApp'
  → Exécuter l'application

Run → Debug 'MainApp'
  → Déboguer l'application

Run → Stop
  → Arrêter l'application
```

---

## ⌨️ Raccourcis Clavier Essentiels

| Raccourci | Action |
|-----------|--------|
| `Ctrl + Shift + F9` | Recompiler |
| `Shift + F10` | Exécuter |
| `Shift + F9` | Déboguer |
| `Ctrl + S` | Sauvegarder |
| `Ctrl + /` | Commenter/Décommenter |
| `Ctrl + F` | Chercher |
| `Ctrl + H` | Remplacer |
| `Ctrl + B` | Aller à la définition |
| `Alt + Entrée` | Quick fix (suggestions) |
| `Ctrl + Space` | Auto-complétion |

---

## 🔍 Fenêtres Importantes

### 1. Project (Gauche)
```
Affiche: Structure des fichiers du projet
Utilité: Naviguer entre les fichiers
Action: Clic = ouvrir fichier
Raccourci: Alt + 1
```

### 2. Run (Bas)
```
Affiche: Logs d'exécution et erreurs
Utilité: Vérifier que tout fonctionne
Erreurs: Affichées en rouge
Raccourci: Alt + 4
```

### 3. Problems (Bas)
```
Affiche: Erreurs de compilation
Utilité: Corriger les bugs
Cliquable: Double-clic va à l'erreur
Raccourci: Alt + 6
```

### 4. Debug (Bas)
```
Affiche: Variables et stacktrace en debug
Utilité: Déboguer le code
Raccourci: Alt + 5
```

---

## 🐛 Dépannage dans IntelliJ

### Erreur: "Cannot find symbol"

**Étapes à suivre**:
```
1. Clic droit pom.xml
2. Maven → Reload Projects
3. Attendre 30-60 secondes
4. Build → Rebuild Project
5. Vérifier onglet "Problems" en bas
```

### Erreur: "Exception in Application start method"

**Étapes à suivre**:
```
1. View → Tool Windows → Run
2. Lire le message d'erreur en rouge
3. Chercher le mot-clé (ex: "FXML", "NullPointer")
4. Corriger le fichier indiqué
5. Rebuild et relancer
```

### Erreur: "FXML file not found"

**Vérification**:
```
1. Project → View → Expand src/main/resources
2. Vérifier que view/statistiques.fxml existe
3. Si absent: File → New → File → statistiques.fxml
4. Copier le contenu manquant
```

### Erreur: Maven ne télécharge pas

**Solutions**:
```
1. File → Settings
2. Build, Execution, Deployment → Maven
3. Vérifier Repository Settings
4. Cliquer "Reload Projects"
5. Attendre le téléchargement
```

---

## 📁 Structure dans IntelliJ

### Comment Ouvrir les Fichiers

#### Services
```
src/main/java
  → service
    → StatisticsService.java (Double-clic)
    → PdfService.java (Double-clic)
```

#### Contrôleurs
```
src/main/java
  → controller
    → StatisticsController.java (Double-clic)
    → ProduitController.java (Double-clic)
```

#### Vues (FXML)
```
src/main/resources
  → view
    → statistiques.fxml (Double-clic)
    → produit_list.fxml (Double-clic)
```

#### Configuration
```
src/main/resources
  → config.properties (Double-clic)
```

#### Documentation
```
(Racine du projet)
  → DEMARRAGE_RAPIDE_STATS.md (Double-clic)
  → etc.
```

---

## 🎯 Workflow Complet dans IntelliJ

### Ajouter/Modifier une Fonctionnalité

```
1. OUVRIR LE FICHIER
   → Project → Double-clic sur fichier

2. ÉDITER LE CODE
   → Taper le code dans l'éditeur
   → IntelliJ affiche les erreurs en rouge

3. SAUVEGARDER
   → Ctrl + S (auto-sauvegarde activée)

4. COMPILER
   → Ctrl + Shift + F9

5. VÉRIFIER LES ERREURS
   → View → Tool Windows → Problems

6. CORRIGER
   → Cliquer l'erreur → Alt + Entrée pour suggestion

7. RELANCER
   → Shift + F10

8. TESTER
   → Cliquer les boutons dans l'app
```

---

## 💡 Tips & Tricks IntelliJ

### 1. Auto-Complétion
```
Taper: `stats.`
Attendre: ListOptions popup
Choisir: Méthode souhaitée
Valider: Entrée
```

### 2. Docs Rapides
```
Survoler: Classe/méthode
Attendre: Tooltip avec doc
Ou: Ctrl + Q pour doc détaillée
```

### 3. Rechercher un Fichier
```
Ctrl + Shift + O
Taper: Nom du fichier (ex: Statistics)
Entrée: Ouvrir
```

### 4. Rechercher dans Fichier
```
Ctrl + F
Taper: Texte cherché
Entrée: Naviguer
```

### 5. Renommer Partout
```
Clic droit → Refactor → Rename
Taper: Nouveau nom
Entrée: Renommer partout
```

### 6. Formater le Code
```
Sélectionner du code
Ctrl + Alt + L: Formater
```

### 7. Organiser les Imports
```
Ctrl + Alt + O: Nettoyer imports
```

---

## 🔄 Cycle de Développement

### Étape 1: Modifier le Code
```
double-clic fichier.java
Modifier le code
Ctrl + S (sauvegarder)
```

### Étape 2: Compiler
```
Ctrl + Shift + F9 (compiler)
Attendre "Compilation completed"
```

### Étape 3: Vérifier les Erreurs
```
Alt + 6 (onglet Problems)
Vérifier pas d'erreur en rouge
Sinon: Cliquer erreur → corriger
```

### Étape 4: Exécuter
```
Shift + F10 (Run)
Ou cliquer triangle vert
```

### Étape 5: Tester
```
Dans l'application:
- Cliquer les boutons
- Entrer des données
- Tester les features
```

### Étape 6: Déboguer (si besoin)
```
Shift + F9 (Debug)
Ou cliquer "Debug" au lieu de "Run"
```

---

## 🐛 Déboguer dans IntelliJ

### Ajouter un Breakpoint
```
1. Cliquer à gauche du numéro de ligne
2. Point rouge apparaît
3. Exécuter en Debug (Shift + F9)
4. App s'arrête au breakpoint
```

### Inspecter une Variable
```
1. Clic droit sur variable
2. Sélectionner "Evaluate Expression"
3. Voir la valeur courante
```

### Exécuter Pas à Pas
```
F10: Step Over (prochaine ligne)
F11: Step Into (entrer dans fonction)
Shift + F11: Step Out (sortir de fonction)
```

### Reprendre l'Exécution
```
F9: Reprendre (Resume)
```

---

## 📊 Monitorer la Performance

### Vérifier la Consommation Mémoire
```
View → Tool Windows → Memory
Affiche: Utilisation RAM en temps réel
```

### Vérifier le CPU
```
View → Tool Windows → Profiler
Affiche: Processeur et mémoire
```

---

## 🔐 Configurations IntelliJ

### SDK Java
```
File → Project Structure
  → Project → SDK
  → Vérifier JDK 17 ou + sélectionné
```

### Compiler
```
File → Project Structure
  → Project → Compiler Output
  → Vérifier chemin correct
```

### Run Configuration
```
Run → Edit Configurations
  → Vérifier MainApp selected
  → Vérifier VM options si besoin
```

---

## 📋 Checklist Avant de Lancer

- [ ] Maven rechargé
- [ ] Pas d'erreur compilation (Alt + 6)
- [ ] pom.xml valide
- [ ] Tous les fichiers existent
- [ ] SDK Java 17+ configuré
- [ ] Pas de fichier non sauvegardé
- [ ] BDD accessible
- [ ] Aucun autre instance en cours d'exécution

---

## 🆘 Blocage? Essayez Ceci

### Si tout est rouge et cassé:
```
1. File → Invalidate Caches → Invalidate and Restart
2. Relancer IntelliJ
3. Clic droit pom.xml → Reload Projects
4. Build → Clean Project
5. Build → Rebuild Project
```

### Si Maven ne télécharge pas:
```
1. File → Settings → Build, Execution, Deployment → Maven
2. Vérifier le "Local repository"
3. Peut être vide → Télécharger manuellement
```

### Si l'app ne démarre pas:
```
1. Vérifier console (Alt + 4)
2. Chercher la ligne d'erreur
3. Clic droit → Go to Source
4. Corriger le problème
```

---

## 🎓 Ressources IntelliJ

### Documentation Officielle
- https://www.jetbrains.com/help/idea/

### Tutorials (En français)
- https://www.jetbrains.com/fr/help/idea/

### Shortcuts (Pdf)
- https://resources.jetbrains.com/storage/products/intellij-idea/IntelliJ_IDEA_ReferenceCard.pdf

---

## ✅ Prêt à Commencer?

1. Ouvrez IntelliJ
2. Ouvrez le projet
3. Suivez les 5 clics ci-dessus
4. Profitez! 🎉

---

**Happy Coding dans IntelliJ! 🚀**

