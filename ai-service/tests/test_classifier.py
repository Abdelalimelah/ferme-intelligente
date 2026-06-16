"""
Unit tests for the plant disease classifier's pure logic — no model weights
or dataset needed (both are mounted separately at runtime, not present in CI).
"""
import os
import sys

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from model.classifier import PlantDiseaseClassifier, CLASS_META, PLANT_TO_PREFIX


def test_build_result_healthy_class_is_marked_healthy():
    result = PlantDiseaseClassifier._build_result("Tomato___healthy", 0.95)

    assert result["healthy"] is True
    assert result["disease"] == "healthy"
    assert result["disease_fr"] == "Sain"
    assert result["class_name"] == "Tomato___healthy"


def test_build_result_diseased_class_is_marked_unhealthy():
    result = PlantDiseaseClassifier._build_result("Potato___Late_blight", 0.88)

    assert result["healthy"] is False
    assert result["disease"] == "Late_blight"
    assert result["disease_fr"] == CLASS_META["Potato___Late_blight"][0]
    assert "fongicide" in result["recommendation"].lower() or "traitement" in result["recommendation"].lower()


def test_build_result_unknown_class_falls_back_gracefully():
    result = PlantDiseaseClassifier._build_result("Unknown___mystery_disease", 0.5)

    assert result["healthy"] is False
    assert result["disease_fr"] == "Inconnu"


def test_pick_dataset_image_returns_none_when_dataset_missing():
    classifier = PlantDiseaseClassifier()
    # No dataset mounted in CI — must degrade gracefully, never raise.
    assert classifier.pick_dataset_image("Tomates") is None


def test_pick_dataset_image_returns_none_for_unmapped_plant_type():
    classifier = PlantDiseaseClassifier()
    assert classifier.pick_dataset_image("Plante Inconnue") is None


def test_plant_to_prefix_covers_seeded_parcelle_crop_types():
    # These are the crop types seeded in the Spring Boot DB (V1 migration) —
    # every one of them must resolve to a dataset folder prefix.
    for plant_type in ["Tomates", "Pommes de terre", "Raisins", "Maïs"]:
        assert plant_type in PLANT_TO_PREFIX
