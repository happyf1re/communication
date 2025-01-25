package com.muravlev.communication.presence;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Хранит (sessionId -> username).
 */
@Service
public class PresenceService {

    private final Map<String, String> sessionIdToUser = new ConcurrentHashMap<>();

    /**
     * Запоминаем, что sessionId принадлежит username.
     */
    public void userConnected(String sessionId, String username) {
        sessionIdToUser.put(sessionId, username);
    }

    /**
     * Удаляем запись.
     */
    public void userDisconnected(String sessionId) {
        sessionIdToUser.remove(sessionId);
    }

    /**
     * Возвращаем уникальное множество usernames, которые сейчас в сети.
     */
    public Set<String> getOnlineUsers() {
        return Collections.unmodifiableSet(
                sessionIdToUser.values().stream().collect(Collectors.toSet())
        );
    }
}

