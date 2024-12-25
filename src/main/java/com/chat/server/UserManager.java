package com.chat.server;

import com.chat.model.User;
import com.chat.exceptions.UserException;

import java.util.HashMap;
import java.util.Map;

public class UserManager {
    private final Map<String, User> users = new HashMap<>();

    public void registerUser (String username, String password) throws UserException {
        if (users.containsKey(username)) {
            throw new UserException("User  already exists");
        }
        String passwordHash = hashPassword(password);
        users.put(username, new User(username, passwordHash));
    }

    private String hashPassword(String password) {
        return password;
    }

    public User getUser (String username) {
        return users.get(username);
    }
}
