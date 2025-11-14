package org.example.repository;

import org.example.dao.TaskDao;
import org.example.model.Task;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TaskRepositoryImpl implements TaskRepository {

    private final Jdbi jdbi;

    public TaskRepositoryImpl(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public Task createTask(Task task) {
        Long id = jdbi.withExtension(TaskDao.class, dao -> dao.insertTask(task));
        task.setId(id);
        return task;
    }

    @Override
    public List<Task> findTasksByOwner(long ownerId) {
        return jdbi.withExtension(TaskDao.class, dao -> dao.findTasksByOwner(ownerId));
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
}
