package org.example.repository;

import java.util.List;
import java.util.Optional;
import org.example.dto.PagedResponse;
import org.example.model.Task;
import org.springframework.data.domain.Pageable;

/** Repository abstraction on top of TaskDao. */
public interface TaskRepository {

    Task createTask(Task task);

    PagedResponse<Task> findTasksByOwner(long ownerId, Pageable pageable);

    Optional<Task> findTaskByIdAndOwner(long taskId, long ownerId);

    boolean updateTask(Task task);

    boolean deleteTask(long taskId, long ownerId);

    List<Task> findTasksForTomorrow(long ownerId, java.time.Instant tomorrowEnd);

    List<Task> findOpenTasksByOwner(long ownerId);
}
