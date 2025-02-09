package com.muravlev.communication.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.stereotype.Component;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;

/**
 * Interceptor, чтобы login -> Principal
 */
@Component
public class UserInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(sha.getCommand())) {

            String login = sha.getLogin();
            if (login == null || login.isEmpty()) {
                login = "anon-" + sha.getSessionId();
            }

            // Лог для проверки
            System.out.println(">>>>> CONNECT: login=" + login + " sessionId=" + sha.getSessionId());

            StompPrincipal principal = new StompPrincipal(login);

            // Ставим Principal
            sha.setUser(principal);

            // ВАЖНО: делаем аксессор «mutable» прежде чем вернуть
            sha.setLeaveMutable(true);

            // Создаём новое сообщение, у которого в заголовках уже user = principal
            return MessageBuilder.createMessage(message.getPayload(), sha.getMessageHeaders());
        }

        return message;
    }
}



