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
        // Берём username из заголовка CONNECT (login), если нет - генерим
        String username = sha.getLogin();
        if (username == null || username.isEmpty()) {
            username = "Anon-" + sha.getSessionId();
        }

        // Запоминаем (sessionId -> username)
        presenceService.userConnected(sha.getSessionId(), username);

        // ВАЖНО: сразу рассылаем новый список
        presenceBroadcaster.broadcastOnlineUsers();
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());

        // Удаляем запись
        presenceService.userDisconnected(sha.getSessionId());

        // И снова рассылаем
        presenceBroadcaster.broadcastOnlineUsers();
    }
}


