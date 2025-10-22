package org.example.service;

import org.example.model.User;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository repo;

    // ✅ Create user
    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public User createUser(User u) { return repo.save(u); }

    // ✅ Get all users
    public List<User> getAllUsers() { return repo.findAll(); }
}
