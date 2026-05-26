# Projet Base de Données — Plateforme de Tournois E-sport

Projet binôme ING1-LSI1-APP-S6 — EFREI 2025/2026  
SGBD : **MySQL 8.x** | Application : **Java (JDBC + Swing)**  
**Acteurs :** CHAU Thi Phuong Nhi, CRISOSTOMO Jhermaine

---

## Table des matières

- [Présentation](#présentation)
- [Structure du projet](#structure-du-projet)
- [Prérequis](#prérequis)
- [Installation et configuration](#installation-et-configuration)
- [Compilation](#compilation)
- [Lancement](#lancement)
- [Fonctionnalités](#fonctionnalités)
- [Données d'exemple](#données-dexemple)
- [Architecture Java](#architecture-java)

---

## Présentation

Ce projet implémente une base de données et une application Java pour gérer une **plateforme de tournois e-sport**. Le système gère :

- Les jeux et leurs caractéristiques
- Les joueurs et leurs équipes (notion de roster par jeu)
- Les tournois, leurs phases et les matchs
- Les statistiques individuelles par match (kills, deaths, assists, score de performance)

---

## Structure du projet

```
Projet_BDD/
├── script_creation.sql     DDL + DML (création des tables + données d'exemple)
├── README.md               Ce fichier
├── lib/
│   └── mysql-connector-j-8.2.0.jar
└── src/
    ├── Main.java
    ├── util/
    │   └── ConnexionBDD.java
    ├── modele/
    │   ├── Joueur.java
    │   ├── Equipe.java
    │   ├── Tournoi.java
    │   ├── Phase.java
    │   ├── Match.java
    │   └── Statistique.java
    ├── dao/
    │   ├── JoueurDAO.java
    │   ├── EquipeDAO.java
    │   ├── TournoiDAO.java
    │   └── MatchDAO.java
    └── vue/
        ├── MenuPrincipal.java
        ├── MenuJoueur.java
        ├── MenuEquipe.java
        ├── MenuTournoi.java
        ├── MenuMatch.java
        ├── MenuConsultation.java
        └── gui/
            ├── MainFrame.java 
            ├── Theme.java
            ├── PanelJoueur.java
            ├── PanelEquipe.java
            ├── PanelTournoi.java
            ├── PanelMatch.java
            └── PanelConsultation.java
```

---

## Prérequis

| Outil | Version minimale |
|---|---|
| JDK | 17 |
| MySQL | 8.x |
| mysql-connector-j | 8.2.0  (déjà présent dans `lib/`) |

---

## Installation et configuration

### 1. Démarrer MySQL

**macOS / Linux**
```bash
mysql.server start
```

**Windows** (depuis le répertoire d'installation MySQL, ex. `C:\Program Files\MySQL\MySQL Server 8.0\bin\`)
```bat
net start MySQL80
```
> Le nom du service peut varier (ex. `MySQL`, `MySQL80`). Vous pouvez aussi démarrer MySQL depuis **Services Windows** ou **MySQL Workbench**.

### 2. Créer la base de données

**macOS / Linux**
```bash
mysql -u root -pVOTRE_MDP < script_creation.sql
```

**Windows**
```bat
mysql -u root -pVOTRE_MDP < script_creation.sql
```
> Remplacez `VOTRE_MDP` par votre mot de passe root MySQL (pas d'espace entre `-p` et le mot de passe).

Cela crée la base `esport` avec toutes les tables et les données d'exemple.

### 3. Configurer la connexion

Ouvrir `src/util/ConnexionBDD.java` et modifier les constantes :

```java
private static final String USER = "root";
private static final String PASS = "votre_mot_de_passe";
```

L'URL par défaut est `jdbc:mysql://localhost:3306/esport`.

---

## Compilation

Depuis la racine du projet :

**macOS / Linux**
```bash
mkdir -p out
javac -cp "lib/mysql-connector-j-8.2.0.jar" -d out \
  src/util/*.java \
  src/modele/*.java \
  src/dao/*.java \
  src/vue/*.java \
  src/vue/gui/*.java \
  src/Main.java
```

**Windows**
```bat
mkdir out
javac -cp "lib/mysql-connector-j-8.2.0.jar" -d out ^
  src/util/*.java ^
  src/modele/*.java ^
  src/dao/*.java ^
  src/vue/*.java ^
  src/vue/gui/*.java ^
  src/Main.java
```

---

## Lancement

L'application propose **deux interfaces** au choix :

| Interface | Description | Argument |
|---|---|---|
| **GUI** *(par défaut)* | Fenêtre Swing — thème bleu/rose, onglets | *(aucun)* |
| **CLI** | Menu texte dans le terminal | `--cli` ou `-c` |

### Interface graphique (GUI)

**macOS / Linux**
```bash
java -cp "out:lib/mysql-connector-j-8.2.0.jar" Main
```

**Windows**
```bat
java -cp "out;lib/mysql-connector-j-8.2.0.jar" Main
```

Une fenêtre s'ouvre avec 5 onglets : **Joueurs**, **Équipes**, **Tournois**, **Matchs**, **Consultation**.

### Interface console (CLI)

**macOS / Linux**
```bash
java -cp "out:lib/mysql-connector-j-8.2.0.jar" Main --cli
```

**Windows**
```bat
java -cp "out;lib/mysql-connector-j-8.2.0.jar" Main --cli
```

Le menu principal s'affiche dans le terminal. Saisissez un numéro (1 à 8) et validez avec Entrée.

```
========================================
 Plateforme E-sport --- Menu Principal
========================================
1. Gestion des joueurs
2. Gestion des équipes
3. Gestion des tournois
4. Saisir le résultat d'un match
5. Classement d'un tournoi
6. Statistiques d'un joueur
7. Consultation avancée
8. Quitter
========================================
```

### Dépannage rapide

| Problème | Solution |
|---|---|
| `Could not find or load main class Main` | Lancer depuis la racine du projet après compilation |
| Erreur de connexion MySQL | Vérifier que MySQL tourne et que les identifiants dans `ConnexionBDD.java` sont corrects |
| `ClassNotFoundException: com.mysql.cj.jdbc.Driver` | Vérifier que le JAR est dans `lib/` et dans le `-cp` |
| Séparateur de classpath | `:` sur macOS/Linux — `;` sur Windows |

---

## Fonctionnalités

| Onglet | Fonctionnalités |
|---|---|
| **Joueurs** | Ajouter, lister, rechercher par pseudo, modifier, supprimer |
| **Équipes** | Ajouter, lister, rechercher, voir statistiques globales, inscrire à un tournoi |
| **Tournois** | Créer un tournoi, lister, ajouter des phases |
| **Matchs** | Créer un match, saisir scores et résultat, saisir statistiques individuelles, consulter stats |
| **Consultation** | Classement d'un tournoi, Palmarès d'un joueur, Recherche joueurs, Top kills par jeu |

---

## Données d'exemple

Le script `script_creation.sql` insère un jeu de données complet couvrant toutes les tables :

| Table | Nb. lignes | Détail |
|---|---|---|
| `Jeu` | 4 | Valorant, League of Legends, CS2, Fortnite |
| `Equipe` | 4 | Team Vitality, Fnatic, G2 Esports, Karmine Corp |
| `Joueur` | 10 | ZywOo, ScreaM, kennyS, NiKo, s1mple, Rekkles… |
| `Roster` | 10 | Joueurs rattachés à leurs équipes et jeux avec rôle |
| `Tournoi` | 3 | VCT EMEA Spring 2025, LEC Spring 2025, IEM Cologne 2025 |
| `Phase` | 6 | Groupes, demi-finales, finales par tournoi |
| `Participer` | 4 | Inscriptions d'équipes aux tournois |
| `Match_Esport` | 6 | Matchs avec scores et vainqueurs |
| `Manche` | 16 | Rounds (cartes jouées) par match |
| `Statistique` | 16 | K/D/A + score de performance par joueur et par match |

---

## Architecture Java

Le code suit une architecture en couches :

| Package | Rôle | Exemple |
|---|---|---|
| `util` | Connexion JDBC centralisée (singleton) | `ConnexionBDD.java` |
| `modele` | Classes métier (POJO) | `Joueur.java`, `Equipe.java` |
| `dao` | Accès aux données via SQL | `JoueurDAO.java`, `MatchDAO.java` |
| `vue` | Interface console (CLI) | `MenuPrincipal.java` |
| `vue.gui` | Interface graphique Swing | `MainFrame.java`, `Theme.java` |

Toutes les requêtes utilisent des **`PreparedStatement`** pour éviter les injections SQL.
