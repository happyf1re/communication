package com.muravlev.communication.presence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

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
            // Если клиент не передал login при connect
            username = "Anon-" + sha.getSessionId();
        }

        // Запоминаем (sessionId -> username)
        presenceService.userConnected(sha.getSessionId(), username);

        // Рассылаем актуальный список всем подписчикам /topic/onlineUsers
        presenceBroadcaster.broadcastOnlineUsers();
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        // Удаляем запись
        presenceService.userDisconnected(sha.getSessionId());

        // Снова рассылаем список, чтобы убрать этого пользователя
        presenceBroadcaster.broadcastOnlineUsers();
    }
}


