package com.muravlev.communication.presence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Отслеживает события CONNECT / DISCONNECT,
 * Чтобы сохранять (sessionId -> username) и рассылает обновленный список.
 */
@Component
public class WebSocketSessionListener {

    @Autowired
    private PresenceService presenceService;

    @Autowired
    private PresenceBroadcaster presenceBroadcaster;

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        // Достаём заголовки STOMP
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());

        // Читаем login, который клиент передал при stompClient.connect({ login: ...})
        String username = sha.getLogin();
        if (username == null || username.isEmpty()) {
            // Если клиент почему-то не передал, придумаем
            username = "Anon-" + sha.getSessionId();
        }

        // Запоминаем
        presenceService.userConnected(sha.getSessionId(), username);
        // Рассылаем обновлённый список онлайн-пользователей
        presenceBroadcaster.broadcastOnlineUsers();
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());

        // Удаляем запись
        presenceService.userDisconnected(sha.getSessionId());
        // Обновляем список
        presenceBroadcaster.broadcastOnlineUsers();
    }
}

