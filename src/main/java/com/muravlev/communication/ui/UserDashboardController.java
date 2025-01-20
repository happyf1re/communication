package com.muravlev.communication.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class UserDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label userInfoLabel;

    // Устанавливаем данные о пользователе
    public void setUserInfo(String username, String firstName, String lastName, String department) {
        welcomeLabel.setText("Welcome, " + firstName + " " + lastName + "!");
        userInfoLabel.setText("Username: " + username + "\nDepartment: " + department);
    }

    @FXML
    public void startChat() {
        Main.switchScene("/chat_view.fxml", "Chat Room");
    }

    @FXML
    public void startVideoConference() {
        // TODO: Реализовать функционал видеоконференций
        System.out.println("Video conference started!");
    }

    @FXML
    public void logout() {
        // Переход на экран логина
        Main.switchScene("/login_form.fxml", "User Login");
    }
}


