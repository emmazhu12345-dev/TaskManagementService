package org.example.service;

import org.example.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final Map<Long, User> userStore = new HashMap<>();
    private Long nextId = 1L;

    // ✅ Create user
    public User createUser(User user) {
        user.setId(nextId++);
        userStore.put(user.getId(), user);
        return user;
    }

    // ✅ Get all users
    public List<User> getAllUsers() {
        return new ArrayList<>(userStore.values());
    }
}
