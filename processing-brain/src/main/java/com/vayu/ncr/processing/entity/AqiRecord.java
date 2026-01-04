package com.vayu.ncr.processing.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "aqi_history",
    indexes = {
        @Index(name = "idx_city", columnList = "city"),           // Faster search by City
        @Index(name = "idx_recorded_at", columnList = "recorded_at") // Faster "History" graphs
    }
)
@Data
public class AqiRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String city;

    // 'name' maps the Java field 'stationId' to DB column 'station_id'
    @Column(name = "station_id", nullable = false)
    private String stationId; 

    private int aqi;
    
    private double latitude;
    private double longitude;

    @Column(name = "source_type")
    private String sourceType; // 'GOVT' or 'SENSOR'

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;
}