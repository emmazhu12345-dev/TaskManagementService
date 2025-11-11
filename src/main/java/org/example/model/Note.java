package org.example.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Note entity representing a user-owned note.
 * Uses constructor-based initialization for cleaner creation,
 * but keeps a no-args constructor for JPA compatibility.
 */
@Getter
@Setter
@NoArgsConstructor // JPA requires a no-args constructor
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "text")
    private String content;

    private Long ownerId;       // store FK id directly (no ORM)

    private Instant createdAt = Instant.now();

    /**
     * Custom constructor for easy creation.
     * This is used in the service layer instead of setter chaining.
     */
    public Note(String title, String content, Long ownerId) {
        this.title = title;
        this.content = content;
        this.ownerId = ownerId;
        this.createdAt = Instant.now();
    }
}

