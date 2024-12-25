package com.chat.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class ActionLogger {
    private static final String LOG_FILE = "user_actions.json";
    private static final Logger logger = LoggerFactory.getLogger(ActionLogger.class);
    private final Gson gson;

    public ActionLogger() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void logAction(String username, String action) {
        UserAction userAction = new UserAction(username, action, LocalDateTime.now());
        try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
            writer.write(gson.toJson(userAction) + "\n");
        } catch (IOException e) {
            logger.error("Error logging action", e);
        }
    }

    private static class UserAction {

        public UserAction(String username, String action, LocalDateTime timestamp) {
        }
    }
}
