# Base de données et tests CRUD

## XAMPP (MySQL / MariaDB)

Le projet est configuré pour **XAMPP** : `localhost:3306`, utilisateur `root`, mot de passe vide (comme par défaut dans XAMPP). Aucun changement dans `Mydatabase.java` n'est nécessaire si vous n'avez pas modifié le mot de passe MySQL.

**Avant de lancer l'application :**

1. Ouvrir **XAMPP Control Panel** et démarrer **MySQL** (bouton Start).
2. Créer la base et les tables (section 1 ci-dessous). Avec XAMPP, le plus simple est d'utiliser **phpMyAdmin** : ouvrir http://localhost/phpmyadmin dans le navigateur, créer la base `3a19`, puis exécuter le script `src/main/resources/schema_probleme_diagnostique.sql` dans l'onglet SQL.

Si vous avez défini un mot de passe pour `root` dans phpMyAdmin, modifiez la ligne dans `Mydatabase.java` (voir section 2).

---

## 1. Créer la base et les tables

### Option A : MySQL en ligne de commande

1. Démarrer MySQL (service MySQL/MariaDB sur votre machine).

2. Créer la base si elle n'existe pas :
   ```bash
   mysql -u root -p
   ```
   Puis dans MySQL :
   ```sql
   CREATE DATABASE IF NOT EXISTS 3a19;
   USE 3a19;
   ```

3. Exécuter le script des tables :
   - Soit en copiant-collant le contenu de `src/main/resources/schema_probleme_diagnostique.sql` dans le client MySQL.
   - Soit depuis le terminal (en étant dans le dossier du projet) :
     ```bash
     mysql -u root -p 3a19 < src/main/resources/schema_probleme_diagnostique.sql
     ```

### Option B : MySQL Workbench (ou autre client graphique)

1. Se connecter au serveur MySQL (localhost, utilisateur `root`, mot de passe le vôtre).

2. Créer la base :
   ```sql
   CREATE DATABASE IF NOT EXISTS `3a19`;
   ```
   Puis sélectionner la base `3a19`.

3. Ouvrir le fichier `src/main/resources/schema_probleme_diagnostique.sql` et exécuter le script (Execute ou Run).

### Option C : phpMyAdmin (avec XAMPP)

1. Démarrer **MySQL** dans XAMPP Control Panel.
2. Aller sur http://localhost/phpmyadmin .
3. Onglet **SQL**, créer la base :  
   `CREATE DATABASE IF NOT EXISTS 3a19;` puis Exécuter.
4. Dans le menu de gauche, sélectionner la base **3a19**.
5. Onglet **SQL**, copier-coller tout le contenu de `src/main/resources/schema_probleme_diagnostique.sql`, puis Exécuter.

---

## 2. Vérifier la connexion

La classe `Utils.Mydatabase` utilise :

- **URL** : `jdbc:mysql://localhost:3306/3a19`
- **Utilisateur** : `root`
- **Mot de passe** : vide `""`

Si votre mot de passe MySQL n'est pas vide, modifiez la ligne dans `Mydatabase.java` :

```java
con = DriverManager.getConnection("jdbc:mysql://localhost:3306/3a19", "root", "VOTRE_MOT_DE_PASSE");
```

---

## 3. Tester l'application

1. **Compiler le projet** (Maven) :
   ```bash
   cd c:\Users\rayen\OneDrive\Bureau\Esprit\3eme\pi_me\pidev-3a19
   mvn compile
   ```

2. **Lancer la classe de test** :
   ```bash
   mvn compile exec:java
   ```
   Ou depuis votre IDE (voir section suivante).

### Lancer depuis IntelliJ IDEA (éviter « No suitable driver found »)

Le projet est configuré pour que le driver MySQL soit chargé depuis le dossier `lib/`. **À faire une seule fois** :

1. **Créer le JAR du driver dans `lib/`**  
   - Ouvrir le panneau **Maven** (View → Tool Windows → Maven).  
   - Déplier votre projet → **Lifecycle**.  
   - Double-cliquer sur **initialize** (Maven va télécharger le driver et le copier dans `lib/`).  
   - Vérifier que le fichier `lib/mysql-connector-java-8.0.33.jar` existe.

2. **Recharger le module**  
   Clic droit sur le projet ou sur `pom.xml` → **Maven** → **Reload Project**.

3. **Lancer**  
   Exécuter la classe `test.Main` (bouton Run). Le classpath du module inclut maintenant la librairie `mysql-driver` (dossier `lib/`).

Si l'erreur persiste : **Run** → **Edit Configurations…** → sélectionner la config de `Main` → vérifier que **Use classpath of module** pointe sur le module du projet (ex. `pidev-3a19`), pas sur « Project ».

3. **Résultat attendu** :
   - Message « connexion etablie » au démarrage.
   - Messages du type « probleme ajoute », « diagnostique ajoute », « probleme mis a jour », etc.
   - Affichage des listes de problèmes et de diagnostics dans la console.

Si une erreur SQL apparaît (ex. « Unknown database '3a19' »), créer la base comme en section 1. Si c'est « Table 'probleme' doesn't exist » ou « Table 'utilisateur' doesn't exist », exécuter le script `schema_probleme_diagnostique.sql` qui contient toutes les tables nécessaires.

---

## 4. Créer uniquement la table utilisateur (si elle manque)

Si vous avez déjà créé les tables `probleme` et `diagnostique` mais que la table `utilisateur` n'existe pas, vous pouvez la créer de deux façons :

### Option A : Via phpMyAdmin (XAMPP)

1. Ouvrir http://localhost/phpmyadmin
2. Sélectionner la base **3a19** dans le menu de gauche
3. Aller dans l'onglet **SQL**
4. Copier-coller et exécuter :

```sql
USE 3a19;

CREATE TABLE IF NOT EXISTS utilisateur (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);
```

### Option B : Via MySQL en ligne de commande

```bash
mysql -u root -p 3a19 < src/main/resources/create_utilisateur_table.sql
```

Ou directement dans MySQL :

```sql
USE 3a19;

CREATE TABLE IF NOT EXISTS utilisateur (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);
```
