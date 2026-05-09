# 🚨 Système d'Alertes Critiques - Guide de Démarrage Rapide

## Installation

Tout est déjà intégré ! Les fichiers suivants ont été créés automatiquement :

```
src/main/java/Services/CriticalAlertNotifier.java    ✅ Créé
src/main/java/controller/AlertController.java         ✅ Créé
src/main/resources/view/alerts.fxml                   ✅ Créé
```

## Utilisation

### 1. Lancer l'application
```bash
mvn clean compile javafx:run
```

### 2. Se connecter comme ADMIN
- Email : un compte avec le rôle `ADMIN`
- Mot de passe : votre mot de passe

### 3. Accéder aux alertes
- Menu latéral → Cliquez sur `🚨 Alertes critiques`

### 4. Configurer et envoyer des alertes

```
┌─────────────────────────────────────────┐
│  🚨 Gestion des Alertes Critiques      │
├─────────────────────────────────────────┤
│                                          │
│  Résumé des alertes              Paramètres
│  ├─ 📦 Stock faible: 3            ├─ Seuil: 10
│  └─ 🔧 En panne: 1                ├─ Email: ...
│                                    └─ [Actualiser] [Envoyer]
│
│  ┌──────────────────────────────────────┐
│  │ Onglets                              │
│  ├──────┬──────────────────────────────┤
│  │ 📦 Stock Faible │ 🔧 En Panne │
│  ├──────────────────────────────────────┤
│  │ [Tableau des produits]               │
│  │ [Tableau des matériels]              │
│  └──────────────────────────────────────┘
```

## Scénarios d'usage

### Scénario 1 : Vérifier les alertes du jour
1. Cliquez sur `🔄 Actualiser`
2. Consultez les onglets pour voir les produits en stock faible
3. Consultez l'onglet matériels pour voir les équipements en panne

### Scénario 2 : Envoyer une notification au responsable
1. Modifiez le **Seuil de stock** si nécessaire (ex: 15 au lieu de 10)
2. Entrez l'**Email destinataire** (ex: responsable@farmtech.tn)
3. Cliquez sur **📤 Envoyer les alertes**
4. Attendez la confirmation
5. Vérifiez votre email (spam possible)

### Scénario 3 : Suivi mensuel
1. Chaque fin de mois, ouvrez la page des alertes
2. Générez un rapport avec un seuil personnalisé
3. Envoyez par email à la direction

## Configuration email

### Option 1 : Utiliser Gmail (par défaut)
```java
String fromEmail = "amnafati94@gmail.com";
String appPassword = "jcdo lljy fgug omgr";
```

### Option 2 : Utiliser vos identifiants
1. Créez un **App Password** sur votre compte Gmail
2. Définissez une variable d'environnement :
   ```bash
   set GMAIL_APP_PASSWORD=votre_app_password
   ```

### Option 3 : Configurer un autre serveur SMTP
Modifiez `EmailService.java` pour utiliser votre serveur SMTP personnel

## API programmatique

### Utiliser le service directement dans votre code

```java
// 1. Créer le service
EmailService emailService = new EmailService("sender@gmail.com", "appPassword");
CriticalAlertNotifier notifier = new CriticalAlertNotifier(
    produitDAO, 
    materielDAO, 
    emailService
);

// 2. Récupérer un résumé
Map<String, Object> summary = notifier.getAlertSummary(10);
System.out.println("Stock faible: " + summary.get("low_stock_count"));

// 3. Envoyer les alertes
Map<String, Object> result = notifier.sendCriticalAlerts(
    "admin@farmtech.tn",
    "recipient@example.com",
    10
);
System.out.println("Envoyé: " + result.get("sent"));
```

## Colonnes affichées

### Onglet "Produits en Stock Faible"
| Colonne | Description |
|---------|-------------|
| ID | Identifiant du produit |
| Nom du produit | Nom du produit |
| Quantité | Stock actuel |
| Unité | Unité de mesure (kg, L, unités, etc.) |

### Onglet "Matériels en Panne"
| Colonne | Description |
|---------|-------------|
| ID | Identifiant du matériel |
| Nom du matériel | Nom de l'équipement |
| État | État actuel (panne, bon, usé, etc.) |
| Produit lié | Produit associé au matériel |

## Messages et notifications

### ✅ Succès
```
Alertes envoyées vers recipient@example.com

Stock faible: 3 produit(s)
Matériels en panne: 1
```

### ⚠️ Aucune alerte
```
Aucune alerte critique à envoyer.
```

### ❌ Erreur
```
Impossible d'envoyer les alertes: 
[Raison technique]
```

## Dépannage

### Les alertes ne s'affichent pas ?
1. Vérifiez que vous êtes connecté en tant qu'ADMIN
2. Cliquez sur le bouton "🔄 Actualiser"
3. Vérifiez la base de données (tables `produit` et `materiel`)

### L'email ne s'envoie pas ?
1. Vérifiez votre connexion internet
2. Confirmez l'adresse email destinataire
3. Vérifiez le dossier SPAM
4. Vérifiez les logs pour les erreurs

### Le bouton n'apparaît pas dans le menu ?
1. Assurez-vous d'être connecté en tant qu'ADMIN
2. Redémarrez l'application
3. Vérifiez que le fichier `dashboard_shell.fxml` contient le bouton

## Fichiers importants

```
📁 Projet
├── 📁 src/main/java
│   ├── 📁 Services
│   │   └── CriticalAlertNotifier.java          ← Service principal
│   │   └── EmailService.java                    ← Envoi d'emails
│   └── 📁 controller
│       ├── AlertController.java                 ← Contrôleur GUI
│       └── DashboardShellController.java        ← Menu principal
├── 📁 src/main/resources
│   └── 📁 view
│       ├── alerts.fxml                          ← Interface
│       └── dashboard_shell.fxml                 ← Menu (modifié)
└── 📁 docs
    ├── ALERT_SYSTEM_README.md                   ← Documentation complète
    └── SYMFONY_VS_JAVA_COMPARISON.md            ← Comparaison Symfony/Java
```

## Prochains pas

### Ajouter des alertes automatiques
Pour vérifier les alertes automatiquement chaque heure :

```java
// Dans un service Scheduled
@Scheduled(fixedRate = 3600000)  // 1 heure
public void checkAlertsAutomatically() {
    CriticalAlertNotifier notifier = new CriticalAlertNotifier(...);
    Map<String, Object> result = notifier.sendCriticalAlerts(
        "admin@farmtech.tn",
        "recipient@example.com",
        10
    );
}
```

### Ajouter des alertes sonores
```java
// Jouer un son d'alerte
AudioClip alert = new AudioClip(getClass().getResource("/sounds/alert.mp3").toExternalForm());
alert.play();
```

### Ajouter des notifications système
```java
// Afficher une notification de bureau
// Utilisez des librairies comme TrayIcon de JavaFX
```

---

**Dernière mise à jour :** 2026-05-10  
**Version :** 1.0  
**Statut :** ✅ Production Ready

