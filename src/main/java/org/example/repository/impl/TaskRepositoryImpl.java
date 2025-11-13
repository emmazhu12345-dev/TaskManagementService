package org.example.repository.impl;

import org.example.dao.TaskDao;
import org.example.model.Task;
import org.example.repository.TaskRepository;
import org.springframework.stereotype.Repository;

@Repository
public class TaskRepositoryImpl implements TaskRepository {

    private final TaskDao taskDao;

    public TaskRepositoryImpl(TaskDao taskDao) {
        this.taskDao = taskDao;
    }

    @Override
    public Task create(Task task) {
        long id = taskDao.insert(task);
        task.setId(id);
        return task;
    }
}
