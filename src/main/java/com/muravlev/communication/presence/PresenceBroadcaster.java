package com.muravlev.communication.presence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Рассылает текущее множество пользователей на топик /topic/onlineUsers.
 */
@Component
public class PresenceBroadcaster {

    @Autowired
    private PresenceService presenceService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Отправляем список онлайн-пользователей в /topic/onlineUsers.
     */
    public void broadcastOnlineUsers() {
        // Берём всех онлайн
        var users = presenceService.getOnlineUsers();
        // Шлём (например, ["User1", "User2"])
        messagingTemplate.convertAndSend("/topic/onlineUsers", users);
    }

}



