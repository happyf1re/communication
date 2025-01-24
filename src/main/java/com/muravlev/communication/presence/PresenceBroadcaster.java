package com.muravlev.communication.presence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Вызывает рассылку списка онлайн-пользователей в /topic/onlineUsers.
 */
@Component
public class PresenceBroadcaster {

    @Autowired
    private PresenceService presenceService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Рассылает текущий список онлайн-пользователей
     * всем, кто подписан на /topic/onlineUsers
     */
    public void broadcastOnlineUsers() {
        messagingTemplate.convertAndSend(
                "/topic/onlineUsers",
                presenceService.getOnlineUsers() // Set<String>
        );
    }
}


