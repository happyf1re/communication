package com.muravlev.communication.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

/**
 * Перехватчик, который при CONNECT вытаскивает login: из заголовка
 * и прописывает его в accessor.setUser(...).
 */
@Component
public class UserInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(message);

        // Если это CONNECT кадр
        if (StompCommand.CONNECT.equals(sha.getCommand())) {
            // Берём логин (sha.getLogin() — то, что клиент передал при stompClient.connect({login: ...}))
            String login = sha.getLogin();
            if (login == null || login.isEmpty()) {
                // fallback
                login = "anon-" + sha.getSessionId();
            }

            // Назначаем principal = наш логин
            sha.setUser(new StompPrincipal(login));
        }

        return message;
    }
}

