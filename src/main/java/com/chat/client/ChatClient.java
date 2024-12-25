package com.chat.client;

import java.io.*;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatClient {
    private static final Logger logger = LoggerFactory.getLogger(ChatClient.class);

    public void start(String host, int port) {
        try (Socket socket = new Socket(host, port)) {
            logger.info("Connected to server at {}:{}", host, port);
            // Чтение и отправка сообщений
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Чтение сообщений от пользователя и отправка на сервер
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            String userMessage;
            while ((userMessage = userInput.readLine()) != null) {
                out.println(userMessage);
                String serverResponse = in.readLine();
                logger.info("Server response: {}", serverResponse);
            }
        } catch (IOException e) {
            logger.error("Error connecting to server", e);
        }
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient();
        client.start("localhost", 8080);
    }
}
