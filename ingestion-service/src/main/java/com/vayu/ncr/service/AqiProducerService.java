package com.vayu.ncr.service;

import java.time.LocalDateTime;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vayu.ncr.dto.AqiDataEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AqiProducerService {

    private static final String TOPIC = "raw-aqi-data";
    
    // 🔴 PASTE YOUR TOKEN INSIDE THE QUOTES BELOW 🔴
    private static final String API_TOKEN = TOKEN_VALUE_HERE; 
    
    // We will track these 3 cities for now
    private static final String[] CITIES = {"delhi", "noida","here","geo:28.58;77.44","geo:28.61;77.23"};

    private final KafkaTemplate<String, AqiDataEvent> kafkaTemplate;

    private final RestTemplate restTemplate; 

    private final ObjectMapper objectMapper;

    // Runs every 10 seconds
    @Scheduled(fixedRate = 10000)
    public void fetchAndStreamRealData() {
        for (String city : CITIES) {
            try {
                String url = "https://api.waqi.info/feed/" + city + "/?token="+API_TOKEN;
                String response = restTemplate.getForObject(url, String.class);

                JsonNode root = objectMapper.readTree(response);

                if (root.path("status").asText().equals("ok")) {
                    JsonNode data = root.path("data");
                    JsonNode cityInfo = data.path("city");
                    
                    String stationName = cityInfo.path("name").asText();
                    AqiDataEvent event = new AqiDataEvent();
                    event.setCity(city);
                    event.setStationId("WAQI-" + data.path("idx").asInt());
                    event.setAqi(data.path("aqi").asInt());
                    
                    // Handle Location Data safely
                    if (data.has("city") && data.path("city").has("geo")) {
                        event.setLatitude(data.path("city").path("geo").get(0).asDouble());
                        event.setLongitude(data.path("city").path("geo").get(1).asDouble());
                    }
                    
                    event.setTimestamp(LocalDateTime.now().toString());

                    kafkaTemplate.send(TOPIC, event);
                    System.out.println("------------------------------------------------");
                    System.out.println("📍 Station Found: " + stationName); // <--- VALIDATION
                    System.out.println("🚀 Sent to Kafka AQI :     " + event.getAqi());
            
                } else {
                    System.err.println("❌ API Error for " + city + ": " + root.path("data").asText());
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error fetching " + city + ": " + e.getMessage());
            }
        }
    }
}