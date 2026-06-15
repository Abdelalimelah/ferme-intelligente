-- ============================================================
--  Ferme Intelligente — Full Database Schema + Seed Data
--  PostgreSQL 14+
--  Run once: psql -U postgres -d ferme_intelligente -f init.sql
-- ============================================================

-- ── 1. PostgreSQL ENUM types ──────────────────────────────────
CREATE TYPE role_enum           AS ENUM ('PROPRIETAIRE', 'GESTIONNAIRE', 'AGRICULTEUR');
CREATE TYPE niveau_alerte_enum  AS ENUM ('INFO', 'WARNING', 'CRITIQUE');
CREATE TYPE statut_tache_enum   AS ENUM ('A_FAIRE', 'EN_COURS', 'TERMINEE');
CREATE TYPE priorite_tache_enum AS ENUM ('BASSE', 'MOYENNE', 'HAUTE');
CREATE TYPE type_rapport_enum   AS ENUM ('RAPPORT', 'PLAINTE');
CREATE TYPE statut_rapport_enum AS ENUM ('NON_TRAITE', 'EN_COURS', 'TRAITE');

-- ── 2. Tables ─────────────────────────────────────────────────

CREATE TABLE utilisateur (
    id                  BIGSERIAL PRIMARY KEY,
    nom                 VARCHAR(100)  NOT NULL,
    prenom              VARCHAR(100)  NOT NULL,
    email               VARCHAR(255)  NOT NULL UNIQUE,
    mot_de_passe        VARCHAR(255)  NOT NULL,
    telephone           VARCHAR(20),
    role                role_enum     NOT NULL,
    statut              VARCHAR(20)   DEFAULT 'ACTIF',
    date_creation       TIMESTAMP,
    must_reset_password BOOLEAN       DEFAULT FALSE
);

CREATE TABLE ferme (
    id              BIGSERIAL PRIMARY KEY,
    nom             VARCHAR(150) NOT NULL,
    localisation    VARCHAR(255),
    surface         DOUBLE PRECISION,
    date_creation   TIMESTAMP,
    proprietaire_id BIGINT NOT NULL REFERENCES utilisateur(id)
);

CREATE TABLE parcelle (
    id              BIGSERIAL PRIMARY KEY,
    nom             VARCHAR(100) NOT NULL,
    surface         DOUBLE PRECISION,
    type_culture    VARCHAR(100),
    coordonnees_gps VARCHAR(100),
    latitude        DOUBLE PRECISION,
    longitude       DOUBLE PRECISION,
    ferme_id        BIGINT NOT NULL REFERENCES ferme(id) ON DELETE CASCADE
);

CREATE TABLE capteur (
    id                  BIGSERIAL PRIMARY KEY,
    type                VARCHAR(50)  NOT NULL,
    unite               VARCHAR(20),
    statut              VARCHAR(20)  DEFAULT 'ACTIF',
    date_installation   DATE,
    valeur_min          DOUBLE PRECISION,
    valeur_max          DOUBLE PRECISION,
    derniere_valeur     DOUBLE PRECISION,
    derniere_lecture    TIMESTAMP,
    api_key             VARCHAR(64),
    parcelle_id         BIGINT NOT NULL REFERENCES parcelle(id) ON DELETE CASCADE
);

CREATE TABLE donnee_capteur (
    id          BIGSERIAL PRIMARY KEY,
    valeur      DOUBLE PRECISION NOT NULL,
    unite       VARCHAR(20),
    date_releve TIMESTAMP        NOT NULL,
    capteur_id  BIGINT NOT NULL REFERENCES capteur(id) ON DELETE CASCADE
);

CREATE TABLE drone (
    id        BIGSERIAL PRIMARY KEY,
    modele    VARCHAR(100),
    statut    VARCHAR(20) DEFAULT 'DISPONIBLE',
    autonomie DOUBLE PRECISION
);

CREATE TABLE image_parcelle (
    id            BIGSERIAL PRIMARY KEY,
    chemin_fichier VARCHAR(500) NOT NULL,
    date_capture  TIMESTAMP    NOT NULL,
    resolution    VARCHAR(20),
    metadonnees   TEXT,
    parcelle_id   BIGINT NOT NULL REFERENCES parcelle(id),
    drone_id      BIGINT NOT NULL REFERENCES drone(id)
);

CREATE TABLE modele_ia (
    id              BIGSERIAL PRIMARY KEY,
    nom             VARCHAR(100) NOT NULL,
    version         VARCHAR(20),
    type_detection  VARCHAR(100),
    precision_val   DOUBLE PRECISION
);

CREATE TABLE resultat_analyse (
    id                BIGSERIAL PRIMARY KEY,
    maladie_detectee  VARCHAR(150),
    niveau_confiance  DOUBLE PRECISION,
    date_analyse      TIMESTAMP,
    recommandation    TEXT,
    image_id          BIGINT NOT NULL UNIQUE REFERENCES image_parcelle(id),
    modele_id         BIGINT NOT NULL REFERENCES modele_ia(id)
);

CREATE TABLE alerte (
    id            BIGSERIAL PRIMARY KEY,
    type          VARCHAR(50) NOT NULL,
    message       TEXT        NOT NULL,
    niveau        niveau_alerte_enum,
    date_creation TIMESTAMP,
    est_lue       BOOLEAN DEFAULT FALSE,
    parcelle_id   BIGINT NOT NULL REFERENCES parcelle(id),
    resultat_id   BIGINT REFERENCES resultat_analyse(id)
);

CREATE TABLE tache (
    id              BIGSERIAL PRIMARY KEY,
    titre           VARCHAR(200)     NOT NULL,
    description     TEXT,
    statut          statut_tache_enum,
    priorite        priorite_tache_enum,
    date_creation   TIMESTAMP,
    date_echeance   TIMESTAMP,
    date_terminee   TIMESTAMP,
    agriculteur_id  BIGINT NOT NULL REFERENCES utilisateur(id),
    gestionnaire_id BIGINT NOT NULL REFERENCES utilisateur(id),
    parcelle_id     BIGINT REFERENCES parcelle(id)
);

CREATE TABLE rapport (
    id            BIGSERIAL PRIMARY KEY,
    type          type_rapport_enum   NOT NULL,
    sujet         VARCHAR(255)         NOT NULL,
    contenu       TEXT                 NOT NULL,
    date_creation TIMESTAMP,
    statut        statut_rapport_enum,
    auteur_id     BIGINT NOT NULL REFERENCES utilisateur(id)
);

CREATE TABLE seuil_alerte (
    id           BIGSERIAL PRIMARY KEY,
    type_capteur VARCHAR(50)       NOT NULL,
    valeur_min   DOUBLE PRECISION  NOT NULL,
    valeur_max   DOUBLE PRECISION  NOT NULL,
    parcelle_id  BIGINT NOT NULL REFERENCES parcelle(id),
    date_creation TIMESTAMP DEFAULT NOW(),
    UNIQUE (type_capteur, parcelle_id)
);

-- ── 3. Join tables ────────────────────────────────────────────

CREATE TABLE gestionnaire_ferme (
    gestionnaire_id BIGINT NOT NULL REFERENCES utilisateur(id),
    ferme_id        BIGINT NOT NULL REFERENCES ferme(id),
    PRIMARY KEY (gestionnaire_id, ferme_id)
);

CREATE TABLE agriculteur_parcelle (
    agriculteur_id BIGINT NOT NULL REFERENCES utilisateur(id),
    parcelle_id    BIGINT NOT NULL REFERENCES parcelle(id),
    PRIMARY KEY (agriculteur_id, parcelle_id)
);

-- ── 4. Indexes ────────────────────────────────────────────────

CREATE INDEX idx_capteur_parcelle      ON capteur(parcelle_id);
CREATE INDEX idx_donnee_capteur_date   ON donnee_capteur(date_releve);
CREATE INDEX idx_alerte_parcelle       ON alerte(parcelle_id);
CREATE INDEX idx_alerte_est_lue        ON alerte(est_lue);
CREATE INDEX idx_tache_agriculteur     ON tache(agriculteur_id);
CREATE INDEX idx_tache_statut          ON tache(statut);
CREATE INDEX idx_rapport_auteur        ON rapport(auteur_id);
CREATE INDEX idx_image_parcelle        ON image_parcelle(parcelle_id);

-- ── 5. Seed data ──────────────────────────────────────────────

-- Users (password = 'password123' hashed with BCrypt)
INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, telephone, role, statut, date_creation, must_reset_password) VALUES
('Benali',   'Ahmed',   'ahmed@ferme.ma',   '$2b$10$XPrQkD/KNhQuokFwJxZ6eu6mF29u61Vx6tTnF6bwIzFcpD7SslAv6', '0661001001', 'PROPRIETAIRE', 'ACTIF', NOW(), FALSE),
('Alaoui',   'Karim',   'karim@ferme.ma',   '$2b$10$XPrQkD/KNhQuokFwJxZ6eu6mF29u61Vx6tTnF6bwIzFcpD7SslAv6', '0662002002', 'GESTIONNAIRE', 'ACTIF', NOW(), FALSE),
('Tazi',     'Youssef', 'youssef@ferme.ma', '$2b$10$XPrQkD/KNhQuokFwJxZ6eu6mF29u61Vx6tTnF6bwIzFcpD7SslAv6', '0663003003', 'AGRICULTEUR',  'ACTIF', NOW(), FALSE),
('Moussaoui','Fatima',  'fatima@ferme.ma',  '$2b$10$XPrQkD/KNhQuokFwJxZ6eu6mF29u61Vx6tTnF6bwIzFcpD7SslAv6', '0664004004', 'AGRICULTEUR',  'ACTIF', NOW(), FALSE);

-- Farm
INSERT INTO ferme (nom, localisation, surface, date_creation, proprietaire_id) VALUES
('Ferme Al-Baraka', 'Casablanca, Maroc', 45.5, NOW(), 1);

-- Parcelles
INSERT INTO parcelle (nom, surface, type_culture, coordonnees_gps, latitude, longitude, ferme_id) VALUES
('Parcelle Nord',   12.5, 'Tomates',    '33.5731,-7.5898', 33.5731, -7.5898, 1),
('Parcelle Sud',    10.2, 'Blé',        '33.5750,-7.5920', 33.5750, -7.5920, 1),
('Parcelle Est',     8.8, 'Olives',     '33.5710,-7.5880', 33.5710, -7.5880, 1),
('Parcelle Ouest',  14.0, 'Maïs',       '33.5695,-7.5940', 33.5695, -7.5940, 1);

-- Assign manager to farm
INSERT INTO gestionnaire_ferme (gestionnaire_id, ferme_id) VALUES (2, 1);

-- Assign workers to parcelles
INSERT INTO agriculteur_parcelle (agriculteur_id, parcelle_id) VALUES (3, 1), (3, 2), (4, 3), (4, 4);

-- Capteurs (3 per parcelle)
INSERT INTO capteur (type, unite, statut, date_installation, valeur_min, valeur_max, parcelle_id) VALUES
-- Parcelle 1
('Température', '°C',  'ACTIF', CURRENT_DATE,  5.0, 40.0, 1),
('Humidité',    '%',   'ACTIF', CURRENT_DATE, 20.0, 90.0, 1),
('pH',          'pH',  'ACTIF', CURRENT_DATE,  5.5,  7.5, 1),
-- Parcelle 2
('Température', '°C',  'ACTIF', CURRENT_DATE,  5.0, 40.0, 2),
('Humidité',    '%',   'ACTIF', CURRENT_DATE, 20.0, 90.0, 2),
('pH',          'pH',  'ACTIF', CURRENT_DATE,  5.5,  7.5, 2),
-- Parcelle 3
('Température', '°C',  'ACTIF', CURRENT_DATE,  5.0, 40.0, 3),
('Humidité',    '%',   'ACTIF', CURRENT_DATE, 20.0, 90.0, 3),
-- Parcelle 4
('Température', '°C',  'ACTIF', CURRENT_DATE,  5.0, 40.0, 4),
('Humidité',    '%',   'ACTIF', CURRENT_DATE, 20.0, 90.0, 4),
('pH',          'pH',  'ACTIF', CURRENT_DATE,  5.5,  7.5, 4);

-- Drone
INSERT INTO drone (modele, statut, autonomie) VALUES ('DJI Agras T40', 'DISPONIBLE', 45.0);

-- AI Model
INSERT INTO modele_ia (nom, version, type_detection, precision_val) VALUES
('PlantNet-Disease-Detector', '2.1', 'Maladie foliaire', 0.94);

-- Alert thresholds per parcelle
INSERT INTO seuil_alerte (type_capteur, valeur_min, valeur_max, parcelle_id) VALUES
('Température', 5.0, 40.0, 1), ('Humidité', 20.0, 90.0, 1), ('pH', 5.5, 7.5, 1),
('Température', 5.0, 40.0, 2), ('Humidité', 20.0, 90.0, 2), ('pH', 5.5, 7.5, 2),
('Température', 5.0, 40.0, 3), ('Humidité', 20.0, 90.0, 3),
('Température', 5.0, 40.0, 4), ('Humidité', 20.0, 90.0, 4), ('pH', 5.5, 7.5, 4);

-- Sample alerts
INSERT INTO alerte (type, message, niveau, date_creation, est_lue, parcelle_id) VALUES
('Humidité',    'Humidité critique détectée : 97.8% sur Parcelle Nord',   'CRITIQUE', NOW() - INTERVAL '2 hours',  FALSE, 1),
('Température', 'Température anormalement basse : 2.1°C sur Parcelle Sud', 'WARNING',  NOW() - INTERVAL '5 hours',  FALSE, 2),
('pH',          'pH hors norme : 8.9 détecté sur Parcelle Nord',           'WARNING',  NOW() - INTERVAL '1 day',    TRUE,  1),
('Humidité',    'Humidité très basse : 8.3% sur Parcelle Est',             'CRITIQUE', NOW() - INTERVAL '3 hours',  FALSE, 3),
('Température', 'Température élevée : 42.5°C sur Parcelle Ouest',          'CRITIQUE', NOW() - INTERVAL '30 minutes', FALSE, 4);

-- Sample tasks
INSERT INTO tache (titre, description, statut, priorite, date_creation, date_echeance, agriculteur_id, gestionnaire_id, parcelle_id) VALUES
('Irrigation Parcelle Nord',    'Activer le système d''irrigation pendant 2h avant 10h du matin.',        'A_FAIRE',  'HAUTE',   NOW(), NOW() + INTERVAL '1 day',  3, 2, 1),
('Traitement fongicide',        'Appliquer le traitement préventif contre le mildiou sur les tomates.',   'EN_COURS', 'HAUTE',   NOW(), NOW() + INTERVAL '2 days', 3, 2, 1),
('Récolte Parcelle Sud',        'Effectuer la récolte du blé dans la parcelle Sud.',                      'A_FAIRE',  'MOYENNE', NOW(), NOW() + INTERVAL '3 days', 3, 2, 2),
('Vérification capteurs',       'Inspecter et nettoyer tous les capteurs de la Parcelle Est.',             'TERMINEE', 'BASSE',   NOW() - INTERVAL '2 days', NOW(), 4, 2, 3),
('Épandage engrais',            'Épandre l''engrais azoté sur la Parcelle Ouest selon le plan.',           'A_FAIRE',  'MOYENNE', NOW(), NOW() + INTERVAL '5 days', 4, 2, 4);

-- Sample reports
INSERT INTO rapport (type, sujet, contenu, date_creation, statut, auteur_id) VALUES
('RAPPORT',  'Rapport hebdomadaire Parcelle Nord',
 'La parcelle Nord présente des signes de stress hydrique malgré une humidité sol correcte. Les capteurs indiquent des variations importantes en soirée. Recommandation : ajuster l''horaire d''irrigation.',
 NOW() - INTERVAL '1 day', 'NON_TRAITE', 2),
('RAPPORT',  'Analyse croissance blé — Parcelle Sud',
 'Croissance conforme aux prévisions saisonnières. Rendement estimé à 4.2 t/ha. Aucune maladie détectée par les drones cette semaine.',
 NOW() - INTERVAL '3 days', 'TRAITE', 2),
('PLAINTE',  'Panne capteur humidité #5',
 'Le capteur d''humidité de la Parcelle Sud (ID 5) affiche des valeurs incohérentes depuis hier soir. Nécessite une vérification ou remplacement.',
 NOW() - INTERVAL '6 hours', 'EN_COURS', 2);

-- Sync sequences to max id (prevents duplicate key errors)
SELECT setval(pg_get_serial_sequence('utilisateur',    'id'), COALESCE(MAX(id), 1)) FROM utilisateur;
SELECT setval(pg_get_serial_sequence('ferme',          'id'), COALESCE(MAX(id), 1)) FROM ferme;
SELECT setval(pg_get_serial_sequence('parcelle',       'id'), COALESCE(MAX(id), 1)) FROM parcelle;
SELECT setval(pg_get_serial_sequence('capteur',        'id'), COALESCE(MAX(id), 1)) FROM capteur;
SELECT setval(pg_get_serial_sequence('donnee_capteur', 'id'), COALESCE(MAX(id), 1)) FROM donnee_capteur;
SELECT setval(pg_get_serial_sequence('drone',          'id'), COALESCE(MAX(id), 1)) FROM drone;
SELECT setval(pg_get_serial_sequence('image_parcelle', 'id'), COALESCE(MAX(id), 1)) FROM image_parcelle;
SELECT setval(pg_get_serial_sequence('modele_ia',      'id'), COALESCE(MAX(id), 1)) FROM modele_ia;
SELECT setval(pg_get_serial_sequence('resultat_analyse','id'),COALESCE(MAX(id), 1)) FROM resultat_analyse;
SELECT setval(pg_get_serial_sequence('alerte',         'id'), COALESCE(MAX(id), 1)) FROM alerte;
SELECT setval(pg_get_serial_sequence('tache',          'id'), COALESCE(MAX(id), 1)) FROM tache;
SELECT setval(pg_get_serial_sequence('rapport',        'id'), COALESCE(MAX(id), 1)) FROM rapport;
SELECT setval(pg_get_serial_sequence('seuil_alerte',   'id'), COALESCE(MAX(id), 1)) FROM seuil_alerte;
