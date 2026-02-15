# PiDev 3A – Projet Java / JavaFX

Projet regroupant trois modules d’une même application : **User**, **Problème/Diagnostique** et **Gestion Produits/Matériels**.

---

## Parties du projet

1. **User** – Gestion des utilisateurs (authentification, rôles, CRUD).
2. **Problème / Diagnostique** – Gestion des problèmes agricoles et des diagnostics (module dans `pidev-3a19/`).
3. **Produits / Matériels** – Gestion de stock (produits, matériels, CRUD, relation produit → matériels).

Les deux premiers sont dans le module **pidev-3a19** (JavaFX, FXML, MVC). Le module Produits/Matériels est à la racine (`src/`, `app.MainApp`).

---

## Module pidev-3a19 (User, Problème, Diagnostique)

- **Point d’entrée** : `gui.MainFX` (configurable dans `pidev-3a19/pom.xml`).
- **Structure** : `Entites`, `Services`, `Iservices`, `gui` (controllers), FXML dans `resources/gui/`.
- **Base de données** : scripts SQL dans `pidev-3a19/src/main/resources/` (`create_utilisateur_table.sql`, `schema_probleme_diagnostique.sql`).

Pour compiler et lancer :

```bash
cd pidev-3a19
mvn clean compile
mvn javafx:run
```

---

## Module Gestion Produits / Matériels (à la racine)

Interface JavaFX (MVC, Maven) pour la gestion de stock : CRUD produits et matériels, relation produit → matériels.

### Prérequis

- **JDK 17**
- **Maven 3.6+**
- **MySQL** (ex. XAMPP/phpMyAdmin)

### Base de données

1. Créer une base **`stockdb`** (ou adapter `URL` dans `src/main/java/model/DBConnection.java`).
2. Exécuter :

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

### Compilation et exécution

À la **racine** du dépôt :

```bash
mvn clean compile
mvn javafx:run
```

Point d’entrée : `app.MainApp`.

Structure : `src/main/java/app/`, `model/`, `controller/`, `src/main/resources/view/`, `css/style.css`.

---

## Résumé

| Module              | Emplacement        | Main class / entrée     |
|---------------------|--------------------|--------------------------|
| User + Problème/Diag| `pidev-3a19/`     | `gui.MainFX`            |
| Produits / Matériels| Racine `src/`     | `app.MainApp`           |

Pour travailler sur User et Problème/Diagnostique : aller dans `pidev-3a19` et lancer Maven depuis ce dossier. Pour Produits/Matériels : lancer Maven à la racine.
