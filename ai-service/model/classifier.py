"""
Plant Disease Classifier using PyTorch ResNet9.
Trained on the PlantVillage dataset (38 classes).
Falls back to simulation mode if model or dataset is unavailable.
"""
import os
import random
import shutil
import uuid
from pathlib import Path
from typing import Optional

import numpy as np
from PIL import Image

# ── 38 PlantVillage classes (alphabetical order = training order) ──────────
CLASS_NAMES = [
    "Apple___Apple_scab",
    "Apple___Black_rot",
    "Apple___Cedar_apple_rust",
    "Apple___healthy",
    "Blueberry___healthy",
    "Cherry_(including_sour)___Powdery_mildew",
    "Cherry_(including_sour)___healthy",
    "Corn_(maize)___Cercospora_leaf_spot Gray_leaf_spot",
    "Corn_(maize)___Common_rust_",
    "Corn_(maize)___Northern_Leaf_Blight",
    "Corn_(maize)___healthy",
    "Grape___Black_rot",
    "Grape___Esca_(Black_Measles)",
    "Grape___Leaf_blight_(Isariopsis_Leaf_Spot)",
    "Grape___healthy",
    "Orange___Haunglongbing_(Citrus_greening)",
    "Peach___Bacterial_spot",
    "Peach___healthy",
    "Pepper,_bell___Bacterial_spot",
    "Pepper,_bell___healthy",
    "Potato___Early_blight",
    "Potato___Late_blight",
    "Potato___healthy",
    "Raspberry___healthy",
    "Soybean___healthy",
    "Squash___Powdery_mildew",
    "Strawberry___Leaf_scorch",
    "Strawberry___healthy",
    "Tomato___Bacterial_spot",
    "Tomato___Early_blight",
    "Tomato___Late_blight",
    "Tomato___Leaf_Mold",
    "Tomato___Septoria_leaf_spot",
    "Tomato___Spider_mites Two-spotted_spider_mite",
    "Tomato___Target_Spot",
    "Tomato___Tomato_Yellow_Leaf_Curl_Virus",
    "Tomato___Tomato_mosaic_virus",
    "Tomato___healthy",
]

# French labels + recommendations per class
CLASS_META = {
    "Apple___Apple_scab":          ("Tavelure du pommier",            "Appliquer un fongicide à base de cuivre dès l'apparition des symptômes."),
    "Apple___Black_rot":           ("Pourriture noire du pommier",     "Retirer les fruits et feuilles infectés. Traitement fongicide préventif."),
    "Apple___Cedar_apple_rust":    ("Rouille du pommier",              "Éliminer les cèdres proches. Appliquer un fongicide systémique."),
    "Apple___healthy":             ("Sain",                            "Aucune action requise. La plante est en bonne santé."),
    "Blueberry___healthy":         ("Sain",                            "Aucune action requise. La plante est en bonne santé."),
    "Cherry_(including_sour)___Powdery_mildew": ("Oïdium du cerisier", "Traitement au soufre ou bicarbonate de potassium. Améliorer la ventilation."),
    "Cherry_(including_sour)___healthy":        ("Sain",               "Aucune action requise. La plante est en bonne santé."),
    "Corn_(maize)___Cercospora_leaf_spot Gray_leaf_spot": ("Tache grise du maïs", "Appliquer un fongicide foliaire. Rotation des cultures recommandée."),
    "Corn_(maize)___Common_rust_": ("Rouille commune du maïs",         "Utiliser des variétés résistantes. Fongicide systémique si nécessaire."),
    "Corn_(maize)___Northern_Leaf_Blight": ("Brûlure nordique du maïs","Rotation des cultures. Fongicide à base de strobilurine."),
    "Corn_(maize)___healthy":      ("Sain",                            "Aucune action requise. La plante est en bonne santé."),
    "Grape___Black_rot":           ("Pourriture noire de la vigne",    "Retirer les grappes infectées. Fongicide à base de cuivre dès le débourrement."),
    "Grape___Esca_(Black_Measles)":("Esca (rougeot parasitaire)",      "Pas de traitement curatif. Retirer et brûler les parties infectées."),
    "Grape___Leaf_blight_(Isariopsis_Leaf_Spot)": ("Brûlure foliaire de la vigne", "Fongicide préventif. Éviter l'humidité excessive."),
    "Grape___healthy":             ("Sain",                            "Aucune action requise. La plante est en bonne santé."),
    "Orange___Haunglongbing_(Citrus_greening)": ("Greening des agrumes","Pas de traitement curatif. Arracher les arbres infectés. Contrôler les psylles."),
    "Peach___Bacterial_spot":      ("Tache bactérienne du pêcher",     "Fongicide à base de cuivre. Éviter l'arrosage par aspersion."),
    "Peach___healthy":             ("Sain",                            "Aucune action requise. La plante est en bonne santé."),
    "Pepper,_bell___Bacterial_spot":("Tache bactérienne du poivron",   "Bactéricide à base de cuivre. Retirer les feuilles infectées."),
    "Pepper,_bell___healthy":      ("Sain",                            "Aucune action requise. La plante est en bonne santé."),
    "Potato___Early_blight":       ("Mildiou précoce de la pomme de terre","Fongicide à base de chlorothalonil. Améliorer la circulation d'air."),
    "Potato___Late_blight":        ("Mildiou tardif de la pomme de terre", "Traitement d'urgence. Retirer et détruire les plants gravement infectés."),
    "Potato___healthy":            ("Sain",                            "Aucune action requise. La plante est en bonne santé."),
    "Raspberry___healthy":         ("Sain",                            "Aucune action requise. La plante est en bonne santé."),
    "Soybean___healthy":           ("Sain",                            "Aucune action requise. La plante est en bonne santé."),
    "Squash___Powdery_mildew":     ("Oïdium de la courge",             "Traitement au soufre. Éviter l'humidité nocturne."),
    "Strawberry___Leaf_scorch":    ("Brûlure foliaire de la fraise",   "Retirer les feuilles infectées. Fongicide préventif en début de saison."),
    "Strawberry___healthy":        ("Sain",                            "Aucune action requise. La plante est en bonne santé."),
    "Tomato___Bacterial_spot":     ("Tache bactérienne de la tomate",  "Bactéricide à base de cuivre. Éviter l'arrosage par aspersion."),
    "Tomato___Early_blight":       ("Mildiou précoce de la tomate",    "Fongicide à base de chlorothalonil. Supprimer les feuilles du bas."),
    "Tomato___Late_blight":        ("Mildiou tardif de la tomate",     "Traitement d'urgence au fongicide. Retirer et détruire les plants infectés."),
    "Tomato___Leaf_Mold":          ("Moisissure foliaire de la tomate","Réduire l'humidité. Fongicide préventif. Améliorer la ventilation."),
    "Tomato___Septoria_leaf_spot": ("Tache septorienne de la tomate",  "Fongicide à base de mancozèbe. Éviter l'arrosage par aspersion."),
    "Tomato___Spider_mites Two-spotted_spider_mite": ("Acariens (tomate)", "Acaricide ou savon insecticide. Augmenter l'humidité ambiante."),
    "Tomato___Target_Spot":        ("Tache cible de la tomate",        "Fongicide systémique. Rotation des cultures recommandée."),
    "Tomato___Tomato_Yellow_Leaf_Curl_Virus": ("Virus de l'enroulement jaune de la tomate", "Contrôler les aleurodes vecteurs. Variétés résistantes."),
    "Tomato___Tomato_mosaic_virus":("Virus mosaïque de la tomate",     "Retirer les plants infectés. Désinfecter les outils. Pas de traitement curatif."),
    "Tomato___healthy":            ("Sain",                            "Aucune action requise. La plante est en bonne santé."),
}

# Dataset folder prefix per plant type (French name → dataset prefix)
PLANT_TO_PREFIX = {
    "Tomates":          "Tomato",
    "Maïs":             "Corn_(maize)",
    "Pommes de terre":  "Potato",
    "Raisins":          "Grape",
    "Pommes":           "Apple",
    "Fraises":          "Strawberry",
    "Poivrons":         "Pepper,_bell",
    "Pêches":           "Peach",
    "Cerises":          "Cherry_(including_sour)",
    "Blé":              "Corn_(maize)",   # fallback
    "Olives":           "Grape",          # fallback
}

MODEL_PATH = os.path.join(os.path.dirname(__file__), "plant-disease-model-complete.pth")
DATASET_PATH = os.environ.get("DATASET_PATH", "/dataset")


# ── ResNet9 architecture (must match training) ─────────────────────────────
def _conv_block(in_ch, out_ch, pool=False):
    import torch.nn as nn
    layers = [nn.Conv2d(in_ch, out_ch, kernel_size=3, padding=1),
              nn.BatchNorm2d(out_ch), nn.ReLU(inplace=True)]
    if pool:
        layers.append(nn.MaxPool2d(4))
    return nn.Sequential(*layers)


class ResNet9:
    """Placeholder so torch.load can reconstruct the model."""
    pass


def _build_resnet9(num_classes=38):
    import torch.nn as nn

    class _ResNet9(nn.Module):
        def __init__(self):
            super().__init__()
            self.conv1 = _conv_block(3, 64)
            self.conv2 = _conv_block(64, 128, pool=True)
            self.res1  = nn.Sequential(_conv_block(128, 128), _conv_block(128, 128))
            self.conv3 = _conv_block(128, 256, pool=True)
            self.conv4 = _conv_block(256, 512, pool=True)
            self.res2  = nn.Sequential(_conv_block(512, 512), _conv_block(512, 512))
            self.classifier = nn.Sequential(
                nn.MaxPool2d(4), nn.Flatten(), nn.Linear(512, num_classes))

        def forward(self, xb):
            out = self.conv1(xb)
            out = self.conv2(out)
            out = self.res1(out) + out
            out = self.conv3(out)
            out = self.conv4(out)
            out = self.res2(out) + out
            return self.classifier(out)

    return _ResNet9()


class PlantDiseaseClassifier:
    def __init__(self):
        self.model = None
        self.simulation_mode = True
        self._load_model()

    def _load_model(self):
        if not os.path.exists(MODEL_PATH):
            print(f"No model at {MODEL_PATH} — running in simulation mode.")
            return
        try:
            import torch
            import torch.nn as nn

            # Inject ResNet9 into __main__ so torch.load can unpickle it
            import sys
            import types
            fake_module = types.ModuleType("__main__")

            def ConvBlock(in_channels, out_channels, pool=False):
                layers = [nn.Conv2d(in_channels, out_channels, kernel_size=3, padding=1),
                          nn.BatchNorm2d(out_channels), nn.ReLU(inplace=True)]
                if pool:
                    layers.append(nn.MaxPool2d(4))
                return nn.Sequential(*layers)

            class ResNet9(nn.Module):
                def __init__(self, in_channels, num_diseases):
                    super().__init__()
                    self.conv1 = ConvBlock(in_channels, 64)
                    self.conv2 = ConvBlock(64, 128, pool=True)
                    self.res1  = nn.Sequential(ConvBlock(128, 128), ConvBlock(128, 128))
                    self.conv3 = ConvBlock(128, 256, pool=True)
                    self.conv4 = ConvBlock(256, 512, pool=True)
                    self.res2  = nn.Sequential(ConvBlock(512, 512), ConvBlock(512, 512))
                    self.classifier = nn.Sequential(
                        nn.MaxPool2d(4), nn.Flatten(), nn.Linear(512, num_diseases))

                def forward(self, xb):
                    out = self.conv1(xb)
                    out = self.conv2(out)
                    out = self.res1(out) + out
                    out = self.conv3(out)
                    out = self.conv4(out)
                    out = self.res2(out) + out
                    return self.classifier(out)

            fake_module.ResNet9 = ResNet9
            fake_module.ConvBlock = ConvBlock
            orig_main = sys.modules.get("__main__")
            sys.modules["__main__"] = fake_module

            self.model = torch.load(MODEL_PATH, map_location=torch.device("cpu"), weights_only=False)
            self.model.eval()

            if orig_main is not None:
                sys.modules["__main__"] = orig_main

            self.simulation_mode = False
            print(f"PyTorch model loaded from {MODEL_PATH}")
        except Exception as e:
            print(f"Failed to load model: {e} — using simulation mode.")

    def preprocess(self, image_path: str):
        import torch
        from torchvision import transforms
        transform = transforms.Compose([
            transforms.Resize((256, 256)),
            transforms.ToTensor(),
        ])
        img = Image.open(image_path).convert("RGB")
        return transform(img).unsqueeze(0)

    def predict(self, image_path: str) -> dict:
        if self.simulation_mode:
            return self._simulate(image_path)

        import torch
        with torch.no_grad():
            tensor = self.preprocess(image_path)
            logits = self.model(tensor)[0]
            probs  = torch.softmax(logits, dim=0).numpy()

        idx        = int(np.argmax(probs))
        confidence = float(probs[idx])
        class_name = CLASS_NAMES[idx]
        return self._build_result(class_name, confidence)

    def pick_dataset_image(self, plant_type: str) -> Optional[str]:
        """
        Pick a random image from the dataset matching the plant type.
        Returns None if dataset is not available.
        """
        if not os.path.isdir(DATASET_PATH):
            return None

        prefix = PLANT_TO_PREFIX.get(plant_type)
        if not prefix:
            return None

        matching_folders = [
            os.path.join(DATASET_PATH, d)
            for d in os.listdir(DATASET_PATH)
            if d.startswith(prefix) and os.path.isdir(os.path.join(DATASET_PATH, d))
        ]
        if not matching_folders:
            return None

        folder = random.choice(matching_folders)
        images = [f for f in os.listdir(folder) if f.lower().endswith((".jpg", ".jpeg", ".png"))]
        if not images:
            return None

        return os.path.join(folder, random.choice(images))

    def copy_to_uploads(self, src: str, uploads_dir: str) -> str:
        """Copy an image to the uploads/analyses dir and return the saved path."""
        dest_dir = os.path.join(uploads_dir, "analyses")
        os.makedirs(dest_dir, exist_ok=True)
        ext      = Path(src).suffix
        filename = f"{uuid.uuid4()}{ext}"
        dest     = os.path.join(dest_dir, filename)
        shutil.copy2(src, dest)
        return dest

    @staticmethod
    def _build_result(class_name: str, confidence: float) -> dict:
        meta    = CLASS_META.get(class_name, ("Inconnu", "Consulter un agronome."))
        healthy = class_name.endswith("___healthy")
        parts   = class_name.split("___")
        disease_en = parts[1] if len(parts) > 1 else class_name
        return {
            "disease":        disease_en,
            "disease_fr":     meta[0],
            "confidence":     round(confidence, 4),
            "recommendation": meta[1],
            "healthy":        healthy,
            "class_name":     class_name,
        }

    def _simulate(self, seed: str) -> dict:
        h = abs(hash(seed)) % len(CLASS_NAMES)
        class_name = CLASS_NAMES[h]
        confidence = 0.72 + (abs(hash(seed + "c")) % 25) / 100
        return self._build_result(class_name, confidence)


# Singleton
classifier = PlantDiseaseClassifier()
