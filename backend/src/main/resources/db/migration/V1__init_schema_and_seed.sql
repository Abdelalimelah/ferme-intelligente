-- ============================================================
--  V1 — Full schema (enums, tables, indexes) + seed data
--  Applied once on fresh databases.
--  Existing databases: use flyway.baseline-on-migrate=true
--                       flyway.baseline-version=1
-- ============================================================

-- ── Enum types ───────────────────────────────────────────────
DO $$ BEGIN
  CREATE TYPE role_enum           AS ENUM ('PROPRIETAIRE','GESTIONNAIRE','AGRICULTEUR');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TYPE niveau_alerte_enum  AS ENUM ('INFO','WARNING','CRITIQUE');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TYPE statut_tache_enum   AS ENUM ('A_FAIRE','EN_COURS','TERMINEE');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TYPE priorite_tache_enum AS ENUM ('BASSE','MOYENNE','HAUTE');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TYPE type_rapport_enum   AS ENUM ('RAPPORT','PLAINTE');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;
DO $$ BEGIN
  CREATE TYPE statut_rapport_enum AS ENUM ('NON_TRAITE','EN_COURS','TRAITE');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- ── Tables ───────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS utilisateur (
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

CREATE TABLE IF NOT EXISTS ferme (
    id              BIGSERIAL PRIMARY KEY,
    nom             VARCHAR(150) NOT NULL,
    localisation    VARCHAR(255),
    surface         DOUBLE PRECISION,
    date_creation   TIMESTAMP,
    proprietaire_id BIGINT NOT NULL REFERENCES utilisateur(id)
);

CREATE TABLE IF NOT EXISTS parcelle (
    id              BIGSERIAL PRIMARY KEY,
    nom             VARCHAR(100) NOT NULL,
    surface         DOUBLE PRECISION,
    type_culture    VARCHAR(100),
    coordonnees_gps VARCHAR(100),
    latitude        DOUBLE PRECISION,
    longitude       DOUBLE PRECISION,
    ferme_id        BIGINT NOT NULL REFERENCES ferme(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS capteur (
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

CREATE TABLE IF NOT EXISTS donnee_capteur (
    id          BIGSERIAL PRIMARY KEY,
    valeur      DOUBLE PRECISION NOT NULL,
    unite       VARCHAR(20),
    date_releve TIMESTAMP        NOT NULL,
    capteur_id  BIGINT NOT NULL REFERENCES capteur(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS drone (
    id        BIGSERIAL PRIMARY KEY,
    modele    VARCHAR(100),
    statut    VARCHAR(20) DEFAULT 'DISPONIBLE',
    autonomie DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS image_parcelle (
    id             BIGSERIAL PRIMARY KEY,
    chemin_fichier VARCHAR(500) NOT NULL,
    date_capture   TIMESTAMP    NOT NULL,
    resolution     VARCHAR(20),
    metadonnees    TEXT,
    parcelle_id    BIGINT NOT NULL REFERENCES parcelle(id),
    drone_id       BIGINT NOT NULL REFERENCES drone(id)
);

CREATE TABLE IF NOT EXISTS modele_ia (
    id              BIGSERIAL PRIMARY KEY,
    nom             VARCHAR(100) NOT NULL,
    version         VARCHAR(20),
    type_detection  VARCHAR(100),
    precision_val   DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS resultat_analyse (
    id                BIGSERIAL PRIMARY KEY,
    maladie_detectee  VARCHAR(150),
    niveau_confiance  DOUBLE PRECISION,
    date_analyse      TIMESTAMP,
    recommandation    TEXT,
    image_id          BIGINT NOT NULL UNIQUE REFERENCES image_parcelle(id),
    modele_id         BIGINT NOT NULL REFERENCES modele_ia(id)
);

CREATE TABLE IF NOT EXISTS alerte (
    id            BIGSERIAL PRIMARY KEY,
    type          VARCHAR(50) NOT NULL,
    message       TEXT        NOT NULL,
    niveau        niveau_alerte_enum,
    date_creation TIMESTAMP,
    est_lue       BOOLEAN DEFAULT FALSE,
    parcelle_id   BIGINT NOT NULL REFERENCES parcelle(id),
    resultat_id   BIGINT REFERENCES resultat_analyse(id)
);

CREATE TABLE IF NOT EXISTS tache (
    id              BIGSERIAL PRIMARY KEY,
    titre           VARCHAR(200)        NOT NULL,
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

CREATE TABLE IF NOT EXISTS rapport (
    id            BIGSERIAL PRIMARY KEY,
    type          type_rapport_enum    NOT NULL,
    sujet         VARCHAR(255)          NOT NULL,
    contenu       TEXT                  NOT NULL,
    date_creation TIMESTAMP,
    statut        statut_rapport_enum,
    auteur_id     BIGINT NOT NULL REFERENCES utilisateur(id)
);

CREATE TABLE IF NOT EXISTS seuil_alerte (
    id            BIGSERIAL PRIMARY KEY,
    type_capteur  VARCHAR(50)      NOT NULL,
    valeur_min    DOUBLE PRECISION NOT NULL,
    valeur_max    DOUBLE PRECISION NOT NULL,
    parcelle_id   BIGINT NOT NULL REFERENCES parcelle(id),
    date_creation TIMESTAMP DEFAULT NOW(),
    UNIQUE (type_capteur, parcelle_id)
);

-- ── Join tables ──────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS gestionnaire_ferme (
    gestionnaire_id BIGINT NOT NULL REFERENCES utilisateur(id),
    ferme_id        BIGINT NOT NULL REFERENCES ferme(id),
    PRIMARY KEY (gestionnaire_id, ferme_id)
);

CREATE TABLE IF NOT EXISTS agriculteur_parcelle (
    agriculteur_id BIGINT NOT NULL REFERENCES utilisateur(id),
    parcelle_id    BIGINT NOT NULL REFERENCES parcelle(id),
    PRIMARY KEY (agriculteur_id, parcelle_id)
);

-- ── Indexes ──────────────────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_capteur_parcelle    ON capteur(parcelle_id);
CREATE INDEX IF NOT EXISTS idx_donnee_date         ON donnee_capteur(date_releve);
CREATE INDEX IF NOT EXISTS idx_alerte_parcelle     ON alerte(parcelle_id);
CREATE INDEX IF NOT EXISTS idx_alerte_est_lue      ON alerte(est_lue);
CREATE INDEX IF NOT EXISTS idx_tache_agriculteur   ON tache(agriculteur_id);
CREATE INDEX IF NOT EXISTS idx_tache_statut        ON tache(statut);
CREATE INDEX IF NOT EXISTS idx_rapport_auteur      ON rapport(auteur_id);
CREATE INDEX IF NOT EXISTS idx_image_parcelle      ON image_parcelle(parcelle_id);

-- ── Seed data (password = 'password123' BCrypt) ──────────────
INSERT INTO utilisateur (nom,prenom,email,mot_de_passe,telephone,role,statut,date_creation,must_reset_password)
SELECT 'Benali','Ahmed','ahmed@ferme.ma','$2b$10$XPrQkD/KNhQuokFwJxZ6eu6mF29u61Vx6tTnF6bwIzFcpD7SslAv6','0661001001','PROPRIETAIRE','ACTIF',NOW(),FALSE
WHERE NOT EXISTS (SELECT 1 FROM utilisateur WHERE email='ahmed@ferme.ma');

INSERT INTO utilisateur (nom,prenom,email,mot_de_passe,telephone,role,statut,date_creation,must_reset_password)
SELECT 'Alaoui','Karim','karim@ferme.ma','$2b$10$XPrQkD/KNhQuokFwJxZ6eu6mF29u61Vx6tTnF6bwIzFcpD7SslAv6','0662002002','GESTIONNAIRE','ACTIF',NOW(),FALSE
WHERE NOT EXISTS (SELECT 1 FROM utilisateur WHERE email='karim@ferme.ma');

INSERT INTO utilisateur (nom,prenom,email,mot_de_passe,telephone,role,statut,date_creation,must_reset_password)
SELECT 'Tazi','Youssef','youssef@ferme.ma','$2b$10$XPrQkD/KNhQuokFwJxZ6eu6mF29u61Vx6tTnF6bwIzFcpD7SslAv6','0663003003','AGRICULTEUR','ACTIF',NOW(),FALSE
WHERE NOT EXISTS (SELECT 1 FROM utilisateur WHERE email='youssef@ferme.ma');

INSERT INTO utilisateur (nom,prenom,email,mot_de_passe,telephone,role,statut,date_creation,must_reset_password)
SELECT 'Moussaoui','Fatima','fatima@ferme.ma','$2b$10$XPrQkD/KNhQuokFwJxZ6eu6mF29u61Vx6tTnF6bwIzFcpD7SslAv6','0664004004','AGRICULTEUR','ACTIF',NOW(),FALSE
WHERE NOT EXISTS (SELECT 1 FROM utilisateur WHERE email='fatima@ferme.ma');

INSERT INTO ferme (nom,localisation,surface,date_creation,proprietaire_id)
SELECT 'Ferme Al-Baraka','Casablanca, Maroc',45.5,NOW(),
       (SELECT id FROM utilisateur WHERE email='ahmed@ferme.ma')
WHERE NOT EXISTS (SELECT 1 FROM ferme WHERE nom='Ferme Al-Baraka');

INSERT INTO parcelle (nom,surface,type_culture,coordonnees_gps,latitude,longitude,ferme_id)
SELECT 'Parcelle Nord',12.5,'Tomates','33.5731,-7.5898',33.5731,-7.5898,(SELECT id FROM ferme WHERE nom='Ferme Al-Baraka')
WHERE NOT EXISTS (SELECT 1 FROM parcelle WHERE nom='Parcelle Nord');
INSERT INTO parcelle (nom,surface,type_culture,coordonnees_gps,latitude,longitude,ferme_id)
SELECT 'Parcelle Sud',10.2,'Blé','33.5750,-7.5920',33.5750,-7.5920,(SELECT id FROM ferme WHERE nom='Ferme Al-Baraka')
WHERE NOT EXISTS (SELECT 1 FROM parcelle WHERE nom='Parcelle Sud');
INSERT INTO parcelle (nom,surface,type_culture,coordonnees_gps,latitude,longitude,ferme_id)
SELECT 'Parcelle Est',8.8,'Olives','33.5710,-7.5880',33.5710,-7.5880,(SELECT id FROM ferme WHERE nom='Ferme Al-Baraka')
WHERE NOT EXISTS (SELECT 1 FROM parcelle WHERE nom='Parcelle Est');
INSERT INTO parcelle (nom,surface,type_culture,coordonnees_gps,latitude,longitude,ferme_id)
SELECT 'Parcelle Ouest',14.0,'Maïs','33.5695,-7.5940',33.5695,-7.5940,(SELECT id FROM ferme WHERE nom='Ferme Al-Baraka')
WHERE NOT EXISTS (SELECT 1 FROM parcelle WHERE nom='Parcelle Ouest');

INSERT INTO gestionnaire_ferme (gestionnaire_id,ferme_id)
SELECT (SELECT id FROM utilisateur WHERE email='karim@ferme.ma'),
       (SELECT id FROM ferme WHERE nom='Ferme Al-Baraka')
WHERE NOT EXISTS (SELECT 1 FROM gestionnaire_ferme);

INSERT INTO agriculteur_parcelle (agriculteur_id,parcelle_id)
SELECT (SELECT id FROM utilisateur WHERE email='youssef@ferme.ma'),(SELECT id FROM parcelle WHERE nom='Parcelle Nord')
WHERE NOT EXISTS (SELECT 1 FROM agriculteur_parcelle WHERE agriculteur_id=(SELECT id FROM utilisateur WHERE email='youssef@ferme.ma') AND parcelle_id=(SELECT id FROM parcelle WHERE nom='Parcelle Nord'));
INSERT INTO agriculteur_parcelle (agriculteur_id,parcelle_id)
SELECT (SELECT id FROM utilisateur WHERE email='youssef@ferme.ma'),(SELECT id FROM parcelle WHERE nom='Parcelle Sud')
WHERE NOT EXISTS (SELECT 1 FROM agriculteur_parcelle WHERE agriculteur_id=(SELECT id FROM utilisateur WHERE email='youssef@ferme.ma') AND parcelle_id=(SELECT id FROM parcelle WHERE nom='Parcelle Sud'));
INSERT INTO agriculteur_parcelle (agriculteur_id,parcelle_id)
SELECT (SELECT id FROM utilisateur WHERE email='fatima@ferme.ma'),(SELECT id FROM parcelle WHERE nom='Parcelle Est')
WHERE NOT EXISTS (SELECT 1 FROM agriculteur_parcelle WHERE agriculteur_id=(SELECT id FROM utilisateur WHERE email='fatima@ferme.ma') AND parcelle_id=(SELECT id FROM parcelle WHERE nom='Parcelle Est'));
INSERT INTO agriculteur_parcelle (agriculteur_id,parcelle_id)
SELECT (SELECT id FROM utilisateur WHERE email='fatima@ferme.ma'),(SELECT id FROM parcelle WHERE nom='Parcelle Ouest')
WHERE NOT EXISTS (SELECT 1 FROM agriculteur_parcelle WHERE agriculteur_id=(SELECT id FROM utilisateur WHERE email='fatima@ferme.ma') AND parcelle_id=(SELECT id FROM parcelle WHERE nom='Parcelle Ouest'));

INSERT INTO capteur (type,unite,statut,date_installation,valeur_min,valeur_max,parcelle_id)
SELECT 'Température','°C','ACTIF',CURRENT_DATE,5.0,40.0,(SELECT id FROM parcelle WHERE nom='Parcelle Nord') WHERE NOT EXISTS (SELECT 1 FROM capteur WHERE type='Température' AND parcelle_id=(SELECT id FROM parcelle WHERE nom='Parcelle Nord'));
INSERT INTO capteur (type,unite,statut,date_installation,valeur_min,valeur_max,parcelle_id)
SELECT 'Humidité','%','ACTIF',CURRENT_DATE,20.0,90.0,(SELECT id FROM parcelle WHERE nom='Parcelle Nord') WHERE NOT EXISTS (SELECT 1 FROM capteur WHERE type='Humidité' AND parcelle_id=(SELECT id FROM parcelle WHERE nom='Parcelle Nord'));
INSERT INTO capteur (type,unite,statut,date_installation,valeur_min,valeur_max,parcelle_id)
SELECT 'pH','pH','ACTIF',CURRENT_DATE,5.5,7.5,(SELECT id FROM parcelle WHERE nom='Parcelle Nord') WHERE NOT EXISTS (SELECT 1 FROM capteur WHERE type='pH' AND parcelle_id=(SELECT id FROM parcelle WHERE nom='Parcelle Nord'));
INSERT INTO capteur (type,unite,statut,date_installation,valeur_min,valeur_max,parcelle_id)
SELECT 'Température','°C','ACTIF',CURRENT_DATE,5.0,40.0,(SELECT id FROM parcelle WHERE nom='Parcelle Sud') WHERE NOT EXISTS (SELECT 1 FROM capteur WHERE type='Température' AND parcelle_id=(SELECT id FROM parcelle WHERE nom='Parcelle Sud'));
INSERT INTO capteur (type,unite,statut,date_installation,valeur_min,valeur_max,parcelle_id)
SELECT 'Humidité','%','ACTIF',CURRENT_DATE,20.0,90.0,(SELECT id FROM parcelle WHERE nom='Parcelle Sud') WHERE NOT EXISTS (SELECT 1 FROM capteur WHERE type='Humidité' AND parcelle_id=(SELECT id FROM parcelle WHERE nom='Parcelle Sud'));
INSERT INTO capteur (type,unite,statut,date_installation,valeur_min,valeur_max,parcelle_id)
SELECT 'pH','pH','ACTIF',CURRENT_DATE,5.5,7.5,(SELECT id FROM parcelle WHERE nom='Parcelle Sud') WHERE NOT EXISTS (SELECT 1 FROM capteur WHERE type='pH' AND parcelle_id=(SELECT id FROM parcelle WHERE nom='Parcelle Sud'));
INSERT INTO capteur (type,unite,statut,date_installation,valeur_min,valeur_max,parcelle_id)
SELECT 'Température','°C','ACTIF',CURRENT_DATE,5.0,40.0,(SELECT id FROM parcelle WHERE nom='Parcelle Est') WHERE NOT EXISTS (SELECT 1 FROM capteur WHERE type='Température' AND parcelle_id=(SELECT id FROM parcelle WHERE nom='Parcelle Est'));
INSERT INTO capteur (type,unite,statut,date_installation,valeur_min,valeur_max,parcelle_id)
SELECT 'Humidité','%','ACTIF',CURRENT_DATE,20.0,90.0,(SELECT id FROM parcelle WHERE nom='Parcelle Est') WHERE NOT EXISTS (SELECT 1 FROM capteur WHERE type='Humidité' AND parcelle_id=(SELECT id FROM parcelle WHERE nom='Parcelle Est'));
INSERT INTO capteur (type,unite,statut,date_installation,valeur_min,valeur_max,parcelle_id)
SELECT 'Température','°C','ACTIF',CURRENT_DATE,5.0,40.0,(SELECT id FROM parcelle WHERE nom='Parcelle Ouest') WHERE NOT EXISTS (SELECT 1 FROM capteur WHERE type='Température' AND parcelle_id=(SELECT id FROM parcelle WHERE nom='Parcelle Ouest'));
INSERT INTO capteur (type,unite,statut,date_installation,valeur_min,valeur_max,parcelle_id)
SELECT 'Humidité','%','ACTIF',CURRENT_DATE,20.0,90.0,(SELECT id FROM parcelle WHERE nom='Parcelle Ouest') WHERE NOT EXISTS (SELECT 1 FROM capteur WHERE type='Humidité' AND parcelle_id=(SELECT id FROM parcelle WHERE nom='Parcelle Ouest'));
INSERT INTO capteur (type,unite,statut,date_installation,valeur_min,valeur_max,parcelle_id)
SELECT 'pH','pH','ACTIF',CURRENT_DATE,5.5,7.5,(SELECT id FROM parcelle WHERE nom='Parcelle Ouest') WHERE NOT EXISTS (SELECT 1 FROM capteur WHERE type='pH' AND parcelle_id=(SELECT id FROM parcelle WHERE nom='Parcelle Ouest'));

INSERT INTO drone (modele,statut,autonomie) SELECT 'DJI Agras T40','DISPONIBLE',45.0 WHERE NOT EXISTS (SELECT 1 FROM drone);
INSERT INTO modele_ia (nom,version,type_detection,precision_val) SELECT 'PlantNet-Disease-Detector','2.1','Maladie foliaire',0.94 WHERE NOT EXISTS (SELECT 1 FROM modele_ia);

INSERT INTO seuil_alerte (type_capteur,valeur_min,valeur_max,parcelle_id)
SELECT s.t,s.mn,s.mx,p.id FROM (VALUES('Température',5.0,40.0),('Humidité',20.0,90.0),('pH',5.5,7.5)) AS s(t,mn,mx), parcelle p
ON CONFLICT (type_capteur,parcelle_id) DO NOTHING;

INSERT INTO alerte (type,message,niveau,date_creation,est_lue,parcelle_id)
SELECT 'Humidité','Humidité critique : 97.8% sur Parcelle Nord','CRITIQUE',NOW()-INTERVAL '2 hours',FALSE,(SELECT id FROM parcelle WHERE nom='Parcelle Nord')
WHERE NOT EXISTS (SELECT 1 FROM alerte WHERE type='Humidité' LIMIT 1);
INSERT INTO alerte (type,message,niveau,date_creation,est_lue,parcelle_id)
SELECT 'Température','Température basse : 2.1°C sur Parcelle Sud','WARNING',NOW()-INTERVAL '5 hours',FALSE,(SELECT id FROM parcelle WHERE nom='Parcelle Sud')
WHERE NOT EXISTS (SELECT 1 FROM alerte WHERE type='Température' LIMIT 1);

INSERT INTO tache (titre,description,statut,priorite,date_creation,date_echeance,agriculteur_id,gestionnaire_id,parcelle_id)
SELECT 'Irrigation Parcelle Nord','Activer le système d''irrigation pendant 2h.','A_FAIRE','HAUTE',NOW(),NOW()+INTERVAL '1 day',
  (SELECT id FROM utilisateur WHERE email='youssef@ferme.ma'),(SELECT id FROM utilisateur WHERE email='karim@ferme.ma'),(SELECT id FROM parcelle WHERE nom='Parcelle Nord')
WHERE NOT EXISTS (SELECT 1 FROM tache LIMIT 1);
INSERT INTO tache (titre,description,statut,priorite,date_creation,date_echeance,agriculteur_id,gestionnaire_id,parcelle_id)
SELECT 'Traitement fongicide','Appliquer traitement contre le mildiou.','EN_COURS','HAUTE',NOW(),NOW()+INTERVAL '2 days',
  (SELECT id FROM utilisateur WHERE email='youssef@ferme.ma'),(SELECT id FROM utilisateur WHERE email='karim@ferme.ma'),(SELECT id FROM parcelle WHERE nom='Parcelle Nord')
WHERE (SELECT COUNT(*) FROM tache)<2;

INSERT INTO rapport (type,sujet,contenu,date_creation,statut,auteur_id)
SELECT 'RAPPORT','Rapport hebdomadaire Parcelle Nord','Signes de stress hydrique. Recommandation : ajuster l''irrigation.',NOW()-INTERVAL '1 day','NON_TRAITE',(SELECT id FROM utilisateur WHERE email='karim@ferme.ma')
WHERE NOT EXISTS (SELECT 1 FROM rapport LIMIT 1);

-- Sync sequences
SELECT setval(pg_get_serial_sequence('utilisateur','id'),   COALESCE(MAX(id),1)) FROM utilisateur;
SELECT setval(pg_get_serial_sequence('ferme','id'),         COALESCE(MAX(id),1)) FROM ferme;
SELECT setval(pg_get_serial_sequence('parcelle','id'),      COALESCE(MAX(id),1)) FROM parcelle;
SELECT setval(pg_get_serial_sequence('capteur','id'),       COALESCE(MAX(id),1)) FROM capteur;
SELECT setval(pg_get_serial_sequence('donnee_capteur','id'),COALESCE(MAX(id),1)) FROM donnee_capteur;
SELECT setval(pg_get_serial_sequence('drone','id'),         COALESCE(MAX(id),1)) FROM drone;
SELECT setval(pg_get_serial_sequence('image_parcelle','id'),COALESCE(MAX(id),1)) FROM image_parcelle;
SELECT setval(pg_get_serial_sequence('modele_ia','id'),     COALESCE(MAX(id),1)) FROM modele_ia;
SELECT setval(pg_get_serial_sequence('resultat_analyse','id'),COALESCE(MAX(id),1)) FROM resultat_analyse;
SELECT setval(pg_get_serial_sequence('alerte','id'),        COALESCE(MAX(id),1)) FROM alerte;
SELECT setval(pg_get_serial_sequence('tache','id'),         COALESCE(MAX(id),1)) FROM tache;
SELECT setval(pg_get_serial_sequence('rapport','id'),       COALESCE(MAX(id),1)) FROM rapport;
SELECT setval(pg_get_serial_sequence('seuil_alerte','id'),  COALESCE(MAX(id),1)) FROM seuil_alerte;
