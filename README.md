# 🌪️ Vayu: Distributed Air Quality Monitoring System

**Vayu** is a real-time, event-driven microservices architecture designed to monitor air quality (AQI). It uses the **Fan-Out Pattern** to ingest data, archive it for history, and trigger instant alerts for hazardous conditions.

![Java](https://img.shields.io/badge/Java-17-orange) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.0-green) ![Kafka](https://img.shields.io/badge/Apache_Kafka-Event_Streaming-red) ![Postgres](https://img.shields.io/badge/PostgreSQL-Database-blue)

## 🏗️ Architecture
The system completely decouples Ingestion (Entry) from Action (Alerts/DB).

```mermaid
graph TD
    User(User/Sensor/WAQI) -- "POST /inject" --> Ingestion[🟢 Ingestion Service]
    Ingestion -- "Raw Event" --> Kafka{🟠 Apache Kafka}
    
    Kafka -- "Topic: raw-aqi-data" --> DB_Service[🔵 Processing Service]
    Kafka -- "Topic: raw-aqi-data" --> Alert_Service[🔴 Alert Hawk Service]
    
    DB_Service -- "Save" --> Postgres[(PostgreSQL)]
    
    Alert_Service -- "1. Check Threshold" --> Logic{AQI > 300?}
    Logic -- "Yes" --> SubDB[(Subscription DB)]
    SubDB -- "Get Emails" --> Email[📧 Send Notification]
    Logic -- "No" --> Ignore[Log & Ignore]
