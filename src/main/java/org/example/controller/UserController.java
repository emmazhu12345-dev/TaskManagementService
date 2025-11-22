package org.example.controller;

import org.example.model.AppUser;
import org.example.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Deprecated
public class UserController {

    private final UserService userService;

    // Constructor Injection (recommended)
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // POST /users → Create new user
    @PostMapping
    public AppUser createUser(@RequestBody AppUser user) {
        return userService.createUser(user);
    }
}
