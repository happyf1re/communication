package com.muravlev.communication.presence;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Хранит соответствие sessionId -> username,
 * чтобы понимать, кто сейчас "онлайн".
 */
@Service
public class PresenceService {

    // Потокобезопасная map: sessionId -> username
    private final Map<String, String> sessionIdToUser = new ConcurrentHashMap<>();

    public void userConnected(String sessionId, String username) {
        sessionIdToUser.put(sessionId, username);
    }

    public void userDisconnected(String sessionId) {
        sessionIdToUser.remove(sessionId);
    }

    /**
     * Возвращает множество всех username, которые сейчас онлайн
     */
    public Set<String> getOnlineUsers() {
        // Извлекаем все значения (username) и делаем Set, чтобы не дублировать
        return Collections.unmodifiableSet(
                sessionIdToUser.values().stream().collect(Collectors.toSet())
        );
    }
}
