package com.muravlev.communication.presence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Рассылает текущее множество пользователей (их username + статус) на /topic/onlineUsers.
 */
@Component
public class PresenceBroadcaster {

    @Autowired
    private PresenceService presenceService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void broadcastOnlineUsers() {
        var all = presenceService.getAllUsersStatuses();
        messagingTemplate.convertAndSend("/topic/onlineUsers", all);
    }
}




