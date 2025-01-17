package com.muravlev.communication.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    public void goToRegister() {
        Main.switchScene("/registration_form.fxml", "User Registration");
    }

    @FXML
    public void loginUser() {
        try {
            String username = usernameField.getText();
            String password = passwordField.getText();

            String jsonData = String.format(
                    "{\"username\":\"%s\",\"password\":\"%s\"}",
                    username, password
            );

            URL url = new URL("http://localhost:8080/api/employees/login");
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
                // Получаем данные о пользователе
                Map<String, String> userInfo = fetchUserInfo(username);

                // Переход к личному кабинету
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/user_dashboard.fxml"));
                Stage stage = Main.getPrimaryStage();
                stage.setScene(new Scene(loader.load(), 400, 300));

                UserDashboardController controller = loader.getController();
                controller.setUserInfo(
                        userInfo.get("username"),
                        userInfo.get("firstName"),
                        userInfo.get("lastName")
                );

                stage.setTitle("User Dashboard");
            } else {
                showAlert(AlertType.ERROR, "Error", "Invalid username or password!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Error", "An error occurred: " + e.getMessage());
        }
    }

    private Map<String, String> fetchUserInfo(String username) throws Exception {
        URL url = new URL("http://localhost:8080/api/employees/" + username);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new InputStreamReader(conn.getInputStream()), Map.class);
    }

    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

