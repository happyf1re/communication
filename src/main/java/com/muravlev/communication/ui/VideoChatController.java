package com.muravlev.communication.ui;

import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class VideoChatController {

    @FXML
    private WebView webView;

    @FXML
    public void initialize() {
        WebEngine webEngine = webView.getEngine();

        // Уникальное имя комнаты
        String roomName = "VideoRoom_" + System.currentTimeMillis();

        // Загрузка Jitsi Meet комнаты
        webEngine.load("https://meet.jit.si/" + roomName);
    }
}

