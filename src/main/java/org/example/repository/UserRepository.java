package org.example.repository;

import org.example.model.AppUser;
import java.util.List;
import java.util.Optional;


public interface UserRepository {
    Optional<AppUser> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    AppUser createUser(AppUser user);
    List<AppUser> findAll();
    Optional<Long> findIdByUsername(String username);
}