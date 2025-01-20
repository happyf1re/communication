package com.muravlev.communication.chat;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketChatController {

    @MessageMapping("/chat.send") // Маршрут для отправки сообщений
    @SendTo("/topic/messages") // Отправляем сообщения подписчикам
    public ChatMessage sendMessage(ChatMessage message) {
        System.out.println("Получено сообщение через WebSocket: " + message.getText());
        return message;
    }
}




