package com.muravlev.communication.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Устанавливаем основной stage
        Main.setPrimaryStage(primaryStage);

        // Загружаем первую сцену (форма логина)
        Main.switchScene("/login_form.fxml", "User Login");

        // Отображаем stage
        primaryStage.show();
    }

    // Метод для установки основного stage
    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    // Метод для получения основного stage
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    // Метод для смены сцены
    public static void switchScene(String fxmlFile, String title) {
        try {
            // Загружаем FXML файл
            Parent root = FXMLLoader.load(Main.class.getResource(fxmlFile));

            // Устанавливаем заголовок окна
            primaryStage.setTitle(title);

            // Устанавливаем новую сцену
            primaryStage.setScene(new Scene(root, 400, 300));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}




