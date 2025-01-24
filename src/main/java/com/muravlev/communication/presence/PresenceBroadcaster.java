package com.muravlev.communication.presence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Рассылает список онлайн-пользователей в /topic/onlineUsers.
 */
@Component
public class PresenceBroadcaster {

    @Autowired
    private PresenceService presenceService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void broadcastOnlineUsers() {
        // Берём всех онлайн-пользователей из PresenceService
        // и отправляем на общий топик
        messagingTemplate.convertAndSend(
                "/topic/onlineUsers",
                presenceService.getOnlineUsers()
        );
    }
}


