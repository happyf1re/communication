package com.muravlev.communication.ui;

import com.muravlev.communication.chat.ChatMessage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;

public class ChatController {

    @FXML
    private ListView<String> messageList;

    @FXML
    private TextField messageInput;

    private StompSession stompSession;

    @FXML
    public void initialize() {
        connectToWebSocket();
    }

    public void connectToWebSocket() {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        try {
            stompSession = stompClient.connect("ws://localhost:8080/ws", new StompSessionHandlerAdapter() { // Заменено на ws://
                @Override
                public void afterConnected(StompSession session, StompHeaders headers) {
                    System.out.println("Connected to WebSocket!");

                    session.subscribe("/topic/messages", new StompFrameHandler() {
                        @Override
                        public Type getPayloadType(StompHeaders headers) {
                            return ChatMessage.class;
                        }

                        @Override
                        public void handleFrame(StompHeaders headers, Object payload) {
                            ChatMessage message = (ChatMessage) payload;
                            Platform.runLater(() -> messageList.getItems().add(message.getUsername() + ": " + message.getText()));
                        }
                    });
                }
            }).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void sendMessage() {
        String message = messageInput.getText();
        if (!message.isEmpty()) {
            messageList.getItems().add("You: " + message);
            sendMessageOverWebSocket(message);
            messageInput.clear();
        }
    }

    public void sendMessageOverWebSocket(String message) {
        if (stompSession != null) {
            stompSession.send("/app/chat.send", new ChatMessage("YourUsername", message));
        } else {
            System.out.println("WebSocket session is not connected.");
        }
    }
}
