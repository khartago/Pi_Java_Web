#!/bin/bash
echo "Lancement de FARMTECH..."
cd "$(dirname "$0")"
mvn clean javafx:run
