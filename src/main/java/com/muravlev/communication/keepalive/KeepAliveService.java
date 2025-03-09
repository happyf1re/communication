package com.muravlev.communication.keepalive;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Периодически пингуем сами себя, чтобы не заснуть.
 */
@Service
@Slf4j
public class KeepAliveService {

    private final RestTemplate restTemplate = new RestTemplate();
    private String selfUrl; // Сформируем при старте

    @PostConstruct
    public void init() {
        this.selfUrl = "https://alta-communication.ru/keepalive";
    }

    @Scheduled(fixedRate = 300_000) // раз в 5 минут
    public void keepAlive() {
        try {
            restTemplate.getForObject(selfUrl, String.class);
            log.info("KeepAlive ping done");
        } catch (Exception e) {
            log.warn("KeepAlive failed: {}", e.getMessage());
        }
    }
}

