package com.muravlev.communication.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class UserDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label userInfoLabel;

    // Метод для установки информации о пользователе
    public void setUserInfo(String username, String firstName, String lastName) {
        welcomeLabel.setText("Welcome, " + firstName + " " + lastName + "!");
        userInfoLabel.setText("Username: " + username + "\nFull Name: " + firstName + " " + lastName);
    }

    @FXML
    public void logout() {
        // Переход к экрану логина
        Main.switchScene("/login_form.fxml", "User Login");
    }
}

