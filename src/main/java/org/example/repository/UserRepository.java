package org.example.repository;

import java.util.List;
import java.util.Optional;
import org.example.model.AppUser;
import org.example.model.Role;

public interface UserRepository {
    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    AppUser createUser(AppUser user);

    List<AppUser> findAll();

    Optional<Long> findIdByUsername(String username);

    Optional<AppUser> findById(Long id);

    void setRole(Long userId, Role role);

    void setActive(Long userId, boolean active);
}
