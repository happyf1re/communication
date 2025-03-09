package com.muravlev.communication.presence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class PresenceController {

    @Autowired
    private PresenceService presenceService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Клиент пошлёт stompClient.send('/app/presence.getOnlineUsers')
    @MessageMapping("/presence.getOnlineUsers")
    public void getOnlineUsers() {
        var users = presenceService.getAllUsersStatuses();
        // Например, вернём массив объектов { username, status }
        messagingTemplate.convertAndSend("/topic/onlineUsers", users);
    }
}

