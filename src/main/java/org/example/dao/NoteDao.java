package org.example.dao;

import org.example.model.Note;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.*;
import java.util.*;

@RegisterBeanMapper(Note.class)
public interface NoteDao {

    @SqlUpdate("""
    INSERT INTO note(title, content, owner_id, created_at)
    VALUES(:title, :content, :ownerId, :createdAt)
  """)
    @GetGeneratedKeys("id")
    Long insertReturnId(@BindBean Note n);

    @SqlQuery("""
    SELECT * FROM note
    WHERE owner_id = :ownerId
    ORDER BY id DESC
    LIMIT :limit OFFSET :offset
  """)
    List<Note> findByOwnerIdPaged(@Bind("ownerId") Long ownerId,
                                  @Bind("limit") int limit,
                                  @Bind("offset") int offset);

    @SqlQuery("""
    SELECT COUNT(*) FROM note WHERE owner_id = :ownerId
  """)
    long countByOwnerId(@Bind("ownerId") Long ownerId);

    @SqlQuery("""
    SELECT * FROM note WHERE id = :id AND owner_id = :ownerId
  """)
    Optional<Note> findByIdAndOwnerId(@Bind("id") Long id, @Bind("ownerId") Long ownerId);

    @SqlUpdate("""
    UPDATE note SET title = :title, content = :content
    WHERE id = :id AND owner_id = :ownerId
  """)
    int updateOwned(@BindBean Note n);

    @SqlUpdate("""
    DELETE FROM note WHERE id = :id AND owner_id = :ownerId
  """)
    int deleteOwned(@Bind("id") Long id, @Bind("ownerId") Long ownerId);

    @SqlQuery("""
    SELECT * FROM note
    ORDER BY id DESC
    LIMIT :limit OFFSET :offset
  """)
    List<Note> findAllPaged(@Bind("limit") int limit, @Bind("offset") int offset);

    @SqlQuery("SELECT COUNT(*) FROM note")
    long countAll();
}
