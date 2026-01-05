package com.vayu.ncr.alert.controller;

import com.vayu.ncr.alert.entity.Subscription;
import com.vayu.ncr.alert.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionRepository repository;

    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe(@RequestParam String email, @RequestParam String city) {
        
        
        if (repository.findByEmailAndCity(email, city).isPresent()) {
            return ResponseEntity.badRequest().body("You are already subscribed to " + city);
        }

        repository.save(new Subscription(email, city));
        return ResponseEntity.ok("✅ Subscribed " + email + " to " + city);
    }

    @DeleteMapping("/unsubscribe")
    @Transactional // Required for delete queries in JPA
    public ResponseEntity<String> unsubscribe(@RequestParam String email, @RequestParam String city) {
        repository.deleteByEmailAndCity(email, city);
        return ResponseEntity.ok("❌ Unsubscribed " + email + " from " + city);
    }
}