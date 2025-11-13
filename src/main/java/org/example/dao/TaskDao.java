// src/main/java/org/example/dao/TaskDao.java
package org.example.dao;

import org.example.model.Task;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterBeanMapper(Task.class)
public interface TaskDao {

    @SqlUpdate("""
        INSERT INTO task (owner_id, title, description, status, priority, due_date)
        VALUES (:ownerId, :title, :description, :status, :priority, :dueDate)
    """)
    @GetGeneratedKeys("id")
    long insert(@BindBean Task task);
}
