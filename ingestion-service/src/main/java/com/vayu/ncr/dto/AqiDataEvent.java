package com.vayu.ncr.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AqiDataEvent {
	private String stationId;
    private String city;
    private double latitude;
    private double longitude;
    private int aqi;
    private double pm25;
    private double pm10;
    private String timestamp;
    private String recordedAt;
}
