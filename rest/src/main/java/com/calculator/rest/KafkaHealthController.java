package com.calculator.rest; // Use appropriate package for each module

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/debug")
public class KafkaHealthController {

    @Autowired
    private KafkaAdmin kafkaAdmin;

    @GetMapping("/kafka-health")
    public Map<String, Object> kafkaHealth() {
        Map<String, Object> health = new HashMap<>();
        try {
            // Try to get cluster metadata
            Map<String, Object>  config = kafkaAdmin.getConfigurationProperties();
            health.put("status", "UP");
            health.put("bootstrapServers", config.get("bootstrap.servers"));
            health.put("config", config);
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
        }
        return health;
    }
}