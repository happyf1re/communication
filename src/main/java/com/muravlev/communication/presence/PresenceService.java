package com.muravlev.communication.presence;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Вместо sessionId->username
 * теперь: username -> PresenceInfo{set of sessionIds, status}.
 */
@Service
public class PresenceService {

    // username -> PresenceInfo
    private final Map<String, PresenceInfo> presenceMap = new ConcurrentHashMap<>();

    /**
     * Новое подключение WebSocket.
     */
    public void userConnected(String sessionId, String username) {
        // Берём/создаём запись для этого username
        PresenceInfo info = presenceMap.computeIfAbsent(username, (u) -> {
            PresenceInfo pi = new PresenceInfo();
            pi.setUsername(u);
            pi.setStatus(UserStatus.SLEEP);
            return pi;
        });

        // Добавляем sessionId
        info.getSessionIds().add(sessionId);
        // Ставим статус ONLINE
        info.setStatus(UserStatus.ONLINE);
    }

    /**
     * Отключилась одна из сессий пользователя.
     */
    public void userDisconnected(String sessionId) {
        // Найти, у кого был этот sessionId
        presenceMap.values().forEach(info -> {
            if (info.getSessionIds().remove(sessionId)) {
                // Если больше нет сессий у этого пользователя => SLEEP
                if (info.getSessionIds().isEmpty()) {
                    info.setStatus(UserStatus.SLEEP);
                }
            }
        });
    }

    /**
     * Пользователь явно вышел (logout).
     * Полностью убираем из presenceMap, чтобы он исчез из списка.
     */
    public void userLoggedOut(String username) {
        presenceMap.remove(username);
    }

    /**
     * Возвращаем всю совокупность (username, status).
     */
    public List<UserStatusDTO> getAllUsersStatuses() {
        return presenceMap.values().stream()
                .map(pi -> new UserStatusDTO(pi.getUsername(), pi.getStatus()))
                .collect(Collectors.toList());
    }

    // DTO-шка, чтобы на фронте было удобно: {username, status}.
    public record UserStatusDTO(String username, UserStatus status) {}
}


