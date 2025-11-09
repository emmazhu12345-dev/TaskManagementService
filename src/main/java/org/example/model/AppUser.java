package org.example.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jdbi.v3.core.mapper.reflect.ColumnName;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class AppUser {
    private Long id;
    private String username;
    private String email;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private Role role;          // ADMIN / MEMBER
    @ColumnName("is_active")
    private boolean active;           // <— maps is_active -> active
    private Instant createdAt;
    private Instant updatedAt;

    public AppUser(
            String username,
            String email,
            String passwordHash,
            Role role,
            boolean active
        ) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = active;
    }
}
