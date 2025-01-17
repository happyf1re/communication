package com.muravlev.communication.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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
    public void registerUser() {
        try {
            // Подготовка данных
            String username = usernameField.getText();
            String department = departmentField.getText();
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();

            String jsonData = String.format(
                    "{\"username\":\"%s\",\"department\":\"%s\",\"firstName\":\"%s\",\"lastName\":\"%s\"}",
                    username, department, firstName, lastName
            );

            // Отправка POST-запроса
            URL url = new URL("http://localhost:8080/api/employees/register");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Проверка ответа
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                showAlert(AlertType.INFORMATION, "Success", "User registered successfully!");
            } else {
                showAlert(AlertType.ERROR, "Error", "Failed to register user!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
        }
    }

    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
