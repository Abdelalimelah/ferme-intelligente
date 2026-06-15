-- Schema updates for Smart Farming features (IoT, AI, Alerts)
-- Run this against ferme_intelligente database

-- 1. Add threshold columns to capteur for smart alert generation
ALTER TABLE capteur ADD COLUMN IF NOT EXISTS valeur_min DOUBLE PRECISION;
ALTER TABLE capteur ADD COLUMN IF NOT EXISTS valeur_max DOUBLE PRECISION;
ALTER TABLE capteur ADD COLUMN IF NOT EXISTS derniere_valeur DOUBLE PRECISION;
ALTER TABLE capteur ADD COLUMN IF NOT EXISTS derniere_lecture TIMESTAMP;

-- 2. Create sensor thresholds configuration table (per parcelle overrides)
CREATE TABLE IF NOT EXISTS seuil_alerte (
    id BIGSERIAL PRIMARY KEY,
    type_capteur VARCHAR(50) NOT NULL,
    valeur_min DOUBLE PRECISION NOT NULL,
    valeur_max DOUBLE PRECISION NOT NULL,
    parcelle_id BIGINT NOT NULL REFERENCES parcelle(id),
    date_creation TIMESTAMP DEFAULT NOW(),
    UNIQUE(type_capteur, parcelle_id)
);

-- 3. Add GPS boundaries to parcelle for drone parcel detection
ALTER TABLE parcelle ADD COLUMN IF NOT EXISTS latitude DOUBLE PRECISION;
ALTER TABLE parcelle ADD COLUMN IF NOT EXISTS longitude DOUBLE PRECISION;

-- 4. Add API key field for IoT devices
ALTER TABLE capteur ADD COLUMN IF NOT EXISTS api_key VARCHAR(64);

-- 5. Insert default thresholds for existing parcelles
INSERT INTO seuil_alerte (type_capteur, valeur_min, valeur_max, parcelle_id)
SELECT 'Température', 5.0, 40.0, id FROM parcelle
ON CONFLICT (type_capteur, parcelle_id) DO NOTHING;

INSERT INTO seuil_alerte (type_capteur, valeur_min, valeur_max, parcelle_id)
SELECT 'Humidité', 20.0, 90.0, id FROM parcelle
ON CONFLICT (type_capteur, parcelle_id) DO NOTHING;

INSERT INTO seuil_alerte (type_capteur, valeur_min, valeur_max, parcelle_id)
SELECT 'pH', 5.5, 7.5, id FROM parcelle
ON CONFLICT (type_capteur, parcelle_id) DO NOTHING;

-- 6. Set default thresholds on existing capteurs
UPDATE capteur SET valeur_min = 5.0, valeur_max = 40.0 WHERE type = 'Température' AND valeur_min IS NULL;
UPDATE capteur SET valeur_min = 20.0, valeur_max = 90.0 WHERE type = 'Humidité' AND valeur_min IS NULL;
UPDATE capteur SET valeur_min = 5.5, valeur_max = 7.5 WHERE type = 'pH' AND valeur_min IS NULL;

-- 7. Update parcelle GPS coordinates for existing parcelles
UPDATE parcelle SET latitude = 33.5731, longitude = -7.5898 WHERE id = 1 AND latitude IS NULL;
UPDATE parcelle SET latitude = 33.5750, longitude = -7.5920 WHERE id = 2 AND latitude IS NULL;
UPDATE parcelle SET latitude = 33.5710, longitude = -7.5880 WHERE id = 3 AND latitude IS NULL;
UPDATE parcelle SET latitude = 33.5695, longitude = -7.5940 WHERE id = 4 AND latitude IS NULL;
