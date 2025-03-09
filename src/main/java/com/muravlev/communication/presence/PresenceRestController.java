package com.muravlev.communication.presence;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/presence")
public class PresenceRestController {

    private final PresenceService presenceService;
    private final PresenceBroadcaster presenceBroadcaster;

    public PresenceRestController(PresenceService presenceService, PresenceBroadcaster presenceBroadcaster) {
        this.presenceService = presenceService;
        this.presenceBroadcaster = presenceBroadcaster;
    }

    @PostMapping("/logout/{username}")
    public void logoutUser(@PathVariable String username) {
        presenceService.userLoggedOut(username);
        presenceBroadcaster.broadcastOnlineUsers();
    }
}

