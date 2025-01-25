package com.muravlev.communication.config;

import java.security.Principal;

/**
 * Простейшая обёртка, где name = наш логин
 */
public class StompPrincipal implements Principal {

    private final String name;

    public StompPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
