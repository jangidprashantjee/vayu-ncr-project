package com.vayu.ncr.alert.repository;

import com.vayu.ncr.alert.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    // Find all users who follow "Delhi" (Used by the Consumer)
    List<Subscription> findByCity(String city);
    
    // Check if "john@gmail.com" already follows "Delhi" (Used by Controller)
    Optional<Subscription> findByEmailAndCity(String email, String city);
    
    // Unsubscribe helper
    void deleteByEmailAndCity(String email, String city);
}