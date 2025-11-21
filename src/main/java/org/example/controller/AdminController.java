package org.example.controller;

import java.util.List;
import org.example.model.AppUser;
import org.example.model.Note;
import org.example.model.Role;
import org.example.service.NoteService;
import org.example.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final NoteService noteService;

    public AdminController(UserService userService, NoteService noteService) {
        this.userService = userService;
        this.noteService = noteService;
    }

    public record UpdateRoleRequest(Role role, Long id) {}

    public record UpdateActiveRequest(boolean active) {}

    // Only Admin role can read all users
    @GetMapping("/users")
    public List<AppUser> users() {
        return userService.getAllUsers();
    }

    @PostMapping("/users/role")
    public void setRole(@RequestBody UpdateRoleRequest req) {
        // todo: HOMEWORK Solve no admin issue
        // 1. last admin, cannot update
        // or disallow admin downgrade themselves to member
        userService.setRole(req.id(), req.role());
    }

    @PostMapping("/users/{id}/active")
    public void setActive(@PathVariable Long id, @RequestBody UpdateActiveRequest req) {
        userService.setActive(id, req.active());
    }

    @GetMapping("/notes")
    public Page<Note> listAll(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return noteService.listNotes(PageRequest.of(page, size));
    }
}
