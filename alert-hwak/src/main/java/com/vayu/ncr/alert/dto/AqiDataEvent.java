package com.vayu.ncr.alert.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AqiDataEvent {
    private String city;
    private String stationId;
    private int aqi;
    private double latitude;
    private double longitude;
    private double pm25;
    private double pm10;
    private String timestamp;
    private String recordedAt;
}