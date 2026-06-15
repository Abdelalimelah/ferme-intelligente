# Ferme Intelligente - Smart Farming Features

## Architecture Overview

```
┌─────────────────────┐     ┌──────────────────┐     ┌─────────────────────┐
│  React Frontend     │────▶│  Spring Boot API  │────▶│  PostgreSQL DB      │
│  (Port 5173)        │◀────│  (Port 8080)      │◀────│  (Port 5432)        │
└─────────────────────┘     └──────┬───────────┘     └─────────────────────┘
                                   │
┌─────────────────────┐           │         ┌─────────────────────┐
│  Arduino/IoT        │───────────┘         │  Python AI Service  │
│  Simulator          │  POST /api/iot/data  │  (Port 8001)        │
└─────────────────────┘                     └─────────────────────┘
                                                     ▲
┌─────────────────────┐                             │
│  Drone Simulator    │─────────────────────────────┘
└─────────────────────┘     POST /analyze
```

## Features Implemented

### 1. Parcel-Based Smart Dashboard
- **Per-parcel detail view** with real-time sensor data, alerts, diseases, and drone images
- **Live sensor gauges** with threshold indicators and alert state
- **Time-series charts** (Recharts) for sensor history
- **Auto-refresh polling** every 5 seconds

### 2. Arduino/IoT Sensor Integration
- **REST endpoint** `POST /api/iot/data` (no JWT required for devices)
- **Auto-threshold alerts**: values outside configured min/max automatically generate alerts
- **Live value tracking**: each sensor stores `derniere_valeur` and `derniere_lecture`
- **Batch ingestion**: `POST /api/iot/data/batch` for multiple readings at once
- **Arduino sketch** (ESP32 compatible) in `ferme-arduino-simulator/arduino_sketch/`

### 3. AI Disease Classification
- **Python FastAPI microservice** with TensorFlow MobileNetV2 model
- **10 disease classes**: Mildiou, Oïdium, Rouille, Tache bactérienne, etc.
- **Simulation mode** when no trained model is available
- **Auto-alert generation** when disease detected with confidence > 70%
- **API**: `POST /api/ai/analyze/{imageId}`

### 4. Disease Map & Visualization
- **Disease distribution charts** (bar + pie) per parcelle and globally
- **Spatial GPS map** showing healthy vs. affected parcelles
- **Detection history** with confidence scores and recommendations
- **Per-parcelle disease breakdown**

### 5. Smart Alert System
- **Sensor threshold alerts**: auto-generated when values exceed configured bounds
- **AI disease alerts**: auto-generated from classification results
- **Severity levels**: INFO, WARNING, CRITIQUE (based on deviation magnitude)
- **Configurable thresholds** per sensor type and per parcelle (`seuil_alerte` table)

## Quick Start

### Prerequisites
- Java 17+, Maven 3.8+
- PostgreSQL 14+
- Node.js 18+, npm 9+
- Python 3.9+ (for AI service and simulators)

### 1. Start PostgreSQL & Run Migration
```bash
# Start PostgreSQL (if using Postgres.app)
pg_ctl -D ~/Library/Application\ Support/Postgres/var-18 start

# Run schema updates for smart features
psql -h localhost -U postgres -d ferme_intelligente -f src/main/resources/schema-update.sql
```

### 2. Start Spring Boot Backend
```bash
cd ferme-intelligente
mvn spring-boot:run
# Runs on http://localhost:8080
```

### 3. Start React Frontend
```bash
cd ferme-frontend
npm install
npx vite --port 5173
# Runs on http://localhost:5173
```

### 4. Start AI Service (optional)
```bash
cd ferme-ai-service
pip install -r requirements.txt
python main.py
# Runs on http://localhost:8001
```

### 5. Run Arduino Simulator
```bash
cd ferme-arduino-simulator
pip install requests
python arduino_simulator.py --interval 5
```

### 6. Run Drone Simulator
```bash
cd ferme-ai-service
pip install requests Pillow
python drone_simulator.py
```

## API Endpoints (New)

### IoT Data Ingestion
```
POST /api/iot/data          (no auth required)
POST /api/iot/data/batch    (no auth required)
```

### AI Analysis
```
POST /api/ai/analyze/{imageId}           (JWT required)
GET  /api/ai/diseases/parcelle/{id}      (JWT required)
```

### Parcel Detail
```
GET /api/parcelles/{id}/detail                          (JWT required)
GET /api/parcelles/{parcelleId}/capteurs/{id}/history   (JWT required)
```

## Test Users
| Email | Password | Role |
|-------|----------|------|
| ahmed@ferme.ma | password123 | PROPRIETAIRE |
| karim@ferme.ma | password123 | GESTIONNAIRE |
| youssef@ferme.ma | password123 | AGRICULTEUR |

## Frontend Routes (New)
| Route | Description |
|-------|-------------|
| /manager/parcelle/:id | Smart parcel dashboard with live sensors, alerts, diseases |
| /manager/live-sensors | Real-time sensor monitoring with auto-refresh |
| /manager/disease-map | Disease visualization across all parcelles |
