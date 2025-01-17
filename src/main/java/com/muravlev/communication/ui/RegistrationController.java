package com.muravlev.communication.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RegistrationController {

    @FXML
    private TextField usernameField;
    @FXML
    private TextField departmentField;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private PasswordField passwordField;

    @FXML
    public void registerUser() {
        try {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String department = departmentField.getText();
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();

            String jsonData = String.format(
                    "{\"username\":\"%s\",\"password\":\"%s\",\"department\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\"}",
                    username, password, department, firstName, lastName
            );

            URL url = new URL("http://localhost:8080/api/employees/register");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "User registered successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to register user!");
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
        }
    }

    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void goToLogin() {
        Main.switchScene("/login_form.fxml", "User Login");
    }
}
