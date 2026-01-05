package com.vayu.ncr.alert.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.vayu.ncr.alert.dto.AqiDataEvent;
import com.vayu.ncr.alert.entity.Subscription;
import com.vayu.ncr.alert.repository.SubscriptionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor // Auto-injects the Repository
public class AlertConsumer {

    private static final Logger logger = LoggerFactory.getLogger(AlertConsumer.class);
    private final SubscriptionRepository repository; // <--- The DB Connection

    @KafkaListener(topics = "raw-aqi-data", groupId = "aqi-alert-group")
    public void inspectAirQuality(AqiDataEvent event) {
        
        if (event.getAqi() > 300) {
            logger.warn("🚨 High Pollution in {} (AQI: {})! Checking database for subscribers...", event.getCity(), event.getAqi());

            // 1. FETCH REAL SUBSCRIBERS FROM DB
            List<Subscription> subscribers = repository.findByCity(event.getCity());

            if (subscribers.isEmpty()) {
                logger.info("No one is subscribed to {}. No alerts sent.", event.getCity());
                return;
            }

            // 2. ALERT THEM
            for (Subscription sub : subscribers) {
                // In a real app, you would call EmailService.send(sub.getEmail(), ...) here
                logger.info("📧 SENDING EMAIL To: [ {} ] | Subject: DANGER in {}", sub.getEmail(), sub.getCity());
            }
        }
    }
}