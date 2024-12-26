package com.chat;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.chat.client.ChatClient;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.time.LocalDateTime; // Добавляем импорт для работы с датой и временем
import java.time.format.DateTimeFormatter; // Импорт для форматирования времени

public class ChatApp extends Application {
    private static final Logger logger = LoggerFactory.getLogger(ChatApp.class);
    private TextArea chatArea;
    private TextField inputField;
    private TextField usernameField;
    private TextField passwordField;
    private Button sendButton;
    private Button logoutButton; // Кнопка для выхода
    private Button registerButton; // Кнопка для регистрации
    private Button loginButton; // Кнопка для входа
    private Label statusLabel; // Label для отображения статуса
    private LaunchServer launchServer;
    private static final String CHAT_HISTORY_FILE = "chat_history.txt"; // Файл для истории чата
    private String currentUsername; // Текущий пользователь

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Chat Application");
        launchServer = new LaunchServer();
        launchServer.initialize();

        Label logoLabel = new Label("ChatHub");
        logoLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-padding: 10;"); // Стиль для логотипа

        usernameField = new TextField();
        usernameField.setPromptText("Имя пользователя");

        passwordField = new TextField();
        passwordField.setPromptText("Пароль");

        loginButton = new Button("Войти");
        registerButton = new Button("Зарегистрироваться");
        sendButton = new Button("Отправить");
        sendButton.setVisible(false);

        logoutButton = new Button("Выйти"); // Инициализация кнопки выхода
        logoutButton.setVisible(false); // Скрываем кнопку до входа
        logoutButton.setOnAction(event -> handleLogout()); // Обработчик для выхода

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setVisible(false);
        chatArea.setPrefHeight(300);
        chatArea.setWrapText(true);
        loadChatHistory(); // Загружаем историю чата при запуске

        inputField = new TextField();
        inputField.setPromptText("Введите ваше сообщение...");
        inputField.setVisible(false);

        statusLabel = new Label(); // Инициализация Label для статуса
        statusLabel.setStyle("-fx-text-fill: red;"); // Установка цвета текста для ошибок

        loginButton.setOnAction(event -> handleLogin());
        registerButton.setOnAction(event -> handleRegister());
        sendButton.setOnAction(event -> handleSendMessage());

        // Создаем HBox для кнопки выхода
        HBox topBar = new HBox();
        topBar.getChildren().add(logoutButton);
        topBar.setStyle("-fx-alignment: center-right; -fx-padding: 10;"); // Выравнивание и отступы

        VBox vbox = new VBox(10, logoLabel,usernameField, passwordField, loginButton, registerButton, statusLabel, chatArea, inputField, sendButton);
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(topBar);
        borderPane.setCenter(vbox);

        Scene scene = new Scene(borderPane, 400, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (launchServer.login(username, password)) {
            currentUsername = username; // Сохраняем имя пользователя
            showChat();
            logger.info("Пользователь {} вошел в систему", username);
            startChatClient();
            statusLabel.setText(""); // Очищаем сообщение об ошибке
        } else {
            statusLabel.setText("Ошибка входа. Проверьте имя пользователя и пароль."); // Отображаем ошибку
            logger.warn("Ошибка входа для пользователя {}", username);
        }
    }

    private void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (launchServer.register(username, password)) {
            statusLabel.setText("Пользователь " + username + " зарегистрирован."); // Успешная регистрация
            logger.info("Пользователь {} зарегистрирован", username);
        } else {
            statusLabel.setText("Ошибка регистрации. Пользователь с таким именем уже существует."); // Отображаем ошибку
            logger.warn("Ошибка регистрации для пользователя {}", username);
        }
    }

    private void handleSendMessage() {
        String message = inputField.getText();
        if (!message.isEmpty()) {
            // Получаем текущее время и форматируем его
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            String time = now.format(formatter);

            String fullMessage = currentUsername + " [" + time + "]: " + message; // Формируем сообщение с ником и временем
            chatArea.appendText(fullMessage + "\n");
            saveMessage(fullMessage); // Сохраняем сообщение в файл
            inputField.clear();
        }
    }

    private void showChat() {
        usernameField.setVisible(false);
        passwordField.setVisible(false);
        loginButton.setVisible(false); // Скрываем кнопку "Войти"
        sendButton.setVisible(true);
        logoutButton.setVisible(true); // Показываем кнопку выхода
        registerButton.setVisible(false); // Скрываем кнопку регистрации
        chatArea.setVisible(true);
        inputField.setVisible(true);
        statusLabel.setText(""); // Очищаем статус при входе
    }

    private void handleLogout() {
        // Логика выхода из чата
        usernameField.setVisible(true);
        passwordField.setVisible(true);
        loginButton.setVisible(true); // Показываем кнопку "Войти" при выходе
        sendButton.setVisible(false);
        logoutButton.setVisible(false); // Скрываем кнопку выхода
        registerButton.setVisible(true); // Показываем кнопку регистрации
        chatArea.setVisible(false);
        inputField.setVisible(false);
        statusLabel.setText(""); // Очищаем статус при выходе
        currentUsername = null; // Очищаем текущее имя пользователя
        logger.info("Пользователь вышел из системы");
    }

    private void startChatClient() {
        new Thread(() -> {
            ChatClient client = new ChatClient();
            client.start("localhost", 8080);
        }).start();
    }

    private void saveMessage(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CHAT_HISTORY_FILE, true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            logger.error("Ошибка при сохранении сообщения: {}", e.getMessage());
        }
    }

    private void loadChatHistory() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(CHAT_HISTORY_FILE));
            for (String line : lines) {
                chatArea.appendText(line + "\n");
            }
        } catch (IOException e) {
            logger.error("Ошибка при загрузке истории чата: {}", e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
