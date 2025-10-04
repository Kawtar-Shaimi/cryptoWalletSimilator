@echo off
echo === BUILD SIMPLE Crypto Wallet Simulator ===

echo [1/4] Nettoyage des anciens builds...
if exist build rmdir /s /q build
if exist dist rmdir /s /q dist
mkdir build
mkdir dist

echo [2/4] Copie du fichier de configuration...
mkdir build\config
copy src\config\config.properties build\config\

echo [3/4] Compilation des classes principales...
javac -encoding UTF-8 -cp "src/Lib/*" -d build src/metier/enums/*.java src/metier/model/*.java src/metier/service/*.java src/repository/*.java src/repository/jdbc/*.java src/ui/*.java src/util/*.java src/config/*.java

echo [4/4] Création du JAR avec manifest intégré...
echo Main-Class: ui.ConsoleApp > manifest.txt
echo Class-Path: Lib/postgresql-42.7.2.jar Lib/h2-2.1.214.jar >> manifest.txt
jar -cfm dist/crypto-wallet-simulator.jar manifest.txt -C build . -C src Lib
del manifest.txt

echo === BUILD RÉUSSI ===
echo JAR créé: dist/crypto-wallet-simulator.jar
echo.
echo === LANCEMENT ===
echo java -cp "dist/crypto-wallet-simulator.jar;src/Lib/*" ui.ConsoleApp