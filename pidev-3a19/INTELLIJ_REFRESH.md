# IntelliJ voit une ancienne structure (gui au lieu de view)

Sur le disque, le projet a bien **`src/main/resources/view/`** (et `dashboard_shell.fxml`).
Si IntelliJ affiche encore **`gui`**, l’IDE n’est pas synchronisé avec les fichiers.

## À faire dans IntelliJ

1. **Rafraîchir les fichiers**
   - Clic droit sur le dossier **`pidev-3a19`** (ou la racine du projet) dans l’explorateur de projet  
   - **Reload from Disk** (ou **Synchronize 'pidev-3a19'**)
   - Ou : **File** → **Invalidate Caches...** → cocher **Clear file system cache and Local History** → **Invalidate and Restart**

2. **Vérifier le dossier ouvert**
   - **File** → **Open** : ouvrir le dossier **`C:\pi_me\pidev-3a19`** (pas seulement `C:\pi_me`)  
   - Ainsi IntelliJ prend bien ce module comme projet et voit `view/` et `dashboard_shell.fxml`.

3. **Nettoyer et recompiler**
   - **Build** → **Clean Project**
   - Puis **Build** → **Rebuild Project**

4. **Lancer l’app**
   - Run configuration : **Main class** = `controller.MainFX`
   - **Working directory** = module/projet `pidev-3a19`

Après ça, dans l’explorateur vous devriez voir **`view`** (plus `gui`) et **`dashboard_shell.fxml`**, et après connexion le tableau de bord avec le menu à gauche.
