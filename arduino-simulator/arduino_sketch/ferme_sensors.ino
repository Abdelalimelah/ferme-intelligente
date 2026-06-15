/*
 * Ferme Intelligente - Arduino IoT Sensor Node
 *
 * Hardware:
 *   - ESP32 (Arduino Nano ESP32 or Dev Module)
 *   - DHT11/DHT22 (Temperature + Humidity) on pin 25
 *   - Soil moisture sensor (analog) on pin 35
 *   - Soil pH sensor (analog) on pin 32
 *
 * Sends sensor data to the Spring Boot backend via REST API.
 * Endpoint: POST http://<server-ip>:8080/api/iot/data
 */

#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>
#include <DHT.h>

// ===== CONFIGURATION =====
const char* WIFI_SSID     = "HONOR X9b 5G";
const char* WIFI_PASSWORD = "87654321";
const char* SERVER_URL    = "http://10.1.152.243:8080/api/iot/data";
const int   PARCELLE_ID   = 1;
const int   SEND_INTERVAL = 30000;   // Send data every 30 seconds
const char* API_KEY       = "parcelle1";

// Sensor pins
DHT dht(25, DHT11);
const int MOISTURE_PIN = 35;
const int PH_PIN       = 32;

// Type names — must exactly match the 'type' column in the capteur table
const char* TYPE_TEMP     = "Température";
const char* TYPE_HUMIDITY = "Humidité";
const char* TYPE_PH       = "pH";
const char* TYPE_MOISTURE = "Humidite_Sol";

// Set to true to send simulated values without real sensors
bool SIMULATION_MODE = false;

void setup() {
    Serial.begin(115200);
    dht.begin();
    delay(2000);
    Serial.println("\n Ferme Intelligente - Sensor Node Starting...");

    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    Serial.print("Connecting to WiFi");

    int attempts = 0;
    while (WiFi.status() != WL_CONNECTED && attempts < 50) {
        delay(500);
        Serial.print(".");
        attempts++;
    }

    if (WiFi.status() == WL_CONNECTED) {
        Serial.println("\nWiFi Connected! IP: " + WiFi.localIP().toString());
    } else {
        Serial.println("\nWiFi Failed. Running in offline mode.");
    }

    Serial.println("Sensor node ready. Sending data every " + String(SEND_INTERVAL / 1000) + "s");
}

void loop() {
    float temperature, humidity, ph, moisture;

    if (SIMULATION_MODE) {
        temperature = 18.0 + random(0, 150) / 10.0;
        humidity    = 40.0 + random(0, 400) / 10.0;
        ph          = 5.5  + random(0, 30)  / 10.0;
        moisture    = 30.0 + random(0, 400) / 10.0;
    } else {
        temperature = dht.readTemperature();
        humidity    = dht.readHumidity();
        moisture    = analogRead(MOISTURE_PIN) * (100.0 / 4095.0);
        ph          = 7.0; // Replace with real pH sensor reading if available
    }

    Serial.println("\n--- Sensor Readings ---");
    Serial.println("Temperature:   " + String(temperature) + " C");
    Serial.println("Humidity:      " + String(humidity) + " %");
    Serial.println("pH:            " + String(ph));
    Serial.println("Soil Moisture: " + String(moisture) + " %");

    if (WiFi.status() == WL_CONNECTED) {
        sendSensorData(TYPE_TEMP,     temperature, "C");
        sendSensorData(TYPE_HUMIDITY, humidity,    "%");
        sendSensorData(TYPE_PH,       ph,          "pH");
        sendSensorData(TYPE_MOISTURE, moisture,    "%");
    } else {
        Serial.println("WiFi disconnected. Data not sent.");
        WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    }

    delay(SEND_INTERVAL);
}

void sendSensorData(const char* type, float value, const char* unite) {
    HTTPClient http;
    http.begin(SERVER_URL);
    http.addHeader("Content-Type", "application/json");

    StaticJsonDocument<256> doc;
    doc["parcelleId"] = PARCELLE_ID;
    doc["type"]       = type;
    doc["valeur"]     = value;
    doc["unite"]      = unite;
    doc["apiKey"]     = API_KEY;

    String payload;
    serializeJson(doc, payload);

    int httpCode = http.POST(payload);

    if (httpCode == 200) {
        String response = http.getString();
        StaticJsonDocument<512> respDoc;
        deserializeJson(respDoc, response);

        bool alertGenerated = respDoc["alerteGeneree"];
        if (alertGenerated) {
            const char* alertMsg = respDoc["alerteMessage"];
            Serial.println("ALERT: " + String(alertMsg));
        } else {
            Serial.println("OK [" + String(type) + "]: " + String(value));
        }
    } else {
        Serial.println("HTTP Error " + String(httpCode) + " for type: " + String(type));
    }

    http.end();
}
