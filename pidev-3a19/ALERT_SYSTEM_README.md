# Système d'Alertes Critiques - FarmTech

## Overview 🚨

Le système d'alertes critiques est équivalent au système Symfony que vous aviez créé précédemment. Il permet de monitorer :
- **📦 Stock faible** : Produits dont la quantité est inférieure ou égale au seuil défini
- **🔧 Matériels en panne** : Équipements actuellement en état de panne

## Architecture

### 1. Service : `CriticalAlertNotifier.java`

Le service principal qui gère la logique des alertes.

**Méthodes principales :**

- `sendCriticalAlerts(fromEmail, toEmail, stockThreshold)` : Envoie les alertes par email
- `getLowStockProducts(threshold)` : Récupère les produits en stock faible
- `getBrokenMateriels()` : Récupère les matériels en panne
- `getAlertSummary(stockThreshold)` : Retourne un résumé sans envoyer d'email

**Exemple d'utilisation :**

```java
CriticalAlertNotifier alertNotifier = new CriticalAlertNotifier(
    produitDAO, 
    materielDAO, 
    emailService
);

// Envoyer les alertes
Map<String, Object> result = alertNotifier.sendCriticalAlerts(
    "admin@farmtech.tn",
    "recipient@example.com", 
    10  // seuil de stock
);

if ((boolean) result.get("sent")) {
    System.out.println("Alertes envoyées avec succès");
    System.out.println("Stock faible: " + result.get("low_stock_count"));
    System.out.println("Matériels en panne: " + result.get("panne_count"));
}
```

### 2. Contrôleur : `AlertController.java`

Gère l'interface graphique pour afficher et envoyer les alertes.

**Fonctionnalités :**

- Affichage des alertes dans deux onglets (Stock faible / Matériels en panne)
- Définir le seuil de stock dynamiquement via un Spinner
- Saisir l'email destinataire
- Actualiser les alertes
- Envoyer les alertes par email

### 3. Vue FXML : `alerts.fxml`

Interface utilisateur pour gérer les alertes avec :
- Résumé des alertes
- Panneau de contrôle (seuil, email, boutons)
- Deux onglets avec les tableaux des alertes

## Accès à la page des alertes

**Pour les administrateurs :**
1. Se connecter au dashboard avec un compte ADMIN
2. Cliquer sur le bouton `🚨 Alertes critiques` dans le menu latéral
3. La page s'ouvre dans l'espace principal du dashboard

## Fonctionnement

### Étape 1 : Charger les alertes
Cliquez sur **"🔄 Actualiser"** pour charger les alertes courantes.

### Étape 2 : Configurer les paramètres
- **Seuil de stock** : Indiquez la quantité minimum avant alerte
- **Email destinataire** : L'adresse où envoyer les alertes

### Étape 3 : Envoyer les alertes
Cliquez sur **"📤 Envoyer les alertes"** pour envoyer un email récapitulatif.

## Format de l'email

### Version HTML
- Header avec date et heure
- Sections détaillées pour chaque type d'alerte
- Tableaux formatés avec colonnes pertinentes
- Footer récapitulatif

### Version Texte
- Format lisible en plaintext
- Symboles emoji pour l'identification
- Liste détaillée de chaque produit/matériel

## Configuration Email

Les emails sont envoyés via Gmail avec authentification :

```java
String fromEmail = "amnafati94@gmail.com";
String appPassword = "jcdo lljy fgug omgr";  // App Password Gmail
```

**Pour utiliser vos propres identifiants :**

1. Créer une variable d'environnement `GMAIL_APP_PASSWORD`
2. Ou modifier directement les constantes dans `AlertController`

## Équivalences avec Symfony

| Symfony | Java |
|---------|------|
| `AlertController::summary()` | `CriticalAlertNotifier::getAlertSummary()` |
| `AlertController::send()` | `CriticalAlertNotifier::sendCriticalAlerts()` |
| API JSON | Méthodes renvoyant `Map<String, Object>` |
| Template Email | `buildAlertHtmlEmail()` + `buildAlertTextEmail()` |
| Dashboard | Interface JavaFX `AlertController` |

## Points importants

✅ **Seulement pour les admins** : Le bouton est visible uniquement pour les utilisateurs avec le rôle `ADMIN`

✅ **Chargement des produits** : Les matériels sont liés aux produits via `idProduit`

✅ **Gestion d'erreurs** : Les erreurs d'envoi sont affichées dans les alertes de l'application

✅ **Performance** : Les alertes sont chargées à la demande via le bouton "Actualiser"

## Fichiers créés/modifiés

- ✅ `Services/CriticalAlertNotifier.java` (créé)
- ✅ `controller/AlertController.java` (créé)
- ✅ `resources/view/alerts.fxml` (créé)
- ✅ `controller/DashboardShellController.java` (modifié - ajout du bouton)
- ✅ `resources/view/dashboard_shell.fxml` (modifié - ajout du bouton)

## Exemple d'intégration personnalisée

Si vous voulez utiliser le service directement (sans l'interface) :

```java
// Dans un autre contrôleur ou service
private CriticalAlertNotifier alertNotifier;

public void checkAlertsAndNotify() {
    EmailService emailService = new EmailService("sender@gmail.com", "appPassword");
    alertNotifier = new CriticalAlertNotifier(produitDAO, materielDAO, emailService);
    
    Map<String, Object> summary = alertNotifier.getAlertSummary(10);
    int lowStockCount = (int) summary.get("low_stock_count");
    int brokenCount = (int) summary.get("broken_count");
    
    if (lowStockCount > 0 || brokenCount > 0) {
        alertNotifier.sendCriticalAlerts("admin@farmtech.tn", "recipient@example.com", 10);
    }
}
```

---

**Système créé le :** 2026-05-10  
**Auteur :** GitHub Copilot

