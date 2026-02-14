# Gestion des Produits et Matériels – Version Premium (Maven)

Cette version du projet est une refonte complète de l’application de gestion de stock
en JavaFX. Elle propose une interface graphique modernisée et ergonomique,
construite autour du pattern MVC et d’une architecture Maven. Tout le code est
compatible avec **JDK 17 (17.0.14)** et utilise JavaFX **17.0.18** via le plugin
Maven. La base de données MySQL se configure facilement via XAMPP/phpMyAdmin.

## ✨ Points clés

- **Design professionnel** : la feuille de style reprend les couleurs de Bootstrap pour
  offrir une interface moderne (barre d’en‑tête sombre, boutons colorés,
  tableaux aérés). Chaque vue est organisée à l’aide de `BorderPane` pour une
  disposition claire et cohérente.
- **CRUD complet** pour les produits et leurs matériels (ajout, lecture,
  modification et suppression) avec confirmation avant suppression.
- **Relation produit → matériels** : un produit peut posséder plusieurs
  matériels. La suppression d’un produit entraîne celle de ses matériels via
  la contrainte `ON DELETE CASCADE`.
- **Contrôles de saisie** : toutes les saisies sont vérifiées (champs obligatoires,
  nombres positifs, dates optionnelles). Les erreurs sont signalées à l’utilisateur.
- **Structure Maven** : les sources Java sont dans `src/main/java` et les
  ressources FXML/CSS dans `src/main/resources`. Le fichier `pom.xml`
  gère les dépendances (`javafx-controls`, `javafx-fxml`, MySQL Connector/J) et
  configure le plugin `javafx-maven-plugin`.

## 🛠 Pré‑requis

- **JDK 17** (la version 17.0.14 est utilisée dans ce projet). Assurez‑vous que
  `JAVA_HOME` pointe vers ce JDK.
- **Maven 3.6** ou supérieur (installé et accessible via la commande `mvn`).
- **MySQL** (via XAMPP/phpMyAdmin) pour héberger la base de données.

## ⚙️ Installation de la base de données

1. Démarrer MySQL via XAMPP et ouvrir phpMyAdmin.
2. Créer une base nommée **`stockdb`** (ou un autre nom de votre choix ; pensez
   alors à modifier la constante `URL` dans `model/DBConnection`).
3. Exécuter les requêtes SQL suivantes :

   ```sql
   CREATE TABLE produit (
     idProduit INT AUTO_INCREMENT PRIMARY KEY,
     nom        VARCHAR(100) NOT NULL,
     quantite   INT         NOT NULL,
     unite      VARCHAR(50) NOT NULL,
     dateExpiration DATE
   );

   CREATE TABLE materiel (
     idMateriel INT AUTO_INCREMENT PRIMARY KEY,
     nom       VARCHAR(100) NOT NULL,
     etat      VARCHAR(50)  NOT NULL,
     dateAchat DATE,
     cout      DOUBLE NOT NULL,
     idProduit INT NOT NULL,
     FOREIGN KEY (idProduit) REFERENCES produit(idProduit) ON DELETE CASCADE
   );
   ```

4. Adapter éventuellement les constantes `URL`, `USER` et `PASSWORD` dans
   `src/main/java/model/DBConnection.java` afin qu’elles correspondent à votre
   configuration MySQL (hôte, port, utilisateur, mot de passe).

## ▶️ Compilation et exécution

Ouvrez un terminal dans le dossier `gestion-produits-premium` et lancez :

```bash
mvn clean compile   # compile le projet
mvn javafx:run      # exécute l’application avec le plugin JavaFX
```

La première commande télécharge automatiquement les dépendances si elles
n’existent pas. La seconde démarre l’application en utilisant la classe
principale `app.MainApp`. Le plugin se charge de configurer le module‑path
et d’inclure les modules JavaFX requis.

### Utilisation dans un IDE

* **IntelliJ IDEA** : Ouvrez le dossier du projet comme un projet Maven. Le
  fichier `pom.xml` sera détecté et les dépendances automatiquement
  téléchargées. Vous pouvez exécuter la tâche `javafx:run` dans la vue Maven
  ou créer une configuration d’exécution qui lance `app.MainApp`.
* **Eclipse** ou **NetBeans** : Importez le projet en tant que projet Maven.
  Les dépendances seront résolues et vous pourrez lancer l’application via
  Maven ou en configurant une exécution de la classe `app.MainApp` avec les
  modules JavaFX.

## 🗂 Structure du projet

```
gestion-produits-premium/
├── pom.xml                  # Dépendances JavaFX & MySQL + configuration plugin
├── README.md                # Guide et instructions (ce fichier)
└── src/
    └── main/
        ├── java/
        │   ├── app/MainApp.java       # Point d’entrée de l’application JavaFX
        │   ├── model/…               # Entités (Produit, Materiel) et DAO
        │   └── controller/…          # Logique métier & contrôleurs MVC
        └── resources/
            ├── view/…               # Vues FXML (listes et formulaires)
            └── css/style.css        # Feuille de style moderne
```

## 📌 Notes finales

Cette version premium met l’accent sur l’ergonomie et la lisibilité tout en
restant simple à maintenir. Grâce à l’architecture Maven, aucune
configuration complexe n’est nécessaire : il suffit de disposer d’un JDK 17 et
d’exécuter les commandes Maven indiquées. Les vues sont prêtes pour être
personnalisées davantage (ajout d’un logo, changement de couleurs, etc.).

N’hésitez pas à évoluer cette base : ajout d’un champ de recherche, export CSV
ou PDF, authentification utilisateur, ou encore déploiement sous forme
d’installateur.
