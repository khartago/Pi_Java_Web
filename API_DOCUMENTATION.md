# API Documentation - Système d'Alertes FarmTech

## Vue d'ensemble

Le système d'alertes offre une API Java pour accéder aux alertes critiques. Vous pouvez l'utiliser de manière programmatique ou via l'interface GUI.

---

## 1. CriticalAlertNotifier API

### Initialisation

```java
// Création du service
ProduitDAO produitDAO = new ProduitDAO();
MaterielDAO materielDAO = new MaterielDAO();
EmailService emailService = new EmailService("sender@gmail.com", "appPassword");

CriticalAlertNotifier notifier = new CriticalAlertNotifier(
    produitDAO,
    materielDAO,
    emailService
);
```

### Méthode 1 : `getAlertSummary(int threshold)`

**Description :** Récupère un résumé des alertes sans envoyer d'email.

**Paramètres :**
- `threshold` (int) : Seuil de stock minimum (ex: 10)

**Retour :** `Map<String, Object>` contenant :
- `threshold` : Le seuil utilisé
- `low_stock_count` : Nombre de produits en stock faible
- `broken_count` : Nombre de matériels en panne
- `low_stock_products` : Liste des produits en stock faible
- `broken_materiels` : Liste des matériels en panne

**Exemple :**

```java
Map<String, Object> summary = notifier.getAlertSummary(10);

int lowStockCount = (int) summary.get("low_stock_count");
int brokenCount = (int) summary.get("broken_count");

System.out.println("Stock faible: " + lowStockCount);
System.out.println("En panne: " + brokenCount);

// Itérer sur les produits
@SuppressWarnings("unchecked")
List<Map<String, Object>> products = 
    (List<Map<String, Object>>) summary.get("low_stock_products");

for (Map<String, Object> product : products) {
    System.out.println("- " + product.get("nom") + ": " + product.get("quantite"));
}
```

### Méthode 2 : `sendCriticalAlerts(String from, String to, int threshold)`

**Description :** Envoie un email avec les alertes critiques.

**Paramètres :**
- `from` (String) : Adresse email expéditeur
- `to` (String) : Adresse email destinataire
- `threshold` (int) : Seuil de stock

**Retour :** `Map<String, Object>` contenant :
- `sent` (boolean) : Si l'email a été envoyé avec succès
- `low_stock_count` (int) : Nombre de produits en stock faible
- `panne_count` (int) : Nombre de matériels en panne
- `error` (String) : Message d'erreur (si `sent` est false)

**Exemple :**

```java
try {
    Map<String, Object> result = notifier.sendCriticalAlerts(
        "admin@farmtech.tn",
        "manager@farmtech.tn",
        15  // Seuil à 15 unités
    );
    
    if ((boolean) result.get("sent")) {
        System.out.println("✅ Email envoyé avec succès");
        System.out.println("Stock faible: " + result.get("low_stock_count"));
        System.out.println("En panne: " + result.get("panne_count"));
    } else {
        System.out.println("❌ Erreur: " + result.get("error"));
    }
} catch (Exception e) {
    System.err.println("Exception: " + e.getMessage());
}
```

### Méthode 3 : `getLowStockProducts(int threshold)`

**Description :** Récupère les produits avec un stock faible.

**Paramètres :**
- `threshold` (int) : Quantité minimum

**Retour :** `List<Produit>`

**Exemple :**

```java
List<Produit> lowStockProducts = notifier.getLowStockProducts(10);

for (Produit produit : lowStockProducts) {
    System.out.println(produit.getNom() + ": " + produit.getQuantite() + " " + produit.getUnite());
}
```

### Méthode 4 : `getBrokenMateriels()`

**Description :** Récupère tous les matériels en état de panne.

**Retour :** `List<Materiel>`

**Exemple :**

```java
List<Materiel> broken = notifier.getBrokenMateriels();

for (Materiel materiel : broken) {
    System.out.println(materiel.getNom() + " - État: " + materiel.getEtat());
}
```

---

## 2. EmailService API

### Initialisation

```java
// Avec Gmail (créer un App Password)
EmailService service = new EmailService("votreemail@gmail.com", "appPassword");

// Avec identifiants par défaut
EmailService service = new EmailService();
```

### Méthode : `sendHtmlEmail(String to, String subject, String html, String text)`

**Description :** Envoie un email HTML avec texte alternatif.

**Paramètres :**
- `to` : Adresse email destinataire
- `subject` : Sujet de l'email
- `html` : Contenu HTML
- `text` : Contenu texte (fallback)

**Exemple :**

```java
EmailService emailService = new EmailService("sender@gmail.com", "appPassword");

String htmlContent = "<html><body>" +
    "<h1>Alertes critiques</h1>" +
    "<p>3 produits en stock faible</p>" +
    "</body></html>";

String textContent = "Alertes critiques\n3 produits en stock faible";

emailService.sendHtmlEmail(
    "recipient@example.com",
    "🚨 Alertes FarmTech",
    htmlContent,
    textContent
);
```

---

## 3. Cas d'utilisation avancés

### Cas 1 : Vérifier les alertes et envoyer si critique

```java
public void checkAndNotifyIfCritical() {
    CriticalAlertNotifier notifier = createNotifier();
    Map<String, Object> summary = notifier.getAlertSummary(10);
    
    int lowStockCount = (int) summary.get("low_stock_count");
    int brokenCount = (int) summary.get("broken_count");
    
    // Envoyer seulement si > 5 alertes totales
    if (lowStockCount + brokenCount > 5) {
        notifier.sendCriticalAlerts(
            "admin@farmtech.tn",
            "alert@farmtech.tn",
            10
        );
    }
}
```

### Cas 2 : Alertes personnalisées par produit

```java
public void alerteParProduit(String nomProduit) {
    ProduitDAO dao = new ProduitDAO();
    List<Produit> allProduits = dao.getAll();
    
    for (Produit p : allProduits) {
        if (p.getNom().contains(nomProduit) && p.getQuantite() < 20) {
            System.out.println("⚠️ " + p.getNom() + " est en stock faible!");
        }
    }
}
```

### Cas 3 : Alertes groupées par type

```java
public void alertesGroupees() {
    CriticalAlertNotifier notifier = createNotifier();
    Map<String, Object> summary = notifier.getAlertSummary(10);
    
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> products = 
        (List<Map<String, Object>>) summary.get("low_stock_products");
    
    // Grouper par unité
    Map<String, List<Map<String, Object>>> grouped = new HashMap<>();
    for (Map<String, Object> p : products) {
        String unite = (String) p.get("unite");
        grouped.computeIfAbsent(unite, k -> new ArrayList<>()).add(p);
    }
    
    for (String unite : grouped.keySet()) {
        System.out.println("\n📦 " + unite + ":");
        for (Map<String, Object> p : grouped.get(unite)) {
            System.out.println("  - " + p.get("nom"));
        }
    }
}
```

### Cas 4 : Rapport mensuel

```java
public void genererRapportMensuel() {
    CriticalAlertNotifier notifier = createNotifier();
    Map<String, Object> summary = notifier.getAlertSummary(10);
    
    StringBuilder rapport = new StringBuilder();
    rapport.append("RAPPORT MENSUEL - ALERTES CRITIQUES\n");
    rapport.append("===================================\n");
    rapport.append("Date: ").append(new Date()).append("\n\n");
    
    rapport.append("📦 Stock faible: ")
        .append(summary.get("low_stock_count")).append(" produit(s)\n");
    
    rapport.append("🔧 Matériels en panne: ")
        .append(summary.get("broken_count")).append("\n\n");
    
    // Sauvegarder dans un fichier ou envoyer par email
    System.out.println(rapport.toString());
}
```

---

## 4. Intégration avec les Contrôleurs

### Dans un Contrôleur Existant

```java
public class MaterielController {
    private CriticalAlertNotifier alertNotifier;
    
    public void initialize() {
        ProduitDAO produitDAO = new ProduitDAO();
        MaterielDAO materielDAO = new MaterielDAO();
        EmailService emailService = new EmailService();
        
        alertNotifier = new CriticalAlertNotifier(
            produitDAO,
            materielDAO,
            emailService
        );
    }
    
    @FXML
    public void onMaterielUpdated(Materiel materiel) {
        if ("panne".equalsIgnoreCase(materiel.getEtat())) {
            // Envoyer une notification
            alertNotifier.sendCriticalAlerts(
                "admin@farmtech.tn",
                "responsable@farmtech.tn",
                10
            );
        }
    }
}
```

---

## 5. Réponses d'exemple

### getAlertSummary() Response

```json
{
  "threshold": 10,
  "low_stock_count": 3,
  "broken_count": 1,
  "low_stock_products": [
    {
      "id": 1,
      "nom": "Tracteur",
      "quantite": 5,
      "unite": "unités"
    },
    {
      "id": 3,
      "nom": "Poivrons doux",
      "quantite": 8,
      "unite": "kg"
    },
    {
      "id": 7,
      "nom": "Plants de tomate",
      "quantite": 10,
      "unite": "unité"
    }
  ],
  "broken_materiels": [
    {
      "id": 2,
      "nom": "Pompe irrigation",
      "etat": "panne",
      "produit": "Poivrons doux"
    }
  ]
}
```

### sendCriticalAlerts() Response (Succès)

```json
{
  "sent": true,
  "low_stock_count": 3,
  "panne_count": 1
}
```

### sendCriticalAlerts() Response (Erreur)

```json
{
  "sent": false,
  "low_stock_count": 3,
  "panne_count": 1,
  "error": "Failed to send email: Invalid recipient address"
}
```

---

## 6. Codes d'erreur

| Erreur | Cause | Solution |
|--------|-------|----------|
| "Invalid recipient address" | Email mal formé | Vérifier le format email |
| "SMTP error" | Problème de connexion | Vérifier la connexion internet |
| "Authentication failed" | Identifiants incorrects | Vérifier l'app password |
| "Email service unavailable" | Service Gmail indisponible | Réessayer plus tard |

---

## 7. Performance

- `getAlertSummary()` : ~100ms (fetch + filter en mémoire)
- `sendCriticalAlerts()` : ~2-5s (email sending)
- `getLowStockProducts()` : ~100ms
- `getBrokenMateriels()` : ~100ms

---

## 8. Sécurité

✅ **Bonnes pratiques appliquées :**
- App Passwords au lieu de mots de passe
- Variables d'environnement pour les secrets
- Validation des adresses email
- Gestion des exceptions appropriée
- Pas d'exposition des détails techniques aux utilisateurs

---

## 9. Tests

### Test unitaire

```java
public class CriticalAlertNotifierTest {
    
    @Test
    public void testGetAlertSummary() {
        CriticalAlertNotifier notifier = new CriticalAlertNotifier(
            mockProduitDAO,
            mockMaterielDAO,
            mockEmailService
        );
        
        Map<String, Object> summary = notifier.getAlertSummary(10);
        
        assertNotNull(summary);
        assertTrue(summary.containsKey("low_stock_count"));
        assertTrue(summary.containsKey("broken_count"));
    }
}
```

---

**Version API :** 1.0  
**Dernière mise à jour :** 2026-05-10

