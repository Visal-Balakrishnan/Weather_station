#include <ESP8266WiFi.h>

// Wi-Fi credentials
const char* ssid = "POCO F5";
const char* password = ""; // Add your Wi-Fi password

// ThingSpeak server and API key
const char* server = "api.thingspeak.com";
String apiKey = "4DOM9ACWSKM4OH4I";  // Replace with your Write API Key

void setup() {
  Serial.begin(9600);  // Begin serial communication
  delay(10);

  // Connect to Wi-Fi
  Serial.println();
  Serial.println("Connecting to Wi-Fi...");
  WiFi.begin(ssid, password);

  // Wait for connection
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting to Wi-Fi...");
  }

  Serial.println("Wi-Fi connected");
  Serial.print("IP Address: ");
  Serial.println(WiFi.localIP());
}

void loop() {
  // Check if any data is available from the Arduino via Serial
  if (Serial.available() > 0) {
    // Read data from Arduino
    String data = Serial.readStringUntil('\n');

    // Debugging output for received data
    Serial.println("Received Data: " + data);

    // Ensure data has the expected number of fields
    int commaIndex1 = data.indexOf(',');
    int commaIndex2 = data.indexOf(',', commaIndex1 + 1);
    if (commaIndex1 == -1 || commaIndex2 == -1) {
      Serial.println("Error: Invalid data format. Expected format: temp,humidity,raindrop");
      return;  // Exit if data format is invalid
    }

    // Parse the data
    String tempDHT = data.substring(0, commaIndex1);
    String humidity = data.substring(commaIndex1 + 1, commaIndex2);
    String raindropValue = data.substring(commaIndex2 + 1);

    // Debugging output
    Serial.println("Parsed Data:");
    Serial.println("Temperature: " + tempDHT);
    Serial.println("Humidity: " + humidity);
    Serial.println("Raindrop Value: " + raindropValue);

    // Validate parsed values
    if (tempDHT.length() == 0 || humidity.length() == 0 || raindropValue.length() == 0) {
      Serial.println("Error: One or more parsed values are empty.");
      return;  // Exit if any value is invalid
    }

    // Send the data to ThingSpeak
    sendDataToThingSpeak(tempDHT, humidity, raindropValue);
  }

  delay(20000);  // Wait 20 seconds before sending the next batch of data
}

void sendDataToThingSpeak(String temperature, String humidity, String raindropValue) {
  if (WiFi.status() == WL_CONNECTED) {
    WiFiClient client;

    // Try to connect to ThingSpeak
    if (client.connect(server, 80)) {
      // Construct the HTTP POST request body
      String postStr = apiKey;
      postStr += "&field1=" + temperature;
      postStr += "&field2=" + humidity;
      postStr += "&field3=" + raindropValue;
      postStr += "\r\n\r\n";

      // Construct the HTTP POST request
      client.print("POST /update HTTP/1.1\n");
      client.print("Host: api.thingspeak.com\n");
      client.print("Connection: close\n");
      client.print("X-THINGSPEAKAPIKEY: " + apiKey + "\n");
      client.print("Content-Type: application/x-www-form-urlencoded\n");
      client.print("Content-Length: " + String(postStr.length()) + "\n\n");
      client.print(postStr);

      Serial.println("Data sent to ThingSpeak:");
      Serial.println(postStr);

      // Variable to check if a successful response is received
      bool success = false;

      // Check the HTTP response
      while (client.connected() || client.available()) {
        if (client.available()) {
          String line = client.readStringUntil('\n');
          Serial.println("ThingSpeak Response: " + line);
          if (line.startsWith("HTTP/1.1 200 OK")) {
            success = true;  // Mark success if 200 OK is found
          }
        }
      }

      if (success) {
        Serial.println("Data successfully sent to ThingSpeak");
      } else {
        Serial.println("Failed to send data to ThingSpeak");
      }

      // Close the connection
      client.stop();
    } else {
      Serial.println("Connection to ThingSpeak failed.");
    }
  } else {
    Serial.println("Wi-Fi not connected. Reconnecting...");
    reconnectWiFi();
  }
}

void reconnectWiFi() {
  while (WiFi.status() != WL_CONNECTED) {
    WiFi.begin(ssid, password);
    Serial.println("Reconnecting to Wi-Fi...");
    delay(5000);
  }
  Serial.println("Reconnected to Wi-Fi.");
}
