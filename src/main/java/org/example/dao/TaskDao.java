package org.example.dao;

import org.example.model.Task;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

/**
 * DAO layer for raw SQL operations on the task table.
 */
@RegisterBeanMapper(Task.class)
public interface TaskDao {

    @SqlUpdate("""
        INSERT INTO task (owner_id, title, description, status, priority, due_date)
        VALUES (:ownerId, :title, :description, :status, :priority, :dueDate)
    """)
    @GetGeneratedKeys("id")
    long insertTask(@BindBean Task task);

    // 1. find the current page for user
    @SqlQuery("""
        SELECT *
        FROM task
        WHERE owner_id = :ownerId
        ORDER BY created_at DESC
        LIMIT :limit OFFSET :offset
    """)
    List<Task> findTasksByOwnerPaged(
            @Bind("ownerId") long ownerId,
            @Bind("limit") int limit,
            @Bind("offset") int offset
    );

    // ✅ 2. find the total number of task for the user
    @SqlQuery("""
        SELECT COUNT(*)
        FROM task
        WHERE owner_id = :ownerId
    """)
    long countTasksByOwner(@Bind("ownerId") long ownerId);

    @SqlQuery("""
        SELECT *
        FROM task
        WHERE id = :taskId AND owner_id = :ownerId
    """)
    Optional<Task> findTaskByIdAndOwner(
            @Bind("taskId") long taskId,
            @Bind("ownerId") long ownerId);

    @SqlUpdate("""
        UPDATE task
        SET title       = :title,
            description = :description,
            status      = :status,
            priority    = :priority,
            due_date    = :dueDate,
            updated_at  = :updatedAt
        WHERE id = :id AND owner_id = :ownerId
    """)
    int updateTask(@BindBean Task task);

    @SqlUpdate("""
        DELETE FROM task
        WHERE id = :taskId AND owner_id = :ownerId
    """)
    int deleteTaskByIdAndOwner(
            @Bind("taskId") long taskId,
            @Bind("ownerId") long ownerId);
}
