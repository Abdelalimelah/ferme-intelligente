"""
Ferme Intelligente - AI Disease Classification Microservice
FastAPI application that classifies plant diseases from drone images.
"""
import os
import shutil
import uuid
from datetime import datetime
from typing import Optional

from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

from model.classifier import classifier

app = FastAPI(
    title="Ferme Intelligente - AI Service",
    description="Plant disease classification microservice using deep learning",
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

UPLOAD_DIR = os.path.join(os.path.dirname(__file__), "uploads")
os.makedirs(UPLOAD_DIR, exist_ok=True)


# --- Request/Response Models ---

class AnalysisRequest(BaseModel):
    imageId: Optional[int] = None
    imagePath: Optional[str] = None
    parcelleId: Optional[int] = None
    latitude: Optional[float] = None
    longitude: Optional[float] = None


class AnalysisResponse(BaseModel):
    disease: str
    disease_fr: str
    confidence: float
    recommendation: str
    healthy: bool
    imageId: Optional[int] = None
    parcelleId: Optional[int] = None
    timestamp: str


class HealthResponse(BaseModel):
    status: str
    model_mode: str
    version: str


# --- Endpoints ---

@app.get("/health", response_model=HealthResponse)
def health_check():
    """Health check endpoint."""
    return HealthResponse(
        status="ok",
        model_mode="simulation" if classifier.simulation_mode else "tensorflow",
        version="1.0.0",
    )


@app.post("/analyze", response_model=AnalysisResponse)
def analyze_from_path(request: AnalysisRequest):
    """
    Analyze a plant image from file path (called by Spring Boot backend).
    If no real image exists at the path, uses simulation.
    """
    image_path = request.imagePath or f"simulated_{request.imageId}"

    # Try to use real image if it exists
    if image_path and os.path.exists(image_path):
        result = classifier.predict(image_path)
    else:
        # Simulation mode - use path hash for deterministic results
        result = classifier._simulate_prediction(image_path)

    return AnalysisResponse(
        disease=result["disease"],
        disease_fr=result["disease_fr"],
        confidence=result["confidence"],
        recommendation=result["recommendation"],
        healthy=result["healthy"],
        imageId=request.imageId,
        parcelleId=request.parcelleId,
        timestamp=datetime.now().isoformat(),
    )


@app.post("/analyze/upload", response_model=AnalysisResponse)
async def analyze_uploaded_image(
    file: UploadFile = File(...),
    parcelle_id: Optional[int] = None,
):
    """
    Analyze an uploaded plant image directly.
    Saves the image and runs classification.
    """
    if not file.content_type or not file.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="File must be an image")

    # Save uploaded file
    ext = file.filename.split(".")[-1] if "." in file.filename else "jpg"
    filename = f"{uuid.uuid4()}.{ext}"
    filepath = os.path.join(UPLOAD_DIR, filename)

    with open(filepath, "wb") as f:
        shutil.copyfileobj(file.file, f)

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
        parcelleId=parcelle_id,
        timestamp=datetime.now().isoformat(),
    )


@app.get("/diseases")
def list_diseases():
    """List all detectable diseases."""
    from model.classifier import DISEASE_CLASSES
    return {"diseases": DISEASE_CLASSES, "count": len(DISEASE_CLASSES)}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8001)
