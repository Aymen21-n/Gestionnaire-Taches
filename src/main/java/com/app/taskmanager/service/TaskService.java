package com.app.taskmanager.service;

import com.app.taskmanager.dao.TaskDao;
import com.app.taskmanager.dao.TaskDaoImpl;
import com.app.taskmanager.model.Task;

import java.util.List;

public class TaskService {
    public static final String STATUS_TOUTES = "Toutes";
    public static final String STATUS_ATTENTE = "En attente";
    public static final String STATUS_EN_COURS = "En cours";
    public static final String STATUS_VALIDEE = "Validée";
    public static final String STATUS_ANNULEE = "Annulée";

    private final TaskDao taskDao;

    public TaskService() {
        this(new TaskDaoImpl());
    }

    public TaskService(TaskDao taskDao) {
        this.taskDao = taskDao;
    }

    public List<Task> getAllTasks() {
        return taskDao.findAll();
    }

    public List<Task> getTasksByStatus(String status) {
        return taskDao.findByStatus(status);
    }

    public List<Task> search(String query, String statusOrNull) {
        if (query == null || query.isBlank()) {
            if (statusOrNull == null) {
                return taskDao.findAll();
            }
            return taskDao.findByStatus(statusOrNull);
        }
        return taskDao.search(query.trim(), statusOrNull);
    }

    public Task createTask(Task task) {
        validateTask(task);
        return taskDao.insert(task);
    }

    public void updateTask(Task task) {
        validateTask(task);
        taskDao.update(task);
    }

    public void deleteTask(int id) {
        taskDao.delete(id);
    }

    private void validateTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("La tâche est requise.");
        }
        if (task.getTitle() == null || task.getTitle().isBlank()) {
            throw new IllegalArgumentException("Le titre est obligatoire.");
        }
        if (task.getStatus() == null || task.getStatus().isBlank()) {
            throw new IllegalArgumentException("Le statut est obligatoire.");
        }
    }
}
