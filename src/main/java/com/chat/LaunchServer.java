package com.chat;

import com.chat.adapter.LocalDateTimeAdapter;
import com.chat.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import com.chat.server.ChatServer;
import com.chat.server.ActionLogger;

public class LaunchServer {
    private static final Logger logger = LoggerFactory.getLogger(LaunchServer.class);
    private static List<User> users = new ArrayList<>();
    private static final String usersFile = "users.json";
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    private static ChatServer server;
    private static ActionLogger actionLogger;

    public void initialize() {
        actionLogger = new ActionLogger();
        loadUsers();
        importUsers("import_users.json");
        server = new ChatServer(actionLogger);
        new Thread(() -> server.start(8080)).start();
    }

    public boolean login(String username, String password) {
        String passwordHash = hashPassword(password);
        return users.stream()
                .anyMatch(user -> user.getUsername().equals(username) && user.getPasswordHash().equals(passwordHash));
    }

    public boolean register(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            logger.warn("Ошибка регистрации: имя пользователя или пароль пусты для пользователя {}", username);
            return false;
        }
        if (users.stream().anyMatch(user -> user.getUsername().equals(username))) {
            logger.warn("Ошибка регистрации: пользователь с именем {} уже существует", username);
            return false;
        }
        String passwordHash = hashPassword(password);
        users.add(new User(username, passwordHash));
        saveUsers();
        actionLogger.logAction(username, "Пользователь зарегистрирован");
        return true;
    }

    public void loadUsers() {
        try {
            if (Files.exists(Paths.get(usersFile))) {
                try (BufferedReader reader = new BufferedReader(new FileReader(usersFile))) {
                    Type userListType = new TypeToken<ArrayList<User>>() {}.getType();
                    users = gson.fromJson(reader, userListType);
                }
            }
        } catch (IOException e) {
            logger.error("Ошибка при загрузке пользователей", e);
            users = new ArrayList<>();
        }
    }

    private void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(usersFile))) {
            gson.toJson(users, writer);
        } catch (IOException e) {
            logger.error("Ошибка при сохранении пользователей", e);
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Ошибка при хэшировании пароля", e);
            return null;
        }
    }

    public void importUsers(String importFile) {
        try {
            if (Files.exists(Paths.get(importFile))) {
                try (BufferedReader reader = new BufferedReader(new FileReader(importFile))) {
                    Type userListType = new TypeToken<ArrayList<User>>() {}.getType();
                    List<User> importedUsers = gson.fromJson(reader, userListType);
                    for (User  user : importedUsers) {
                        if (users.stream().noneMatch(u -> u.getUsername().equals(user.getUsername()))) {
                            users.add(user);
                        }
                    }
                    saveUsers();
                    logger.info("Пользователи успешно импортированы из {}", importFile);
                }
            } else {
                logger.warn("Файл для импорта не найден: {}", importFile);
            }
        } catch (IOException e) {
            logger.error("Ошибка при импорте пользователей", e);
        }
    }
}
