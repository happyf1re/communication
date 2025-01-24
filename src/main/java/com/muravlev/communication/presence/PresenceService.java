package com.muravlev.communication.presence;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Хранит (sessionId -> username), чтобы понимать, кто сейчас в сети.
 */
@Service
public class PresenceService {

    // Потокобезопасная Map: sessionId -> username
    private final Map<String, String> sessionIdToUser = new ConcurrentHashMap<>();

    public void userConnected(String sessionId, String username) {
        sessionIdToUser.put(sessionId, username);
    }

    public void userDisconnected(String sessionId) {
        sessionIdToUser.remove(sessionId);
    }

    /**
     * Возвращает все текущие userName.
     */
    public Set<String> getOnlineUsers() {
        // Собираем values() в Set (на случай, если один юзер в нескольких вкладках)
        return Collections.unmodifiableSet(
                sessionIdToUser.values().stream().collect(Collectors.toSet())
        );
    }
}
