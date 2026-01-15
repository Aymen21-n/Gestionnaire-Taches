package com.app.taskmanager.dao;

import com.app.taskmanager.model.Task;

import java.util.List;

public interface TaskDao {
    List<Task> findAll();

    List<Task> findByStatus(String status);

    List<Task> search(String query, String statusOrNull);

    Task insert(Task task);

    void update(Task task);

    void delete(int id);
}
