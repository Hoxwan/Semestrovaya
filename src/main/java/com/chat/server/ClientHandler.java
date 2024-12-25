package com.chat.server;

import java.io.*;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.chat.model.User;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private final Socket clientSocket;
    private final UserManager userManager;

    public ClientHandler(Socket socket, UserManager userManager) {
        this.clientSocket = socket;
        this.userManager = userManager;
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

            if ("register".equals(action)) {
                String username = jsonMessage.get("username").getAsString();
                String password = jsonMessage.get("password").getAsString();
                userManager.registerUser (username, password);
                out.println("User  registered successfully.");
            } else if ("login".equals(action)) {
                String username = jsonMessage.get("username").getAsString();
                String password = jsonMessage.get("password").getAsString();
                User user = userManager.getUser (username);
                if (user != null && user.getPasswordHash().equals(hashPassword(password))) {
                    // Добавлено условие для вывода имени пользователя
                    out.println("User  " + user.getUsername() + " logged in successfully.");
                } else {
                    out.println("Invalid username or password.");
                }
            } else {
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
