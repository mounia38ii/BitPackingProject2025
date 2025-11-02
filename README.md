# BitPackingProject2025

**Author:** Mounia AREZZOUG  
**Email:** mounia.arezzoug@etu.unice.fr  

---

## Description

Ce projet implémente plusieurs méthodes de **compression de listes d’entiers** en Java en utilisant la technique du **bit-packing**.  
L’objectif est de réduire la taille mémoire des données tout en permettant un accès rapide aux éléments compressés.

Trois variantes de compression ont été développées :

1. **BitPackingChevau** : Compression avec chevauchement de bits pour une compacité maximale.  
2. **BitPackingSansChevau** : Compression sans chevauchement, plus simple et sûre.  
3. **BitPackingOverflow** : Compression hybride avec zone de débordement pour gérer efficacement les valeurs extrêmes.

---

## Structure du projet

- `src/bitpacking/` : code source Java des classes de compression.  
- `tests/` : jeux de données pour tests et benchmarks.  
- `out/` : répertoire de compilation des fichiers `.class`.  

---

## Compilation

Depuis la racine du projet, compilez tous les fichiers Java avec la commande suivante :

javac -d out $(find src -name "*.java")

## Execution
java -cp out bitpacking.Main tests/nom_fichier 

Remplacez nom_fichier par le nom de votre fichier de test.
chevau_test[1-6]
SansChevau_test[1-6]
Overflow_test[1-6]

Le programme choisira automatiquement la méthode de compression appropriée selon le nom du fichier grâce à la factory BitPacking.create().

## Benchmark
Pour mesurer le temps de compression, décompression et comparer les tailles compressées :

java -cp out bitpacking.Benchmark