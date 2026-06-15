#!/usr/bin/env python3
"""
Drone Flight Simulator for Ferme Intelligente.

Simulates drone flights over parcelles, capturing images and
sending them to the backend + AI service for disease classification.

Workflow:
1. "Fly" to each parcelle (GPS coordinates)
2. Generate a simulated drone image
3. POST image metadata to Spring Boot backend
4. Trigger AI analysis on the captured image
5. Results auto-generate alerts if disease detected

Usage:
    python drone_simulator.py                     # Fly over all parcelles
    python drone_simulator.py --parcelle 1        # Fly over specific parcelle
    python drone_simulator.py --drone-id 1        # Use specific drone
"""
import argparse
import json
import os
import random
import time
from datetime import datetime

import requests
from PIL import Image, ImageDraw, ImageFont

BACKEND_URL = "http://localhost:8080"
AI_SERVICE_URL = "http://localhost:8001"
IMAGE_DIR = os.path.join(os.path.dirname(__file__), "uploads", "drone_captures")
os.makedirs(IMAGE_DIR, exist_ok=True)

# Parcelle GPS data (matching database)
PARCELLES = {
    1: {"nom": "Parcelle A1", "lat": 33.5731, "lon": -7.5898, "culture": "Blé"},
    2: {"nom": "Parcelle A2", "lat": 33.5750, "lon": -7.5920, "culture": "Oliviers"},
    3: {"nom": "Parcelle B1", "lat": 33.5710, "lon": -7.5880, "culture": "Tomates"},
    4: {"nom": "Parcelle B2", "lat": 33.5695, "lon": -7.5940, "culture": "Agrumes"},
}


def get_auth_token() -> str:
    """Login to get JWT token."""
    try:
        resp = requests.post(f"{BACKEND_URL}/api/auth/login", json={
            "email": "karim@ferme.ma",
            "motDePasse": "password123"
        })
        resp.raise_for_status()
        return resp.json()["token"]
    except Exception as e:
        print(f"⚠️  Auth failed: {e}. Some features may not work.")
        return ""


def generate_drone_image(parcelle_id: int, parcelle_info: dict) -> str:
    """Generate a simulated aerial/drone image with plant patterns."""
    width, height = 640, 480
    img = Image.new("RGB", (width, height))
    draw = ImageDraw.Draw(img)

    # Background: green field with variation
    for y in range(height):
        for x in range(0, width, 4):
            g = random.randint(80, 180)
            r = random.randint(30, 80)
            b = random.randint(20, 50)
            draw.rectangle([x, y, x + 3, y], fill=(r, g, b))

    # Draw crop rows
    row_spacing = random.randint(20, 40)
    for y in range(0, height, row_spacing):
        for x in range(0, width, 8):
            g = random.randint(100, 200)
            draw.rectangle([x, y, x + 6, y + 2], fill=(40, g, 30))

    # Add some "disease spots" (brown/yellow patches) randomly
    num_spots = random.randint(0, 8)
    for _ in range(num_spots):
        cx = random.randint(50, width - 50)
        cy = random.randint(50, height - 50)
        r = random.randint(10, 40)
        color = random.choice([
            (180, 140, 60),   # Yellow spot
            (120, 80, 40),    # Brown spot
            (200, 180, 100),  # Light yellow
        ])
        draw.ellipse([cx - r, cy - r, cx + r, cy + r], fill=color)

    # Add metadata overlay
    try:
        font = ImageFont.load_default()
    except:
        font = None
    timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    text = f"Drone Capture | {parcelle_info['nom']} | {timestamp}"
    draw.rectangle([0, height - 25, width, height], fill=(0, 0, 0, 180))
    draw.text((10, height - 20), text, fill=(255, 255, 255), font=font)

    # GPS overlay
    gps_text = f"GPS: {parcelle_info['lat']:.4f}, {parcelle_info['lon']:.4f}"
    draw.text((10, height - 40), gps_text, fill=(200, 200, 200), font=font)

    # Save image
    filename = f"drone_p{parcelle_id}_{datetime.now().strftime('%Y%m%d_%H%M%S')}.jpg"
    filepath = os.path.join(IMAGE_DIR, filename)
    img.save(filepath, "JPEG", quality=85)
    return filepath


def register_image(token: str, parcelle_id: int, filepath: str, drone_id: int = 1) -> dict:
    """Register the captured image in the backend."""
    headers = {"Authorization": f"Bearer {token}"} if token else {}
    payload = {
        "cheminFichier": filepath,
        "dateCapture": datetime.now().isoformat(),
        "resolution": "640x480",
        "metadonnees": json.dumps({
            "drone_id": drone_id,
            "altitude": random.randint(20, 50),
            "speed": round(random.uniform(2, 8), 1),
        }),
        "parcelleId": parcelle_id,
        "droneId": drone_id,
    }
    try:
        resp = requests.post(f"{BACKEND_URL}/api/images", json=payload, headers=headers)
        resp.raise_for_status()
        return resp.json()
    except Exception as e:
        print(f"  ⚠️  Image registration failed: {e}")
        return {"id": None}


def trigger_ai_analysis(token: str, image_id: int) -> dict:
    """Trigger AI analysis on the captured image."""
    if not image_id:
        return {"error": "No image ID"}
    headers = {"Authorization": f"Bearer {token}"} if token else {}
    try:
        resp = requests.post(
            f"{BACKEND_URL}/api/ai/analyze/{image_id}",
            headers=headers
        )
        resp.raise_for_status()
        return resp.json()
    except Exception as e:
        print(f"  ⚠️  AI analysis failed: {e}")
        return {"error": str(e)}


def simulate_flight(parcelle_id: int, parcelle_info: dict, token: str, drone_id: int):
    """Simulate a complete drone flight over one parcelle."""
    print(f"\n🛸 Flying to {parcelle_info['nom']}...")
    print(f"   GPS: {parcelle_info['lat']:.4f}, {parcelle_info['lon']:.4f}")
    print(f"   Culture: {parcelle_info['culture']}")

    # Simulate flight time
    time.sleep(1)
    print("   📸 Capturing aerial image...")

    # Generate image
    filepath = generate_drone_image(parcelle_id, parcelle_info)
    print(f"   💾 Image saved: {os.path.basename(filepath)}")

    # Register in backend
    image_data = register_image(token, parcelle_id, filepath, drone_id)
    image_id = image_data.get("id")
    if image_id:
        print(f"   📝 Registered as image #{image_id}")

        # Trigger AI analysis
        print("   🤖 Running AI disease classification...")
        result = trigger_ai_analysis(token, image_id)

        if "error" not in result:
            disease = result.get("maladieDetectee", "Unknown")
            confidence = result.get("niveauConfiance", 0)
            icon = "🌿" if disease == "Healthy" else "🦠"
            print(f"   {icon} Result: {disease} (confidence: {confidence:.0%})")
            if result.get("recommandation"):
                print(f"   💡 {result['recommandation']}")
        else:
            print(f"   ❌ Analysis error: {result['error']}")
    else:
        print("   ⚠️  Could not register image, skipping AI analysis")

    print(f"   ✅ Flight over {parcelle_info['nom']} complete")


def main():
    parser = argparse.ArgumentParser(description="Drone Flight Simulator")
    parser.add_argument("--parcelle", type=int, help="Specific parcelle ID")
    parser.add_argument("--drone-id", type=int, default=1, help="Drone ID (default: 1)")
    parser.add_argument("--repeat", type=int, default=1, help="Number of flight cycles")
    parser.add_argument("--delay", type=float, default=3, help="Delay between flights (seconds)")
    args = parser.parse_args()

    print("=" * 60)
    print("🚁 Ferme Intelligente - Drone Flight Simulator")
    print(f"   Backend: {BACKEND_URL}")
    print(f"   AI Service: {AI_SERVICE_URL}")
    print("=" * 60)

    # Authenticate
    token = get_auth_token()
    if token:
        print("🔐 Authenticated successfully")

    parcelles = {args.parcelle: PARCELLES[args.parcelle]} if args.parcelle else PARCELLES

    for cycle in range(args.repeat):
        if args.repeat > 1:
            print(f"\n{'='*40} Cycle {cycle + 1}/{args.repeat} {'='*40}")

        for pid, info in parcelles.items():
            simulate_flight(pid, info, token, args.drone_id)
            time.sleep(args.delay)

    print(f"\n{'='*60}")
    print("✅ All drone flights completed!")
    print(f"   Images saved in: {IMAGE_DIR}")
    print(f"{'='*60}")


if __name__ == "__main__":
    main()
