"""
Plant Disease Classifier using TensorFlow/Keras.
Uses a pre-trained MobileNetV2 fine-tuned on plant disease dataset.
Falls back to simulation mode if no model file is found.
"""
import os
import numpy as np
from PIL import Image

# Disease classes (PlantVillage dataset common classes mapped to French)
DISEASE_CLASSES = [
    {"name": "Healthy", "fr": "Sain", "recommendation": "Aucune action requise. La plante est en bonne santé."},
    {"name": "Bacterial Spot", "fr": "Tache bactérienne", "recommendation": "Appliquer un bactéricide à base de cuivre. Retirer les feuilles infectées."},
    {"name": "Early Blight", "fr": "Mildiou précoce", "recommendation": "Appliquer un fongicide à base de chlorothalonil. Améliorer la circulation d'air."},
    {"name": "Late Blight", "fr": "Mildiou tardif", "recommendation": "Traitement d'urgence au fongicide. Retirer et détruire les plantes gravement infectées."},
    {"name": "Leaf Mold", "fr": "Moisissure foliaire", "recommendation": "Réduire l'humidité. Appliquer un fongicide préventif."},
    {"name": "Powdery Mildew", "fr": "Oïdium", "recommendation": "Traitement au soufre ou au bicarbonate de potassium recommandé."},
    {"name": "Rust", "fr": "Rouille", "recommendation": "Appliquer un fongicide systémique. Retirer les feuilles infectées."},
    {"name": "Septoria Leaf Spot", "fr": "Tache septorienne", "recommendation": "Fongicide à base de mancozèbe. Éviter l'arrosage par aspersion."},
    {"name": "Yellow Leaf Curl", "fr": "Enroulement jaune", "recommendation": "Contrôler les insectes vecteurs (mouches blanches). Utiliser des variétés résistantes."},
    {"name": "Mosaic Virus", "fr": "Virus mosaïque", "recommendation": "Pas de traitement curatif. Retirer les plantes infectées. Désinfecter les outils."},
]

MODEL_PATH = os.path.join(os.path.dirname(__file__), "plant_disease_model.h5")


class PlantDiseaseClassifier:
    def __init__(self):
        self.model = None
        self.simulation_mode = True
        self._load_model()

    def _load_model(self):
        """Try to load TensorFlow model, fall back to simulation."""
        if os.path.exists(MODEL_PATH):
            try:
                import tensorflow as tf
                self.model = tf.keras.models.load_model(MODEL_PATH)
                self.simulation_mode = False
                print(f"✅ Model loaded from {MODEL_PATH}")
            except Exception as e:
                print(f"⚠️  Failed to load model: {e}. Using simulation mode.")
        else:
            print(f"ℹ️  No model file at {MODEL_PATH}. Running in simulation mode.")
            print("   To use real AI: train a model and save it as plant_disease_model.h5")

    def preprocess_image(self, image_path: str) -> np.ndarray:
        """Preprocess image for model input (224x224, normalized)."""
        img = Image.open(image_path).convert("RGB")
        img = img.resize((224, 224))
        arr = np.array(img, dtype=np.float32) / 255.0
        return np.expand_dims(arr, axis=0)

    def predict(self, image_path: str) -> dict:
        """
        Classify plant disease from image.
        Returns: {disease, disease_fr, confidence, recommendation, healthy}
        """
        if self.simulation_mode:
            return self._simulate_prediction(image_path)

        # Real model inference
        input_data = self.preprocess_image(image_path)
        predictions = self.model.predict(input_data, verbose=0)[0]
        class_idx = int(np.argmax(predictions))
        confidence = float(predictions[class_idx])

        disease = DISEASE_CLASSES[class_idx % len(DISEASE_CLASSES)]
        return {
            "disease": disease["name"],
            "disease_fr": disease["fr"],
            "confidence": confidence,
            "recommendation": disease["recommendation"],
            "healthy": disease["name"] == "Healthy",
        }

    def _simulate_prediction(self, image_path: str) -> dict:
        """Simulate prediction for demo purposes."""
        # Use hash of image path for deterministic but varied results
        hash_val = hash(image_path) % 100

        if hash_val < 30:
            # 30% chance healthy
            disease = DISEASE_CLASSES[0]
            confidence = 0.92 + (hash_val % 8) * 0.01
        else:
            # Pick a disease based on hash
            idx = (hash_val % (len(DISEASE_CLASSES) - 1)) + 1
            disease = DISEASE_CLASSES[idx]
            confidence = 0.70 + (hash_val % 25) * 0.01

        return {
            "disease": disease["name"],
            "disease_fr": disease["fr"],
            "confidence": round(confidence, 4),
            "recommendation": disease["recommendation"],
            "healthy": disease["name"] == "Healthy",
        }


# Singleton
classifier = PlantDiseaseClassifier()
