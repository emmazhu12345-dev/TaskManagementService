package org.example.repository;

import org.example.model.Task;

import java.util.List;
import java.util.Optional;

/**
 * Repository abstraction on top of TaskDao.
 */
public interface TaskRepository {

    Task createTask(Task task);

    List<Task> findTasksByOwner(long ownerId);

    Optional<Task> findTaskByIdAndOwner(long taskId, long ownerId);

    boolean updateTask(Task task);

    boolean deleteTask(long taskId, long ownerId);
}
