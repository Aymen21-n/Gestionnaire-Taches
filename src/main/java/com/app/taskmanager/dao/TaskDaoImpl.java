package com.app.taskmanager.dao;

import com.app.taskmanager.model.Task;
import com.app.taskmanager.util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskDaoImpl implements TaskDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskDaoImpl.class);

    @Override
    public List<Task> findAll() {
        String sql = "SELECT id, title, description, status, created_at, updated_at FROM tasks ORDER BY created_at DESC";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return mapTasks(resultSet);
        } catch (SQLException e) {
            LOGGER.error("Erreur lors du chargement des tâches", e);
            throw new RuntimeException("Impossible de charger les tâches.", e);
        }
    }

    @Override
    public List<Task> findByStatus(String status) {
        String sql = "SELECT id, title, description, status, created_at, updated_at FROM tasks WHERE status = ? ORDER BY created_at DESC";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapTasks(resultSet);
            }
        } catch (SQLException e) {
            LOGGER.error("Erreur lors du chargement des tâches par statut", e);
            throw new RuntimeException("Impossible de charger les tâches filtrées.", e);
        }
    }

    @Override
    public List<Task> search(String query, String statusOrNull) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, title, description, status, created_at, updated_at FROM tasks WHERE (LOWER(title) LIKE ? OR LOWER(COALESCE(description, '')) LIKE ?)");
        if (statusOrNull != null) {
            sql.append(" AND status = ?");
        }
        sql.append(" ORDER BY created_at DESC");

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            String term = "%" + query.toLowerCase() + "%";
            statement.setString(1, term);
            statement.setString(2, term);
            if (statusOrNull != null) {
                statement.setString(3, statusOrNull);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapTasks(resultSet);
            }
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la recherche de tâches", e);
            throw new RuntimeException("Impossible de rechercher les tâches.", e);
        }
    }

    @Override
    public Task insert(Task task) {
        String sql = "INSERT INTO tasks (title, description, status, created_at, updated_at) VALUES (?, ?, ?, SYSDATETIME(), SYSDATETIME())";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, task.getTitle());
            statement.setString(2, task.getDescription());
            statement.setString(3, task.getStatus());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    task.setId(keys.getInt(1));
                }
            }
            task.setCreatedAt(LocalDateTime.now());
            task.setUpdatedAt(LocalDateTime.now());
            return task;
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de l'insertion de la tâche", e);
            throw new RuntimeException("Impossible d'ajouter la tâche.", e);
        }
    }

    @Override
    public void update(Task task) {
        String sql = "UPDATE tasks SET title = ?, description = ?, status = ?, updated_at = SYSDATETIME() WHERE id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, task.getTitle());
            statement.setString(2, task.getDescription());
            statement.setString(3, task.getStatus());
            statement.setInt(4, task.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la mise à jour de la tâche", e);
            throw new RuntimeException("Impossible de mettre à jour la tâche.", e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Erreur lors de la suppression de la tâche", e);
            throw new RuntimeException("Impossible de supprimer la tâche.", e);
        }
    }

    private List<Task> mapTasks(ResultSet resultSet) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        while (resultSet.next()) {
            Task task = new Task();
            task.setId(resultSet.getInt("id"));
            task.setTitle(resultSet.getString("title"));
            task.setDescription(resultSet.getString("description"));
            task.setStatus(resultSet.getString("status"));
            Timestamp created = resultSet.getTimestamp("created_at");
            if (created != null) {
                task.setCreatedAt(created.toLocalDateTime());
            }
            Timestamp updated = resultSet.getTimestamp("updated_at");
            if (updated != null) {
                task.setUpdatedAt(updated.toLocalDateTime());
            }
            tasks.add(task);
        }
        return tasks;
    }
}
