package com.chat;

import com.chat.server.ChatServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Launch_Server {
    private static final Logger logger = LoggerFactory.getLogger(Launch_Server.class);

    public static void main(String[] args) {
        ChatServer server = new ChatServer();

        // Ввод пользователя
        new Thread(() -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                System.out.println("Press Enter to stop the server...");
                reader.readLine(); // Ждем Enter
                server.stop();
            } catch (IOException e) {
                logger.error("Error reading input", e); // Лог ошибки
            }
        }).start();

        // Запускаем сервер на порту 8080
        server.start(8080);
    }
}
