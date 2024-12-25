package com.chat.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
    private static final Logger logger = LoggerFactory.getLogger(ChatServer.class);
    private final UserManager userManager = new UserManager();
    private final ActionLogger actionLogger;
    private volatile boolean running = true;

    public ChatServer(ActionLogger actionLogger) {
        this.actionLogger = actionLogger;
    }

    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Сервер запущен на порту: {}", port);
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(new ClientHandler(clientSocket, userManager, actionLogger)).start();
                } catch (IOException e) {
                    logger.error("Ошибка при принятии соединения клиента", e);
                }
            }
        } catch (IOException e) {
            logger.error("Ошибка при запуске сервера", e);
        }
    }

    public void stop() {
        running = false;
    }
}
