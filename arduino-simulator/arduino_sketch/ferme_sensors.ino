/*
 * Ferme Intelligente - Arduino IoT Sensor Node
 *
 * Hardware:
 *   - ESP32 or Arduino with WiFi shield
 *   - DHT22 (Temperature + Humidity)
 *   - Soil pH sensor (analog)
 *   - Soil moisture sensor (analog)
 *
 * Sends sensor data to the Spring Boot backend via REST API.
 * Endpoint: POST http://<server-ip>:8080/api/iot/data
 */

#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>

// ===== CONFIGURATION =====
const char* WIFI_SSID     = "YOUR_WIFI_SSID";
const char* WIFI_PASSWORD = "YOUR_WIFI_PASSWORD";
const char* SERVER_URL    = "http://192.168.1.100:8080/api/iot/data";
const int   PARCELLE_ID   = 1;       // ID of the parcelle this node monitors
const int   SEND_INTERVAL = 30000;   // Send data every 30 seconds

// Sensor pins
const int DHT_PIN        = 4;
const int PH_PIN         = 34;  // Analog input
const int MOISTURE_PIN   = 35;  // Analog input

// Capteur IDs (must match database entries)
const int CAPTEUR_TEMP_ID     = 1;
const int CAPTEUR_HUMIDITY_ID = 2;
const int CAPTEUR_PH_ID       = 3;

// ===== SENSOR SIMULATION (for testing without hardware) =====
// Set to true to use simulated values
bool SIMULATION_MODE = true;

// ===== DHT22 Setup =====
// Uncomment for real hardware:
// #include <DHT.h>
// DHT dht(DHT_PIN, DHT22);

void setup() {
    Serial.begin(115200);
    Serial.println("\n🌾 Ferme Intelligente - Sensor Node Starting...");

    // Connect to WiFi
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    Serial.print("Connecting to WiFi");

    int attempts = 0;
    while (WiFi.status() != WL_CONNECTED && attempts < 30) {
        delay(500);
        Serial.print(".");
        attempts++;
    }

    if (WiFi.status() == WL_CONNECTED) {
        Serial.println("\n✅ WiFi Connected! IP: " + WiFi.localIP().toString());
    } else {
        Serial.println("\n❌ WiFi Failed. Running in offline mode.");
    }

    // Initialize sensors
    // dht.begin();  // Uncomment for real DHT22

    Serial.println("📡 Sensor node ready. Sending data every " + String(SEND_INTERVAL/1000) + "s");
}

void loop() {
    float temperature, humidity, ph;

    if (SIMULATION_MODE) {
        // Simulated realistic values
        temperature = 18.0 + random(0, 150) / 10.0;  // 18-33°C
        humidity    = 40.0 + random(0, 400) / 10.0;   // 40-80%
        ph          = 5.5  + random(0, 30) / 10.0;    // 5.5-8.5
    } else {
        // Real sensor readings
        // temperature = dht.readTemperature();
        // humidity = dht.readHumidity();
        // ph = readPhSensor();
        temperature = 0; humidity = 0; ph = 0; // Placeholder
    }

    Serial.println("\n--- Sensor Readings ---");
    Serial.println("🌡️  Temperature: " + String(temperature) + "°C");
    Serial.println("💧 Humidity:    " + String(humidity) + "%");
    Serial.println("🧪 pH:          " + String(ph));

    // Send each sensor reading
    if (WiFi.status() == WL_CONNECTED) {
        sendSensorData(CAPTEUR_TEMP_ID, temperature, "°C");
        sendSensorData(CAPTEUR_HUMIDITY_ID, humidity, "%");
        sendSensorData(CAPTEUR_PH_ID, ph, "pH");
    } else {
        Serial.println("⚠️  WiFi disconnected. Data not sent.");
        WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    }

    delay(SEND_INTERVAL);
}

void sendSensorData(int capteurId, float value, const char* unite) {
    HTTPClient http;
    http.begin(SERVER_URL);
    http.addHeader("Content-Type", "application/json");

    // Build JSON payload
    StaticJsonDocument<200> doc;
    doc["capteurId"] = capteurId;
    doc["valeur"]    = value;
    doc["unite"]     = unite;

    String payload;
    serializeJson(doc, payload);

    int httpCode = http.POST(payload);

    if (httpCode == 200) {
        String response = http.getString();

        // Parse response to check for alerts
        StaticJsonDocument<512> respDoc;
        deserializeJson(respDoc, response);

        bool alertGenerated = respDoc["alerteGeneree"];
        if (alertGenerated) {
            const char* alertMsg = respDoc["alerteMessage"];
            Serial.println("🚨 ALERT: " + String(alertMsg));
            // Could trigger a buzzer or LED here
        } else {
            Serial.println("✅ Sent capteur " + String(capteurId) + ": " + String(value));
        }
    } else {
        Serial.println("❌ HTTP Error: " + String(httpCode));
    }

    http.end();
}

float readPhSensor() {
    // Read analog pH sensor
    int rawValue = analogRead(PH_PIN);
    // Convert to pH (calibration depends on sensor model)
    // Typical: 0V = pH 0, 3.3V = pH 14
    float voltage = rawValue * (3.3 / 4095.0);
    float ph = voltage * (14.0 / 3.3);
    return ph;
}
