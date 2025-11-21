package org.example.repository;

import java.util.Optional;
import org.example.model.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NoteRepository {
    Page<Note> findByOwnerId(Long ownerId, Pageable pageable);

    Optional<Note> findByIdAndOwnerId(Long id, Long ownerId);

    Note save(Note note);

    void delete(Note note);

    // Admin use case: list all notes paged
    Page<Note> findAll(Pageable pageable);
}
