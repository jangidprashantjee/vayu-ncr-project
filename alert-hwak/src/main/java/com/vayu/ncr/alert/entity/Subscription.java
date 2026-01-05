package com.vayu.ncr.alert.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "aqi_subscriptions") // Custom table name to avoid confusion
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email; // The User
    private String city;  // The City they watch

    public Subscription(String email, String city) {
        this.email = email;
        this.city = city;
    }
}