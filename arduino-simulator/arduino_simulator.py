#!/usr/bin/env python3
"""
Arduino/IoT Sensor Simulator for Ferme Intelligente.

Simulates 3 sensor types (Temperature, Humidity, pH) sending data
to the Spring Boot backend via REST API at configurable intervals.

Generates realistic sensor patterns with:
- Diurnal temperature cycles (warm during day, cool at night)
- Humidity inversely correlated with temperature
- pH with slow drift and occasional spikes
- Random anomalies to trigger smart alerts

Usage:
    python arduino_simulator.py                      # Default: all parcelles, 5s interval
    python arduino_simulator.py --parcelle 1         # Specific parcelle
    python arduino_simulator.py --interval 2         # Every 2 seconds
    python arduino_simulator.py --anomaly-rate 0.2   # 20% chance of anomaly
"""
import argparse
import math
import random
import sys
import time
from datetime import datetime

import requests

API_URL = "http://localhost:8080/api/iot/data"

# Sensor configurations per parcelle (capteur IDs from database)
PARCELLE_SENSORS = {
    1: {"Temperature": 1, "Humidite": 2, "pH": 3},
    2: {"Temperature": 4, "Humidite": 5, "pH": 6},
    3: {"Temperature": 7, "Humidite": 8},
}


class SensorSimulator:
    """Simulates realistic sensor readings for a single parcelle."""

    def __init__(self, parcelle_id: int, sensors: dict, anomaly_rate: float = 0.05):
        self.parcelle_id = parcelle_id
        self.sensors = sensors
        self.anomaly_rate = anomaly_rate
        self.base_temp = 20 + random.uniform(-3, 3)  # Parcelle-specific base temp
        self.base_humidity = 60 + random.uniform(-10, 10)
        self.base_ph = 6.5 + random.uniform(-0.5, 0.5)
        self.ph_drift = 0  # Slow drift for pH

    def get_readings(self) -> list:
        """Generate current sensor readings for all sensors in this parcelle."""
        readings = []
        now = datetime.now()
        hour = now.hour + now.minute / 60.0

        for sensor_type, capteur_id in self.sensors.items():
            value = self._generate_value(sensor_type, hour)
            readings.append({
                "capteurId": capteur_id,
                "valeur": round(value, 2),
            })
        return readings

    def _generate_value(self, sensor_type: str, hour: float) -> float:
        """Generate a realistic sensor value based on type and time of day."""
        is_anomaly = random.random() < self.anomaly_rate

        if sensor_type == "Temperature":
            # Diurnal cycle: warmest at 14h, coolest at 4h
            cycle = math.sin((hour - 4) * math.pi / 12) * 8
            noise = random.gauss(0, 0.5)
            value = self.base_temp + cycle + noise
            if is_anomaly:
                value += random.choice([-15, 20])  # Frost or heat spike
            return max(-5, min(55, value))

        elif sensor_type == "Humidite":
            # Inversely correlated with temperature
            cycle = -math.sin((hour - 4) * math.pi / 12) * 15
            noise = random.gauss(0, 2)
            value = self.base_humidity + cycle + noise
            if is_anomaly:
                value = random.choice([5, 98])  # Extreme dry or wet
            return max(0, min(100, value))

        elif sensor_type == "pH":
            # Slow drift with occasional correction
            self.ph_drift += random.gauss(0, 0.02)
            self.ph_drift = max(-1, min(1, self.ph_drift))
            noise = random.gauss(0, 0.05)
            value = self.base_ph + self.ph_drift + noise
            if is_anomaly:
                value += random.choice([-2, 2])  # Acid or alkaline spike
            return max(2, min(12, value))

        return 0


def send_reading(reading: dict) -> dict:
    """Send a single sensor reading to the API."""
    try:
        resp = requests.post(API_URL, json=reading, timeout=5)
        resp.raise_for_status()
        return resp.json()
    except requests.exceptions.ConnectionError:
        return {"error": "Backend not reachable at " + API_URL}
    except Exception as e:
        return {"error": str(e)}


def print_reading(reading: dict, response: dict):
    """Pretty-print a sensor reading and API response."""
    alert_icon = "🚨" if response.get("alerteGeneree") else "✅"
    capteur_type = response.get("capteurType", "?")
    parcelle = response.get("parcelleNom", "?")
    value = reading["valeur"]

    if "error" in response:
        print(f"  ❌ Capteur {reading['capteurId']}: {value} → {response['error']}")
    else:
        msg = f"  {alert_icon} [{parcelle}] {capteur_type}: {value}"
        if response.get("alerteGeneree"):
            msg += f" → ALERTE: {response['alerteMessage']}"
        print(msg)


def main():
    parser = argparse.ArgumentParser(description="Arduino/IoT Sensor Simulator")
    parser.add_argument("--parcelle", type=int, help="Specific parcelle ID (default: all)")
    parser.add_argument("--interval", type=float, default=5, help="Seconds between readings (default: 5)")
    parser.add_argument("--anomaly-rate", type=float, default=0.05, help="Anomaly probability 0-1 (default: 0.05)")
    parser.add_argument("--count", type=int, default=0, help="Number of iterations (0=infinite)")
    args = parser.parse_args()

    # Build simulators
    parcelles = {args.parcelle: PARCELLE_SENSORS[args.parcelle]} if args.parcelle else PARCELLE_SENSORS
    simulators = {
        pid: SensorSimulator(pid, sensors, args.anomaly_rate)
        for pid, sensors in parcelles.items()
    }

    print("=" * 60)
    print("🌾 Ferme Intelligente - Arduino/IoT Simulator")
    print(f"   Parcelles: {list(parcelles.keys())}")
    print(f"   Interval: {args.interval}s | Anomaly rate: {args.anomaly_rate}")
    print(f"   API: {API_URL}")
    print("=" * 60)

    iteration = 0
    try:
        while True:
            iteration += 1
            timestamp = datetime.now().strftime("%H:%M:%S")
            print(f"\n📡 [{timestamp}] Iteration #{iteration}")

            for pid, sim in simulators.items():
                readings = sim.get_readings()
                for reading in readings:
                    response = send_reading(reading)
                    print_reading(reading, response)

            if args.count > 0 and iteration >= args.count:
                print(f"\n✅ Completed {args.count} iterations.")
                break

            time.sleep(args.interval)

    except KeyboardInterrupt:
        print(f"\n\n🛑 Simulator stopped after {iteration} iterations.")
        sys.exit(0)


if __name__ == "__main__":
    main()
