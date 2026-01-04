package com.vayu.ncr.processing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vayu.ncr.processing.entity.AqiRecord;

@Repository
public interface AqiRepository extends JpaRepository<AqiRecord, Long> {
    // We get .save() for free.
    // We can add lookup methods later if we build a "History API".
	@Query(value = "SELECT * FROM aqi_history a " +
	           "WHERE a.latitude BETWEEN :minLat AND :maxLat " +
	           "AND a.longitude BETWEEN :minLon AND :maxLon " +
	           "ORDER BY a.recorded_at DESC LIMIT 1", 
	           nativeQuery = true)
	    AqiRecord findLatestInBox(@Param("minLat") double minLat, 
	                              @Param("maxLat") double maxLat, 
	                              @Param("minLon") double minLon, 
	                              @Param("maxLon") double maxLon);
}