package com.muravlev.communication.call;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * Контроллер для логики вызовов (1-на-1 видеозвонок).
 */
@Controller
public class CallController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/call.invite") // => "/app/call.invite"
    public void inviteUser(CallInvite invite) {
        System.out.println(">>> Sending message to /user/" + invite.getTo() + "/queue/callInvites");


        messagingTemplate.convertAndSendToUser(
                invite.getTo(),
                "/queue/callInvites",
                invite
        );
    }

    @Data
    public static class CallInvite {
        private String from; // инициатор
        private String to;   // кого вызываем
        private String room; // название комнаты
    }
}





