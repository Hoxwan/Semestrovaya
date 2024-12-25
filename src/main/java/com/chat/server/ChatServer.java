package com.chat.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatServer {
    private static final Logger logger = LoggerFactory.getLogger(ChatServer.class);
    private final UserManager userManager = new UserManager();
    private volatile boolean running = true;

    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Server started on port: {}", port);
            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(new ClientHandler(clientSocket, userManager)).start();
                } catch (IOException e) {
                    logger.error("Error accepting client connection", e);

                }
            }
        } catch (IOException e) {
            logger.error("Error starting server", e);
        }
    }

    // Метод для остановки сервера
    public void stop() {
        running = false;
    }
}
