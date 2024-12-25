package com.chat.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.chat.model.User;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private final Socket clientSocket;
    private final UserManager userManager;
    private final ActionLogger actionLogger;

    public ClientHandler(Socket socket, UserManager userManager, ActionLogger actionLogger) {
        this.clientSocket = socket;
        this.userManager = userManager;
        this.actionLogger = actionLogger;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String message;
            while ((message = in.readLine()) != null) {
                logger.info("Received: {}", message);
                handleMessage(message, out);
            }
        } catch (IOException e) {
            logger.error("Error in client handler", e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.error("Error closing client socket", e);
            }
        }
    }

    private void handleMessage(String message, PrintWriter out) {
        try {
            JsonObject jsonMessage = JsonParser.parseString(message).getAsJsonObject();
            String action = jsonMessage.get("action").getAsString();
            String username = jsonMessage.get("username").getAsString();

            switch (action) {
                case "register":
                    String password = jsonMessage.get("password").getAsString();
                    userManager.registerUser (username, password);
                    actionLogger.logAction(username, "registered");
                    out.println("User  registered successfully.");
                    break;
                case "login":
                    password = jsonMessage.get("password").getAsString();
                    User user = userManager.getUser (username);
                    if (user != null && user.getPasswordHash().equals(hashPassword(password))) {
                        actionLogger.logAction(username, "logged in");
                        out.println("User  " + user.getUsername() + " logged in successfully.");
                    } else {
                        out.println("Invalid username or password.");
                    }
                    break;
                case "message":
                    String text = jsonMessage.get("text").getAsString();
                    actionLogger.logAction(username, "sent a message: " + text);
                    // Логика отправки сообщения другим пользователям
                    break;
                default:
                    out.println("Unknown action: " + action);
            }
        } catch (Exception e) {
            out.println("Error processing message: " + e.getMessage());
            logger.error("Error processing message", e);
        }
    }

    private String hashPassword(String password) {
        return password;
    }
}
