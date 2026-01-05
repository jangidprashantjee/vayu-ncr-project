package com.vayu.ncr.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vayu.ncr.dto.AqiDataEvent;
import com.vayu.ncr.service.AqiProducerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/aqi")
public class AqiController {
	
	private final AqiProducerService aqiProducerService;
	
	//user check live aqi data for given lat lon
	@GetMapping("/live")
	public ResponseEntity<?> getLiveAqi(@RequestParam double lat, @RequestParam double lon) {
		double roundedLat = Math.round(lat * 100.0) / 100.0;
	    double roundedLon = Math.round(lon * 100.0) / 100.0;
		String locationParam = "geo:" + roundedLat + ";" + roundedLon;
		AqiDataEvent data=aqiProducerService.fetchAndStreamRealData(locationParam);
		if (data == null) {
	        return ResponseEntity.badRequest().body("Could not fetch AQI data for this location.");
	    }
	    
	    // Return 200 OK with the JSON object
	    return ResponseEntity.ok(data);
	}
	
	
	//user get list of monitored locations which are supported by app , so that user can choose from them
	@GetMapping("/locations")
    public ResponseEntity<List<String>> getLocations() {
        return ResponseEntity.ok(aqiProducerService.getMonitoredLocations());
    }
	
	//post admin endpoint to trigger aqi data production for all monitored locations
	
	@PostMapping("/trigger-batch")
	public ResponseEntity<?> triggerAqi() {
		aqiProducerService.triggerBatchScan();
		return ResponseEntity.ok("AQI data production for all monitored locations started.");
	}	
	
	
	// Admin endpoint to manually inject AQI data for testing
	@PostMapping("/inject")
    public ResponseEntity<String> manualInjection(@RequestBody AqiDataEvent event) {
        
        if (event.getCity() == null || event.getAqi() == 0) {
            return ResponseEntity.badRequest().body("Error: City and AQI are required.");
        }

        // Call the NEW clean method
        aqiProducerService.publishManualEvent(event);

        return ResponseEntity.ok("✅ Successfully injected: " + event.getCity());
    }
}
