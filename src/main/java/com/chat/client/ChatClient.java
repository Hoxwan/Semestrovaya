package com.chat.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

public class ChatClient {
    private static final Logger logger = LoggerFactory.getLogger(ChatClient.class);

    public void start(String host, int port) {
        try (Socket socket = new Socket(host, port)) {
            logger.info("Подключено к серверу по адресу {}:{}", host, port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

            String userMessage;
            while ((userMessage = userInput.readLine()) != null) {
                out.println(userMessage);
                String serverResponse = in.readLine();
                logger.info("Ответ сервера: {}", serverResponse);
            }
        } catch (IOException e) {
            logger.error("Ошибка при подключении к серверу", e);
        }
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient();
        client.start("localhost", 8080);
    }
}
