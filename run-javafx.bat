@echo off
echo Lancement de FARMTECH...
cd /d "%~dp0"
mvn clean javafx:run
pause
