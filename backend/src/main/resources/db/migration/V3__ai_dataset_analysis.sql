-- ── V3: AI dataset analysis support ──────────────────────────────────────────

-- Allow image_parcelle without a real drone (for dataset-sourced images)
ALTER TABLE image_parcelle
    ALTER COLUMN drone_id DROP NOT NULL;

-- Store public image URL alongside the file path
ALTER TABLE image_parcelle
    ADD COLUMN IF NOT EXISTS image_url VARCHAR(500);

-- Store French disease name in analysis results
ALTER TABLE resultat_analyse
    ADD COLUMN IF NOT EXISTS maladie_fr VARCHAR(150),
    ADD COLUMN IF NOT EXISTS class_name VARCHAR(150);

-- Update parcelle types to use plants available in the dataset
UPDATE parcelle SET type_culture = 'Pommes de terre' WHERE type_culture = 'Blé';
UPDATE parcelle SET type_culture = 'Raisins'         WHERE type_culture = 'Olives';
