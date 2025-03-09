package com.muravlev.communication.presence;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * Содержит информацию о пользователе в контексте "присутствия"
 */
@Data
public class PresenceInfo {
    private String username;
    private Set<String> sessionIds = new HashSet<>();
    private UserStatus status = UserStatus.SLEEP; // По умолчанию SLEEP
}

