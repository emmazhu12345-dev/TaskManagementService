package org.example.repository.impl;

import org.example.dao.AppUserDao;
import org.example.dao.TaskDao;
import org.example.model.Task;
import org.example.repository.TaskRepository;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Repository;

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
}
