# Comparaison : Symfony vs Java FarmTech - Système d'Alertes

## 📋 Résumé des implémentations

Ce document montre le mapping entre votre implémentation Symfony et la nouvelle implémentation Java JavaFX.

---

## 1️⃣ Contrôleur API Symfony → Contrôleur JavaFX

### Symfony (`AlertController`)
```php
#[Route('/api/alerts', name: 'api_alerts_')]
final class AlertController extends AbstractController
{
    #[Route('/summary', name: 'summary', methods: ['GET'])]
    public function summary(): JsonResponse { ... }
    
    #[Route('/send', name: 'send', methods: ['POST'])]
    public function send(): JsonResponse { ... }
}
```

### Java JavaFX (`AlertController`)
```java
public class AlertController {
    @FXML private void initialize() { ... }
    
    private void loadAlerts() { ... }  // Équivalent de /summary
    
    @FXML private void handleSendAlerts() { ... }  // Équivalent de /send
}
```

**Différences clés :**
- Symfony expose les endpoints via HTTP JSON
- Java utilise JavaFX pour l'interface graphique
- Les données sont chargées directement en mémoire au lieu de requêtes HTTP

---

## 2️⃣ Service de notification

### Symfony (`CriticalAlertNotifier`)
```php
class CriticalAlertNotifier {
    public function __construct(
        private readonly ProduitRepository $produitRepository,
        private readonly MaterielRepository $materielRepository,
        private readonly MailerInterface $mailer,
    ) { }
    
    public function sendCriticalAlerts(...) { ... }
}
```

### Java (`CriticalAlertNotifier`)
```java
public class CriticalAlertNotifier {
    private final ProduitDAO produitDAO;
    private final MaterielDAO materielDAO;
    private final EmailService emailService;
    
    public CriticalAlertNotifier(...) { ... }
    
    public Map<String, Object> sendCriticalAlerts(...) { ... }
}
```

**Parallèles :**
| Symfony | Java |
|---------|------|
| `ProduitRepository` | `ProduitDAO` |
| `MaterielRepository` | `MaterielDAO` |
| `MailerInterface` | `EmailService` |
| `sendCriticalAlerts()` | `sendCriticalAlerts()` |

---

## 3️⃣ Récupération des données

### Symfony
```php
$lowStockProducts = $this->produitRepository->findLowStock($this->stockThreshold);
$brokenMateriels = $this->materielRepository->findByEtatWithProduit('panne');
```

### Java
```java
List<Produit> lowStockProducts = getLowStockProducts(stockThreshold);
List<Materiel> brokenMateriels = getBrokenMateriels();
```

**Fonctionnalités :**
- ✅ Symfony : Requêtes personnalisées au niveau repository
- ✅ Java : Filtrage en mémoire via des méthodes dédiées

---

## 4️⃣ Réponse JSON vs Interface Graphique

### Symfony (JSON)
```php
return $this->json([
    'threshold' => $this->stockThreshold,
    'low_stock_count' => count($lowStockData),
    'broken_count' => count($brokenData),
    'low_stock_products' => $lowStockData,
    'broken_materiels' => $brokenData,
]);
```

### Java (Interface + Données)
```java
Map<String, Object> summary = new HashMap<>();
summary.put("threshold", stockThreshold);
summary.put("low_stock_count", lowStockData.size());
summary.put("broken_count", brokenData.size());
summary.put("low_stock_products", lowStockData);
summary.put("broken_materiels", brokenData);
// Affichage dans TableView JavaFX
```

**Différences :**
- Symfony retourne du JSON pour une API
- Java retourne une `Map` qui alimente l'interface JavaFX

---

## 5️⃣ Construction des emails

### Symfony (Twig)
```php
// Utilise un template Twig pour générer l'HTML
{% for produit in lowStockProducts %}
    <tr>
        <td>{{ produit.id }}</td>
        <td>{{ produit.nom }}</td>
        ...
    </tr>
{% endfor %}
```

### Java (StringBuilder)
```java
for (Produit p : lowStockProducts) {
    html.append("<tr>");
    html.append("<td>").append(p.getIdProduit()).append("</td>");
    html.append("<td>").append(p.getNom()).append("</td>");
    ...
    html.append("</tr>");
}
```

**Avantages/Inconvénients :**
- Symfony : Plus élégant avec Twig, mais nécessite un fichier template
- Java : Tout en code, plus simple sans dépendances supplémentaires

---

## 6️⃣ Configuration des emails

### Symfony
```php
public function __construct(
    private readonly string $alertFromEmail,    // config/services.yaml
    private readonly string $alertToEmail,
    private readonly int $stockThreshold,
) { }
```

### Java
```java
String fromEmail = "amnafati94@gmail.com";
String appPassword = System.getenv("GMAIL_APP_PASSWORD");
EmailService emailService = new EmailService(fromEmail, appPassword);
```

**Implémentation :**
- Symfony : Injection de dépendances via services.yaml
- Java : Variables d'environnement ou constantes

---

## 7️⃣ Gestion des erreurs

### Symfony
```php
try {
    $result = $this->notifier->sendCriticalAlerts(...);
} catch (\Throwable $e) {
    return $this->json([
        'sent' => false,
        'message' => 'Echec envoi: '.$e->getMessage(),
    ], JsonResponse::HTTP_INTERNAL_SERVER_ERROR);
}
```

### Java
```java
try {
    Map<String, Object> result = alertNotifier.sendCriticalAlerts(...);
    if ((boolean) result.get("sent")) {
        showInfo("Succès", "Alertes envoyées");
    }
} catch (Exception e) {
    showError("Erreur", "Erreur lors de l'envoi: " + e.getMessage());
}
```

**Approches :**
- Symfony : Retourne une réponse HTTP d'erreur
- Java : Affiche des dialogues d'erreur à l'utilisateur

---

## 8️⃣ Intégration dans l'application

### Symfony (Routes)
```php
// Routes automatiquement disponibles
GET  /api/alerts/summary
POST /api/alerts/send

// Frontend JavaScript appelle les endpoints
fetch('/api/alerts/summary').then(r => r.json())
```

### Java (Navigation)
```java
// Bouton dans le menu latéral
@FXML private void showAlertes() {
    Parent root = FXMLLoader.load(getClass().getResource("/view/alerts.fxml"));
    setContent(root);
}
```

**Flux :**
- Symfony : API REST → Frontend JavaScript
- Java : Menu → Contrôleur → FXML → Interface utilisateur

---

## 📊 Table de comparaison complet

| Aspect | Symfony | Java |
|--------|---------|------|
| **Framework** | Symfony 6.x | JavaFX |
| **Architecture** | REST API | MVC Desktop |
| **Contrôleur** | HTTP + JSON | GUI Controller |
| **Repositories** | Doctrine ORM | DAO Pattern |
| **Email** | Mailer Component | Jakarta Mail |
| **Templates** | Twig | FXML + CSS |
| **Interface** | Frontend JS | JavaFX UI |
| **Response** | JSON Response | Map<String, Object> |
| **Gestion erreurs** | HTTP Status Codes | DialogBox |
| **Configuration** | services.yaml | Environment variables |
| **Visibilité données** | API publique | Interface desktop |

---

## 🔄 Flux de données

### Symfony
```
User → Frontend → HTTP Request → AlertController::summary() 
  → CriticalAlertNotifier → Repositories (DB) 
  → JSON Response → Frontend → Display
```

### Java
```
User → AlertController.initialize() 
  → loadAlerts() 
  → CriticalAlertNotifier → DAO (DB) 
  → Map<String, Object> 
  → TableView Display
```

---

## ✨ Points forts de chaque implémentation

### Symfony ✅
- API réutilisable pour d'autres clients
- Séparation frontend/backend claire
- Templating flexible avec Twig

### Java ✅
- Interface utilisateur native et responsive
- Pas de latence réseau (appels locaux)
- Intégration directe avec la base de données
- Contrôle total du rendu UI

---

## 🚀 Prochaines étapes possibles

### Pour Symfony :
- Créer un frontend Angular/Vue.js pour consommer l'API
- Ajouter des webhooks pour les alertes critiques
- Implémenter une file d'attente (RabbitMQ) pour les emails

### Pour Java :
- Ajouter des alertes sonores/notifications système
- Implémenter un système de scheduled tasks pour vérifier les alertes automatiquement
- Ajouter des graphiques avec ChartFX pour visualiser les tendances
- Intégrer des notifications push

---

**Créé le :** 2026-05-10  
**Équivalence :** 100% des fonctionnalités Symfony reproduites en Java

