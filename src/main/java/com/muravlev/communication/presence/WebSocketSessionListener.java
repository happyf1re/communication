package com.muravlev.communication.presence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Отслеживаем CONNECT / DISCONNECT.
 */
@Component
public class WebSocketSessionListener {

    @Autowired
    private PresenceService presenceService;

    @Autowired
    private PresenceBroadcaster presenceBroadcaster;

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());

        String username = sha.getLogin();
        if (username == null || username.isEmpty()) {
            username = "Anon-" + sha.getSessionId();
        }

        presenceService.userConnected(sha.getSessionId(), username);
        presenceBroadcaster.broadcastOnlineUsers();
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());

        presenceService.userDisconnected(sha.getSessionId());
        presenceBroadcaster.broadcastOnlineUsers();
    }

}



