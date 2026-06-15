# 🌾 Ferme Intelligente — Smart Farm Management System

Full-stack smart farming platform: IoT sensors, AI disease detection, multi-role dashboard.

| Service | Tech | Port |
|---|---|---|
| **Backend** | Spring Boot 3.4.5 · Java 17 · PostgreSQL | `8080` |
| **Frontend** | React 19 · Vite 8 · Tailwind CSS 4 | `5173` |
| **AI Service** | FastAPI · Python 3.11 · TensorFlow 2.17 | `8001` |
| **Simulator** | Python (IoT data generator) | — |

---

## Prerequisites

| Tool | Min version | Install |
|---|---|---|
| Java JDK | 17 | https://adoptium.net |
| Maven | 3.9 | https://maven.apache.org |
| Node.js | 20 | https://nodejs.org |
| Python | 3.9+ | https://python.org |
| PostgreSQL | 14+ | https://postgresql.org |
| Docker + Compose | 24+ | https://docs.docker.com |

---

## Option A — Local Development (Manual)

### 1. PostgreSQL — Create the database

```bash
# Connect to PostgreSQL
psql -U postgres

# Create the database
CREATE DATABASE ferme_intelligente;
\q

# Restore the full schema + seed data
psql -U postgres -d ferme_intelligente -f backend/src/main/resources/init.sql

# Apply schema extensions (IoT columns, thresholds)
psql -U postgres -d ferme_intelligente -f backend/src/main/resources/schema-update.sql

# Fix sequences if you get "duplicate key" errors on inserts
psql -U postgres -d ferme_intelligente -c "
  SELECT setval(pg_get_serial_sequence('utilisateur','id'), COALESCE((SELECT MAX(id) FROM utilisateur),1));
  SELECT setval(pg_get_serial_sequence('ferme','id'),       COALESCE((SELECT MAX(id) FROM ferme),1));
  SELECT setval(pg_get_serial_sequence('parcelle','id'),    COALESCE((SELECT MAX(id) FROM parcelle),1));
  SELECT setval(pg_get_serial_sequence('capteur','id'),     COALESCE((SELECT MAX(id) FROM capteur),1));
  SELECT setval(pg_get_serial_sequence('alerte','id'),      COALESCE((SELECT MAX(id) FROM alerte),1));
"
```

### 2. Backend — Spring Boot

```bash
cd backend

# Run in dev mode (hot reload)
mvn spring-boot:run

# Or build JAR and run it
mvn package -DskipTests
java -jar target/ferme-intelligente-*.jar

# Verify it's running
curl http://localhost:8080/api/health
```

Backend starts on **http://localhost:8080**  
API docs (if Swagger added): http://localhost:8080/swagger-ui.html

### 3. AI Service — FastAPI

```bash
cd ai-service

# Create a virtual environment (recommended)
python3 -m venv .venv
source .venv/bin/activate          # Windows: .venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Start the service
uvicorn main:app --host 0.0.0.0 --port 8001 --reload

# Verify
curl http://localhost:8001/health
# → {"status":"ok","model_mode":"simulation","version":"1.0.0"}
```

AI service starts on **http://localhost:8001**

> **Note:** Without a trained `.h5` model at `model/weights/plant_disease_model.h5`,
> the service runs in **simulation mode** — it still returns realistic results for testing.

### 4. Frontend — React + Vite

```bash
cd frontend

# Install dependencies
npm install

# Start dev server (with hot reload)
npm run dev

# → http://localhost:5173
```

Frontend starts on **http://localhost:5173**

### 5. Arduino/IoT Simulator

```bash
cd arduino-simulator

pip install requests

# Simulate ALL parcelles every 5 seconds
python arduino_simulator.py

# Options
python arduino_simulator.py --interval 2          # Every 2 seconds
python arduino_simulator.py --parcelle 1          # Only parcelle 1
python arduino_simulator.py --anomaly-rate 0.2    # 20% anomalies (triggers alerts)
python arduino_simulator.py --count 50            # Stop after 50 iterations

# Output example:
# 📡 [14:32:01] Iteration #1
#   ✅ [Parcelle Nord] Température: 22.4
#   🚨 [Parcelle Nord] Humidité: 97.8 → ALERTE: Humidité critique
#   ✅ [Parcelle Nord] pH: 6.7
```

---

## Order of startup (IMPORTANT)

```
1. PostgreSQL   ← must be ready first
2. Backend      ← needs DB
3. AI Service   ← needs nothing (independent)
4. Frontend     ← needs Backend (for API calls)
5. Simulator    ← optional, needs Backend
```

---

## Option B — Docker Compose (All services at once)

```bash
# Copy env file and edit passwords/secrets
cp .env.example .env
nano .env

# Start all services (postgres + backend + frontend + ai-service)
docker compose up -d

# Watch logs
docker compose logs -f

# Start with IoT simulator too
docker compose --profile simulator up -d

# Stop everything
docker compose down

# Stop and delete volumes (full reset)
docker compose down -v
```

| URL | Service |
|---|---|
| http://localhost:3000 | Frontend (Nginx) |
| http://localhost:8080 | Backend API |
| http://localhost:8001 | AI Service |
| http://localhost:5432 | PostgreSQL |

---

## Demo Accounts

| Role | Email | Password |
|---|---|---|
| Propriétaire | ahmed@ferme.ma | password123 |
| Gestionnaire | karim@ferme.ma | password123 |
| Agriculteur | youssef@ferme.ma | password123 |

---

## API Quick Reference

```bash
BASE=http://localhost:8080/api

# Authenticate
curl -X POST $BASE/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ahmed@ferme.ma","password":"password123"}'

# Use returned token
TOKEN="your_jwt_here"

# List sensors
curl -H "Authorization: Bearer $TOKEN" $BASE/capteurs

# Create user (returns temp password)
curl -X POST $BASE/utilisateurs \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nom":"Alami","prenom":"Sara","email":"sara@ferme.ma","role":"GESTIONNAIRE"}'

# Send IoT reading
curl -X POST $BASE/iot/data \
  -H "Content-Type: application/json" \
  -d '{"capteurId":1,"valeur":23.5}'

# Analyze plant image
curl -X POST http://localhost:8001/analyze/upload \
  -F "file=@/path/to/plant.jpg" \
  -F "parcelle_id=1"

# Health checks
curl $BASE/health                  # Backend
curl http://localhost:8001/health  # AI Service
```

---

## Running Tests

```bash
# Backend unit + integration tests (requires local PostgreSQL)
cd backend
mvn test

# Frontend lint check
cd frontend
npm run lint

# Frontend production build check
npm run build
```

---

## Project Structure

```
ferme-intelligente-project/
├── backend/                    ← Spring Boot API
│   ├── src/main/java/ma/ferme/
│   │   ├── controller/         ← REST controllers
│   │   ├── service/            ← Business logic
│   │   ├── entity/             ← JPA entities
│   │   ├── dto/                ← Data transfer objects
│   │   ├── repository/         ← Spring Data JPA repos
│   │   └── security/           ← JWT filter, config
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   ├── application-prod.properties
│   │   ├── init.sql            ← Full schema + seed data
│   │   └── schema-update.sql   ← IoT / AI column additions
│   └── Dockerfile
│
├── frontend/                   ← React SPA
│   ├── src/
│   │   ├── api/                ← Axios API calls per domain
│   │   ├── components/         ← Shared UI components
│   │   ├── context/            ← AuthContext
│   │   ├── hooks/              ← useAuth, useClickOutside
│   │   ├── layouts/            ← DashboardLayout
│   │   └── pages/
│   │       ├── owner/          ← Propriétaire pages
│   │       ├── manager/        ← Gestionnaire pages
│   │       └── worker/         ← Agriculteur pages
│   ├── nginx.conf
│   └── Dockerfile
│
├── ai-service/                 ← FastAPI AI microservice
│   ├── main.py                 ← FastAPI app + endpoints
│   ├── model/
│   │   └── classifier.py       ← TensorFlow classifier + simulation
│   ├── requirements.txt
│   └── Dockerfile
│
├── arduino-simulator/          ← IoT data generator
│   ├── arduino_simulator.py    ← Simulates sensors → API
│   └── Dockerfile
│
├── docker-compose.yml          ← Orchestrates all 5 services
├── .env.example                ← Copy to .env
├── .gitignore
└── .github/
    └── workflows/
        ├── ci.yml              ← Build + test on every push
        └── cd.yml              ← Build images + deploy on main
```

---

## Troubleshooting

### Port already in use
```bash
# Find and kill the process on a port
lsof -ti:8080 | xargs kill -9
lsof -ti:5173 | xargs kill -9
lsof -ti:8001 | xargs kill -9
```

### PostgreSQL connection refused
```bash
# macOS (Homebrew)
brew services start postgresql@16

# Linux systemd
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

### "duplicate key violates constraint" on inserts
```bash
psql -U postgres -d ferme_intelligente -c "
  DO \$\$ DECLARE r record;
  BEGIN
    FOR r IN SELECT tablename FROM pg_tables WHERE schemaname='public' LOOP
      EXECUTE format('SELECT setval(pg_get_serial_sequence(%L, ''id''), COALESCE(MAX(id),1)) FROM %I', r.tablename, r.tablename);
    END LOOP;
  END \$\$;
"
```

### Frontend blank page / 401 errors
- Check that backend is running on port 8080
- Check browser console for CORS errors
- Verify `.env` file has `VITE_API_URL=http://localhost:8080/api`

### AI Service — model not found
- Service falls back to simulation mode automatically
- To use a real model: place `.h5` file at `ai-service/model/weights/plant_disease_model.h5`

---

## DevOps & Production Setup

See the **DevOps Guide** section below for full deployment instructions.
