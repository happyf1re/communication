package com.muravlev.communication.keepalive;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KeepAliveController {

    @GetMapping("/keepalive")
    public String keepAlive() {
        return "OK " + System.currentTimeMillis();
    }
}

