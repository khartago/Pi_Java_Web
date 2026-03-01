@echo off
REM Script de compilation et exécution du projet Gestion Produits Premium
REM Assurez-vous que Java 17+ est installé

echo ===========================================
echo Gestion Produits Premium - Build & Run
echo ===========================================
echo.

REM Déterminer le chemin du répertoire courant
setlocal enabledelayedexpansion
set "PROJECT_DIR=%~dp0"

echo Repertoire du projet: %PROJECT_DIR%
echo.

REM Vérifier si Maven est disponible
where mvn >nul 2>nul
if errorlevel 1 (
    echo.
    echo [ERREUR] Maven n'est pas installé ou non disponible dans le PATH
    echo Veuillez installer Maven ou ajouter son chemin au PATH
    echo Voir: https://maven.apache.org/download.cgi
    echo.
    pause
    exit /b 1
)

REM Vérifier si Java est disponible
where java >nul 2>nul
if errorlevel 1 (
    echo.
    echo [ERREUR] Java n'est pas installé ou non disponible dans le PATH
    echo Veuillez installer Java 17 ou supérieur
    echo.
    pause
    exit /b 1
)

REM Afficher les versions
echo Versions détectées:
java -version 2>&1 | findstr /R "version"
echo.
mvn -version 2>&1 | findstr /R "Apache"
echo.

REM Compiler le projet
echo [1/3] Compilation du projet...
echo.
cd /d "%PROJECT_DIR%"
call mvn clean compile -DskipTests

if errorlevel 1 (
    echo.
    echo [ERREUR] La compilation a échoué
    echo Vérifiez les dépendances Maven et la syntaxe Java
    echo.
    pause
    exit /b 1
)

echo [OK] Compilation réussie
echo.

REM Exécution (optionnel - décommenter pour exécuter directement)
REM echo [2/3] Emballage du projet...
REM call mvn package -DskipTests
REM
REM if errorlevel 1 (
REM     echo [ERREUR] Le packaging a échoué
REM     pause
REM     exit /b 1
REM )
REM
REM echo [OK] Packaging réussi
REM echo.
REM
REM echo [3/3] Exécution de l'application...
REM call mvn javafx:run

echo.
echo ===========================================
echo Compilation terminée avec succès!
echo ===========================================
echo.
echo Pour exécuter l'application:
echo   mvn javafx:run
echo.
echo Pour exécuter via IntelliJ IDEA:
echo   Cliquez sur le bouton Run dans l'IDE
echo.
pause

