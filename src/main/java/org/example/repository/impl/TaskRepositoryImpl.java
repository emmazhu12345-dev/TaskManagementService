package org.example.repository.impl;

import java.util.List;
import java.util.Optional;
import org.example.dao.TaskDao;
import org.example.dto.PagedResponse;
import org.example.model.Task;
import org.example.repository.TaskRepository;
import org.jdbi.v3.core.Jdbi;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class TaskRepositoryImpl implements TaskRepository {

    private final Jdbi jdbi;

    public TaskRepositoryImpl(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public Task createTask(Task task) {
        long id = jdbi.withExtension(TaskDao.class, dao -> dao.insertTask(task));
        task.setId(id);
        return task;
    }

    @Override
    public PagedResponse<Task> findTasksByOwner(long ownerId, Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        int offset = page * size;

        // 1. find the current page
        List<Task> tasks = jdbi.withExtension(TaskDao.class, dao -> dao.findTasksByOwnerPaged(ownerId, size, offset));

        // 2. find the total number of tasks
        long totalElements = jdbi.withExtension(TaskDao.class, dao -> dao.countTasksByOwner(ownerId));

        int totalPages = (size == 0) ? 0 : (int) ((totalElements + size - 1) / size); // 向上取整

        boolean hasNext = page + 1 < totalPages;
        boolean hasPrevious = page > 0;

        return new PagedResponse<>(tasks, page, size, totalElements, totalPages, hasNext, hasPrevious);
    }

    @Override
    public Optional<Task> findTaskByIdAndOwner(long taskId, long ownerId) {
        return jdbi.withExtension(TaskDao.class, dao -> dao.findTaskByIdAndOwner(taskId, ownerId));
    }

    @Override
    public boolean updateTask(Task task) {
        int rows = jdbi.withExtension(TaskDao.class, dao -> dao.updateTask(task));
        return rows > 0;
    }

    @Override
    public boolean deleteTask(long taskId, long ownerId) {
        int rows = jdbi.withExtension(TaskDao.class, dao -> dao.deleteTaskByIdAndOwner(taskId, ownerId));
        return rows > 0;
    }

    @Override
    public List<Task> findTasksForTomorrow(long ownerId, java.time.Instant tomorrowEnd) {
        return jdbi.withExtension(TaskDao.class, dao -> dao.findTasksForTomorrow(ownerId, tomorrowEnd));
    }

    @Override
    public List<Task> findOpenTasksByOwner(long ownerId) {
        return jdbi.withExtension(TaskDao.class, dao -> dao.findOpenTasksByOwner(ownerId));
    }
}
