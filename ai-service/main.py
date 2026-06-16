"""
Ferme Intelligente - AI Disease Classification Microservice
FastAPI application that classifies plant diseases from images.
"""
import os
import uuid
from datetime import datetime
from typing import Optional

import aiofiles
from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from pydantic import BaseModel
from prometheus_fastapi_instrumentator import Instrumentator

from model.classifier import classifier

app = FastAPI(
    title="Ferme Intelligente - AI Service",
    description="Plant disease classification microservice using deep learning",
    version="2.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

# Exposes /metrics for Prometheus scraping
Instrumentator().instrument(app).expose(app)

UPLOAD_DIR = os.path.join(os.path.dirname(__file__), "uploads")
os.makedirs(UPLOAD_DIR, exist_ok=True)

# Serve uploaded images as static files — accessible via /ai/uploads/ through nginx
app.mount("/uploads", StaticFiles(directory=UPLOAD_DIR), name="uploads")


# ── Request / Response models ────────────────────────────────────────────────

class AnalysisRequest(BaseModel):
    imageId: Optional[int] = None
    imagePath: Optional[str] = None
    parcelleId: Optional[int] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None


class DatasetAnalysisRequest(BaseModel):
    plantType: str        # e.g. "Tomates", "Maïs"
    parcelleId: int


class AnalysisResponse(BaseModel):
    disease: str
    disease_fr: str
    confidence: float
    recommendation: str
    healthy: bool
    class_name: Optional[str] = None
    imageId: Optional[int] = None
    parcelleId: Optional[int] = None
    imageSavedPath: Optional[str] = None  # absolute path inside container
    imageUrl: Optional[str] = None        # public URL via nginx /ai/uploads/...
    timestamp: str


class HealthResponse(BaseModel):
    status: str
    model_mode: str
    dataset_available: bool
    version: str


# ── Endpoints ─────────────────────────────────────────────────────────────────

@app.get("/health", response_model=HealthResponse)
def health_check():
    import os
    dataset_path = os.environ.get("DATASET_PATH", "/dataset")
    return HealthResponse(
        status="ok",
        model_mode="pytorch" if not classifier.simulation_mode else "simulation",
        dataset_available=os.path.isdir(dataset_path),
        version="2.0.0",
    )


@app.post("/analyze", response_model=AnalysisResponse)
def analyze_from_path(request: AnalysisRequest):
    """
    Analyze a plant image from file path (called by Spring Boot backend).
    Falls back to simulation if the image path doesn't exist.
    """
    image_path = request.imagePath or f"simulated_{request.imageId}"

    if image_path and os.path.exists(image_path):
        result = classifier.predict(image_path)
    else:
        result = classifier._simulate(image_path)

    return AnalysisResponse(
        disease=result["disease"],
        disease_fr=result["disease_fr"],
        confidence=result["confidence"],
        recommendation=result["recommendation"],
        healthy=result["healthy"],
        class_name=result.get("class_name"),
        imageId=request.imageId,
        parcelleId=request.parcelleId,
        timestamp=datetime.now().isoformat(),
    )


@app.post("/analyze/dataset", response_model=AnalysisResponse)
def analyze_from_dataset(request: DatasetAnalysisRequest):
    """
    Pick a random image from the dataset matching the plant type,
    run the model, return the result with the image URL.
    """
    src_image = classifier.pick_dataset_image(request.plantType)

    if src_image is None:
        # Dataset not mounted or plant type not in dataset — simulate
        result = classifier._simulate(f"{request.plantType}_{request.parcelleId}")
        return AnalysisResponse(
            disease=result["disease"],
            disease_fr=result["disease_fr"],
            confidence=result["confidence"],
            recommendation=result["recommendation"],
            healthy=result["healthy"],
            class_name=result.get("class_name"),
            parcelleId=request.parcelleId,
            imageSavedPath=None,
            imageUrl=None,
            timestamp=datetime.now().isoformat(),
        )

    # Copy image to uploads/analyses/ so it can be served as static content
    saved_path = classifier.copy_to_uploads(src_image, UPLOAD_DIR)
    rel_path   = os.path.relpath(saved_path, UPLOAD_DIR)          # analyses/xxx.jpg
    image_url  = f"/ai/uploads/{rel_path}"                        # routed via nginx

    # Run model on the picked image
    result = classifier.predict(src_image)

    return AnalysisResponse(
        disease=result["disease"],
        disease_fr=result["disease_fr"],
        confidence=result["confidence"],
        recommendation=result["recommendation"],
        healthy=result["healthy"],
        class_name=result.get("class_name"),
        parcelleId=request.parcelleId,
        imageSavedPath=saved_path,
        imageUrl=image_url,
        timestamp=datetime.now().isoformat(),
    )


@app.post("/analyze/upload", response_model=AnalysisResponse)
async def analyze_uploaded_image(
    file: UploadFile = File(...),
    parcelle_id: Optional[int] = None,
):
    """Analyze an image uploaded directly by the user."""
    if not file.content_type or not file.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="File must be an image")

    ext      = file.filename.split(".")[-1] if "." in file.filename else "jpg"
    filename = f"{uuid.uuid4()}.{ext}"
    filepath = os.path.join(UPLOAD_DIR, filename)

    async with aiofiles.open(filepath, "wb") as f:
        await f.write(await file.read())

    try:
        result = classifier.predict(filepath)
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Analysis failed: {str(e)}")

    return AnalysisResponse(
        disease=result["disease"],
        disease_fr=result["disease_fr"],
        confidence=result["confidence"],
        recommendation=result["recommendation"],
        healthy=result["healthy"],
        class_name=result.get("class_name"),
        parcelleId=parcelle_id,
        imageUrl=f"/ai/uploads/{filename}",
        timestamp=datetime.now().isoformat(),
    )


@app.get("/diseases")
def list_diseases():
    from model.classifier import CLASS_NAMES, CLASS_META
    return {
        "diseases": [
            {"class": c, "fr": CLASS_META.get(c, ("?", ""))[0]}
            for c in CLASS_NAMES
        ],
        "count": len(CLASS_NAMES),
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8001)
