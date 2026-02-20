# PiDev 3A – Projet Java / JavaFX (FARMTECH)

Application regroupant **User**, **Support** (Problèmes/Diagnostics) et **Produits/Matériels** dans un seul module.

---

## Module unique : pidev-3a19

- **Point d'entrée** : `controller.MainFX`
- **Fonctionnalités** : authentification, rôles (Admin / Fermier), support (signalements + diagnostics), gestion produits et matériels.
- **Base de données** : MySQL, schéma **3a19**. Scripts dans `pidev-3a19/src/main/resources/` (`schema_probleme_diagnostique.sql`, `schema_produit_materiel.sql`, etc.).

### Compilation et exécution

```bash
cd pidev-3a19
mvn clean compile
mvn javafx:run
```

Ou depuis la racine :

```bash
mvn -pl pidev-3a19 javafx:run
```

Voir `pidev-3a19/README_BDD_ET_TESTS.md` pour la configuration de la base.
