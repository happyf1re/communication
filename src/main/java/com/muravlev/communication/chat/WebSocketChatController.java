package com.muravlev.communication.chat;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketChatController {

    // Приходит /app/chat.send => этот метод
    @MessageMapping("/chat.send")
    @SendTo("/topic/messages")
    public ChatMessage sendMessage(ChatMessage message) {
        System.out.println("Получено сообщение через WebSocket: " + message.getText());
        return message; // просто ретранслируем

    }
}





