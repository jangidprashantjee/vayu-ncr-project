package com.vayu.ncr.processing.service;

import com.vayu.ncr.processing.dto.AqiDataEvent;
import com.vayu.ncr.processing.entity.AqiRecord;
import com.vayu.ncr.processing.repository.AqiRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AqiConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(AqiConsumerService.class);
    private final AqiRepository aqiRepository;

    @KafkaListener(topics = "raw-aqi-data", groupId = "aqi-processing-group")
    public void consumeAqiData(AqiDataEvent event) {
        
        logger.info("📥 Brain Received: {} (Lat: {}, Lon: {}) | AQI: {}", 
            event.getCity(), event.getLatitude(), event.getLongitude(), event.getAqi());

        // --- SPATIAL DEDUPLICATION LOGIC START ---
        
        // 1. Define the "1 KM Box" (Roughly 0.01 degrees)
        double range = 0.01;
        
        // 2. Search DB for neighbors
        AqiRecord nearbyRecord = aqiRepository.findLatestInBox(
                event.getLatitude() - range,  // Min Lat
                event.getLatitude() + range,  // Max Lat
                event.getLongitude() - range, // Min Lon
                event.getLongitude() + range  // Max Lon
        );

        // 3. Check if the neighbor is a "Duplicate"
        if (nearbyRecord != null) {
            // Is it recent? (Within last 1 hour)
            boolean isRecent = nearbyRecord.getRecordedAt().isAfter(LocalDateTime.now().minusHours(1));
            // Is the AQI value basically the same?
            boolean isSameAqi = nearbyRecord.getAqi() == event.getAqi();

            if (isRecent && isSameAqi) {
                logger.info("🚫 DUPLICATE SKIPPED: Neighbor ID={} already reported AQI {} nearby.", 
                    nearbyRecord.getId(), event.getAqi());
                return; // STOP! Do not save.
            }
        }
        // --- SPATIAL DEDUPLICATION LOGIC END ---

        // 4. Map & Save (Only if unique)
        AqiRecord record = new AqiRecord();
        record.setCity(event.getCity());
        record.setStationId(event.getStationId());
        record.setAqi(event.getAqi());
        record.setLatitude(event.getLatitude());
        record.setLongitude(event.getLongitude());
        record.setRecordedAt(LocalDateTime.now());

        if (event.getStationId().startsWith("WAQI")) {
            record.setSourceType("GOVT");
        } else {
            record.setSourceType("SENSOR");
        }

        AqiRecord saved = aqiRepository.save(record);
        logger.info("✅ Saved to DB: ID={} [Source: {}]", saved.getId(), saved.getSourceType());
    }
}