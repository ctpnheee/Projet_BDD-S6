-- Projet BDD EFREI — Plateforme e-sport
-- MySQL 8.x / InnoDB
-- Ce fichier : création de la base + DDL + échantillon minimal (1 équipe, 2 joueurs)

CREATE DATABASE IF NOT EXISTS esport CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE esport;

-- Suppression des tables (ordre : dépendances d'abord)
DROP TABLE IF EXISTS Statistique;
DROP TABLE IF EXISTS Manche;
DROP TABLE IF EXISTS Match_Esport;
DROP TABLE IF EXISTS Participer;
DROP TABLE IF EXISTS Phase;
DROP TABLE IF EXISTS Tournoi;
DROP TABLE IF EXISTS Roster;
DROP TABLE IF EXISTS Joueur;
DROP TABLE IF EXISTS Equipe;
DROP TABLE IF EXISTS Jeu;

-- DDL

CREATE TABLE Jeu (
    id_jeu INT AUTO_INCREMENT,
    nom VARCHAR(80) NOT NULL,
    genre ENUM('FPS','MOBA','Sport','Battle Royale','Autre') NOT NULL,
    editeur VARCHAR(80),
    annee_sortie YEAR,
    PRIMARY KEY (id_jeu),
    CONSTRAINT uk_jeu_nom UNIQUE (nom)
) ENGINE=InnoDB;

CREATE TABLE Joueur (
    id_joueur INT AUTO_INCREMENT,
    pseudo VARCHAR(50) NOT NULL,
    nom VARCHAR(50) NOT NULL,
    prenom VARCHAR(50) NOT NULL,
    date_naissance DATE NOT NULL,
    nationalite VARCHAR(50) NOT NULL,
    niveau VARCHAR(50) NOT NULL COMMENT 'Elo, rang, etc.',
    PRIMARY KEY (id_joueur),
    CONSTRAINT uk_joueur_pseudo UNIQUE (pseudo)
) ENGINE=InnoDB;

CREATE TABLE Equipe (
    id_equipe INT AUTO_INCREMENT,
    nom VARCHAR(80) NOT NULL,
    chemin_logo VARCHAR(255),
    date_creation DATE NOT NULL,
    pays VARCHAR(50) NOT NULL,
    PRIMARY KEY (id_equipe),
    CONSTRAINT uk_equipe_nom UNIQUE (nom)
) ENGINE=InnoDB;

-- Roster : un joueur est dans une équipe pour un jeu précis (dates + rôle)
CREATE TABLE Roster (
    id_roster INT AUTO_INCREMENT,
    id_joueur INT NOT NULL,
    id_equipe INT NOT NULL,
    id_jeu INT NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NULL COMMENT 'NULL = toujours dans l''effectif',
    role VARCHAR(40) NOT NULL,
    PRIMARY KEY (id_roster),
    CONSTRAINT fk_roster_joueur FOREIGN KEY (id_joueur)
        REFERENCES Joueur(id_joueur) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_roster_equipe FOREIGN KEY (id_equipe)
        REFERENCES Equipe(id_equipe) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_roster_jeu FOREIGN KEY (id_jeu)
        REFERENCES Jeu(id_jeu) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE Tournoi (
    id_tournoi INT AUTO_INCREMENT,
    nom VARCHAR(120) NOT NULL,
    date_debut DATE NOT NULL,
    date_fin DATE NOT NULL,
    type ENUM('en_ligne','LAN') NOT NULL,
    dotation DECIMAL(12,2) NOT NULL DEFAULT 0,
    statut ENUM('a_venir','en_cours','termine') NOT NULL DEFAULT 'a_venir',
    id_jeu INT NOT NULL,
    PRIMARY KEY (id_tournoi),
    CONSTRAINT fk_tournoi_jeu FOREIGN KEY (id_jeu)
        REFERENCES Jeu(id_jeu) ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE Phase (
    id_phase INT AUTO_INCREMENT,
    libelle VARCHAR(80) NOT NULL,
    numero_ordre INT NOT NULL DEFAULT 1,
    id_tournoi INT NOT NULL,
    PRIMARY KEY (id_phase),
    CONSTRAINT fk_phase_tournoi FOREIGN KEY (id_tournoi)
        REFERENCES Tournoi(id_tournoi) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- Inscription d'une équipe à un tournoi
CREATE TABLE Participer (
    id_equipe INT NOT NULL,
    id_tournoi INT NOT NULL,
    date_inscription DATE DEFAULT (CURRENT_DATE),
    PRIMARY KEY (id_equipe, id_tournoi),
    CONSTRAINT fk_part_equipe FOREIGN KEY (id_equipe)
        REFERENCES Equipe(id_equipe) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_part_tournoi FOREIGN KEY (id_tournoi)
        REFERENCES Tournoi(id_tournoi) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE Match_Esport (
    id_match INT AUTO_INCREMENT,
    date_match DATETIME NOT NULL,
    score_equipe1 INT NOT NULL DEFAULT 0 COMMENT 'manches gagnées équipe 1',
    score_equipe2 INT NOT NULL DEFAULT 0 COMMENT 'manches gagnées équipe 2',
    id_phase INT NOT NULL,
    id_equipe1 INT NOT NULL,
    id_equipe2 INT NOT NULL,
    id_equipe_vainqueur INT NULL COMMENT 'doit être équipe1 ou équipe2 si renseigné',
    PRIMARY KEY (id_match),
    CONSTRAINT fk_match_phase FOREIGN KEY (id_phase)
        REFERENCES Phase(id_phase) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_match_e1 FOREIGN KEY (id_equipe1)
        REFERENCES Equipe(id_equipe) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_match_e2 FOREIGN KEY (id_equipe2)
        REFERENCES Equipe(id_equipe) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_match_vainqueur FOREIGN KEY (id_equipe_vainqueur)
        REFERENCES Equipe(id_equipe) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

-- Une manche / partie dans un match (Bo3, etc.)
CREATE TABLE Manche (
    id_manche INT AUTO_INCREMENT,
    nom_carte VARCHAR(80) NOT NULL,
    numero_manche INT NOT NULL,
    id_match INT NOT NULL,
    id_equipe_vainqueur INT NULL,
    PRIMARY KEY (id_manche),
    CONSTRAINT fk_manche_match FOREIGN KEY (id_match)
        REFERENCES Match_Esport(id_match) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_manche_vainqueur FOREIGN KEY (id_equipe_vainqueur)
        REFERENCES Equipe(id_equipe) ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE Statistique (
    id_joueur INT NOT NULL,
    id_match INT NOT NULL,
    nb_kills INT NOT NULL DEFAULT 0,
    nb_deaths INT NOT NULL DEFAULT 0,
    nb_assists INT NOT NULL DEFAULT 0,
    score_performance DECIMAL(8,2) NOT NULL DEFAULT 0,
    PRIMARY KEY (id_joueur, id_match),
    CONSTRAINT fk_stat_joueur FOREIGN KEY (id_joueur)
        REFERENCES Joueur(id_joueur) ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_stat_match FOREIGN KEY (id_match)
        REFERENCES Match_Esport(id_match) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- DML minimal : 1 jeu, 1 équipe "duo", 2 joueurs, 2 lignes de roster

-- Jeux
INSERT INTO Jeu (nom, genre, editeur, annee_sortie) VALUES
('Valorant',        'FPS',          'Riot Games',      2020),
('League of Legends','MOBA',        'Riot Games',      2009),
('CS2',             'FPS',          'Valve',           2023),
('Fortnite',        'Battle Royale','Epic Games',      2017);

-- Equipes
INSERT INTO Equipe (nom, chemin_logo, date_creation, pays) VALUES
('Team Vitality',   'logos/vitality.png',   '2013-06-10', 'France'),
('Fnatic',          'logos/fnatic.png',     '2004-07-23', 'Royaume-Uni'),
('G2 Esports',      'logos/g2.png',         '2014-08-11', 'Espagne'),
('Karmine Corp',    'logos/kc.png',         '2020-04-01', 'France');

-- Joueurs (10 joueurs)
INSERT INTO Joueur (pseudo, nom, prenom, date_naissance, nationalite, niveau) VALUES
('ZywOo',      'Mathieu',  'Herbaut',   '2000-11-09', 'France',        'Radiant'),
('ScreaM',     'Adil',     'Benrlitom', '1994-03-02', 'Belgique',      'Immortal 3'),
('kennyS',     'Kenny',    'Schrub',    '1995-05-19', 'France',        'Diamond 1'),
('AceMin',     'Martin',   'Léa',       '2003-04-12', 'France',        'Immortal 2'),
('FlexBot',    'Bernard',  'Tom',       '2002-11-03', 'France',        'Diamond 3'),
('Rekkles',    'Martin',   'Larsson',   '1996-09-20', 'Suède',         'Challenger'),
('Caps',       'Rasmus',   'Winther',   '1999-12-18', 'Danemark',      'Grandmaster'),
('Jankos',     'Marcin',   'Jankowski', '1995-02-21', 'Pologne',       'Master'),
('NiKo',       'Nikola',   'Kovač',     '1997-02-16', 'Bosnie',        'Global Elite'),
('s1mple',     'Aleksandr','Kostyliev', '1997-10-02', 'Ukraine',       'Global Elite');

-- Rosters
INSERT INTO Roster (id_joueur, id_equipe, id_jeu, date_debut, date_fin, role) VALUES
-- Team Vitality — Valorant
(1, 1, 1, '2023-01-10', NULL,         'Duelist'),
(2, 1, 1, '2023-01-10', NULL,         'Sentinel'),
(3, 1, 1, '2023-03-01', NULL,         'Controller'),
-- Fnatic — League of Legends
(6, 2, 2, '2022-11-15', NULL,         'ADC'),
(7, 3, 2, '2022-11-15', NULL,         'Mid'),
(8, 3, 2, '2022-11-15', NULL,         'Jungle'),
-- G2 — CS2
(9, 3, 3, '2023-09-01', NULL,         'Rifler'),
(10,3, 3, '2023-09-01', NULL,         'AWPer'),
-- Karmine Corp — Valorant
(4, 4, 1, '2025-01-20', NULL,         'Duelist'),
(5, 4, 1, '2025-01-20', NULL,         'Initiator');

-- Tournois
INSERT INTO Tournoi (nom, date_debut, date_fin, type, dotation, statut, id_jeu) VALUES
('VCT EMEA Spring 2025',  '2025-02-01', '2025-04-15', 'en_ligne', 200000.00, 'termine',  1),
('LEC Spring 2025',       '2025-01-20', '2025-04-20', 'LAN',      500000.00, 'termine',  2),
('IEM Cologne 2025',      '2025-07-01', '2025-07-13', 'LAN',      1000000.00,'a_venir',  3);

-- Phases
INSERT INTO Phase (libelle, numero_ordre, id_tournoi) VALUES
('Phase de groupes', 1, 1),
('Demi-finales',     2, 1),
('Finale',           3, 1),
('Phase de groupes', 1, 2),
('Playoffs',         2, 2),
('Finale',           3, 2);

-- Participations
INSERT INTO Participer (id_equipe, id_tournoi, date_inscription) VALUES
(1, 1, '2025-01-05'),
(4, 1, '2025-01-06'),
(2, 2, '2024-12-10'),
(3, 2, '2024-12-11');

-- Matchs
INSERT INTO Match_Esport (date_match, score_equipe1, score_equipe2, id_phase, id_equipe1, id_equipe2, id_equipe_vainqueur) VALUES
('2025-02-10 18:00:00', 2, 0, 1, 1, 4, 1),
('2025-02-12 18:00:00', 1, 2, 1, 4, 1, 4),
('2025-03-20 20:00:00', 2, 1, 2, 1, 4, 1),
('2025-04-15 17:00:00', 2, 0, 3, 1, 4, 1),
('2025-01-25 16:00:00', 1, 2, 4, 2, 3, 3),
('2025-04-18 17:00:00', 2, 1, 5, 3, 2, 3);

-- Manches
INSERT INTO Manche (nom_carte, numero_manche, id_match, id_equipe_vainqueur) VALUES
('Ascent',  1, 1, 1), ('Bind',    2, 1, 1),
('Haven',   1, 2, 4), ('Icebox',  2, 2, 1), ('Pearl',   3, 2, 4),
('Lotus',   1, 3, 1), ('Split',   2, 3, 4), ('Sunset',  3, 3, 1),
('Breeze',  1, 4, 1), ('Fracture',2, 4, 1),
('Inferno', 1, 5, 3), ('Mirage',  2, 5, 2), ('Nuke',    3, 5, 3),
('Dust2',   1, 6, 3), ('Overpass',2, 6, 2), ('Anubis',  3, 6, 3);

-- Statistiques
INSERT INTO Statistique (id_joueur, id_match, nb_kills, nb_deaths, nb_assists, score_performance) VALUES
(1, 1, 22, 14, 5,  1.57),
(2, 1, 18, 16, 8,  1.13),
(4, 1, 12, 19, 6,  0.63),
(5, 1, 10, 20, 4,  0.50),
(1, 2, 19, 17, 6,  1.12),
(2, 2, 15, 18, 9,  0.83),
(4, 2, 21, 13, 7,  1.62),
(5, 2, 17, 15, 5,  1.13),
(1, 3, 25, 12, 4,  2.08),
(2, 3, 20, 14, 6,  1.43),
(4, 3, 16, 18, 5,  0.89),
(5, 3, 14, 19, 3,  0.74),
(1, 4, 28, 10, 3,  2.80),
(2, 4, 22, 13, 7,  1.69),
(4, 4, 13, 21, 4,  0.62),
(5, 4, 11, 22, 2,  0.50);

