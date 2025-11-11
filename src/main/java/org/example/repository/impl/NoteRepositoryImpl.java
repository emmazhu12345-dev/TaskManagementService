package org.example.repository.impl;

import org.example.dao.NoteDao;
import org.example.model.Note;
import org.example.repository.NoteRepository;
import org.jdbi.v3.core.Jdbi;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class NoteRepositoryImpl implements NoteRepository {

    private final Jdbi jdbi;

    public NoteRepositoryImpl(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public Page<Note> findByOwnerId(Long ownerId, Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        int offset = page * size;

        List<Note> content = jdbi.withExtension(NoteDao.class,
                dao -> dao.findByOwnerIdPaged(ownerId, size, offset));
        long total = jdbi.withExtension(NoteDao.class,
                dao -> dao.countByOwnerId(ownerId));

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Optional<Note> findByIdAndOwnerId(Long id, Long ownerId) {
        return jdbi.withExtension(NoteDao.class, dao -> dao.findByIdAndOwnerId(id, ownerId));
    }

    @Override
    public Note save(Note note) {
        if (note.getId() == null) {
            Long id = jdbi.withExtension(NoteDao.class, dao -> dao.insertReturnId(note));
            note.setId(id);
            return note;
        } else {
            // update owned record (ownerId required)
            int updated = jdbi.withExtension(NoteDao.class, dao -> dao.updateOwned(note));
            if (updated == 0) {
                throw new IllegalStateException("Update failed (not found or not owner).");
            }
            return note;
        }
    }

    @Override
    public void delete(Note note) {
        if (note.getId() == null || note.getOwnerId() == null) {
            throw new IllegalArgumentException("Note id and ownerId are required for delete.");
        }
        int deleted = jdbi.withExtension(NoteDao.class, dao -> dao.deleteOwned(note.getId(), note.getOwnerId()));
        if (deleted == 0) {
            throw new IllegalStateException("Delete failed (not found or not owner).");
        }
    }
}
