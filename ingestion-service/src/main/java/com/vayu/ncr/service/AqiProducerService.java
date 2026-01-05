package com.vayu.ncr.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private static final Logger logger = LoggerFactory.getLogger(AqiProducerService.class);
    
    // We will track these 3 cities for now
    private static final String[] CITIES = {"delhi", "noida","here","geo:28.58;77.44","geo:28.61;77.23"};
    
    private static final List<String> MONITORED_LOCATIONS = Arrays.asList(
            "geo:28.58;77.44",      // Greater Noida (Sector 1)
            "geo:28.62;77.38",      // Noida (Sector 62)
            "delhi/anand-vihar",    // Delhi (Pollution Hotspot)
            "gurugram/sector-51"    // Gurugram
    );
    
    private final KafkaTemplate<String, AqiDataEvent> kafkaTemplate;

    private final RestTemplate restTemplate; 

    private final ObjectMapper objectMapper;
    
    @Value("${waqi.api.token}")
    private String API_TOKEN;
    
    // Runs every 10 seconds
    //@Scheduled(fixedRate = 10000)
    @Cacheable(value = "aqi_cache", key = "#locationParam")
    public AqiDataEvent fetchAndStreamRealData( String locationParam) {
            try {
                String url = "https://api.waqi.info/feed/" + locationParam + "/?token="+API_TOKEN;
                String response = restTemplate.getForObject(url, String.class);

                JsonNode root = objectMapper.readTree(response);

                if (root.path("status").asText().equals("ok")) {
                    JsonNode data = root.path("data");
                    JsonNode cityInfo = data.path("city");
                    
                    String stationName = cityInfo.path("name").asText();
                    AqiDataEvent event = new AqiDataEvent();
                    event.setCity(locationParam);
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
                    return event;
            
                } else {
                    System.err.println("❌ API Error for " + locationParam + ": " + root.path("data").asText());
                }
            } catch (Exception e) {
                System.err.println("⚠️ Error fetching " + locationParam + ": " + e.getMessage());
            }
            return null;
    }
	    
	public void publishManualEvent(AqiDataEvent event) {
	        
	       
	        if (event.getRecordedAt() == null) {
	            event.setRecordedAt(LocalDateTime.now().toString());
	        }
	
	        logger.info("🧪 MANUAL INJECTION: Pushing fake event for {} to Kafka", event.getCity());
	        
	        // Direct send
	        kafkaTemplate.send("raw-aqi-data", event);
	    }
    
    public void triggerBatchScan() {
        System.out.println("🔄 Starting Batch Scan for NCR...");
        for(String location : MONITORED_LOCATIONS) {
            fetchAndStreamRealData(location);  }
        System.out.println("✅ Batch Scan Completed.");
    }
    
    public List<String> getMonitoredLocations() {
        return MONITORED_LOCATIONS;
    }
    
    
    public void fetchForCoordinates(String lat, String lon) {
        String locationParam = "geo:" + lat + ";" + lon;
        fetchAndStreamRealData(locationParam);
    }
    
    public void fetchByCityName(String city) {
        // We pass the city name directly (e.g., "bangalore")
        fetchAndStreamRealData(city);
    }
    
    
    @Scheduled(fixedRate = 600000)
    @CacheEvict(value = "aqi_cache", allEntries = true)
    public void clearCache() {
        System.out.println("⏰ Cache Cleared! Next request will fetch fresh data.");
    }
    
    
}