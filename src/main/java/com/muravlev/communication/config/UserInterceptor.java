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
            System.out.println(">>>>> CONNECT: login=" + login + " sessionId=" + sha.getSessionId());

            StompPrincipal principal = new StompPrincipal(login);

            sha.setUser(principal);
            sha.setLeaveMutable(true);

            return MessageBuilder.createMessage(message.getPayload(), sha.getMessageHeaders());
        }

        if (StompCommand.SEND.equals(sha.getCommand())) {
            System.out.println(">>>>> SEND from " + sha.getUser() + " to destination=" + sha.getDestination());
        }

        return message;
    }
}



