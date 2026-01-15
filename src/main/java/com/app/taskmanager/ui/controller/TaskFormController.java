package com.app.taskmanager.ui.controller;

import com.app.taskmanager.model.Task;
import com.app.taskmanager.service.TaskService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;

public class TaskFormController {
    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private ComboBox<String> statusCombo;

    private TaskService taskService;
    private Task task;
    private Runnable onSave;

    @FXML
    public void initialize() {
        statusCombo.setItems(FXCollections.observableArrayList(
                TaskService.STATUS_ATTENTE,
                TaskService.STATUS_EN_COURS,
                TaskService.STATUS_VALIDEE,
                TaskService.STATUS_ANNULEE
        ));
        statusCombo.getSelectionModel().selectFirst();
    }

    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    public void setTask(Task task) {
        this.task = task;
        if (task != null) {
            titleField.setText(task.getTitle());
            descriptionArea.setText(task.getDescription());
            statusCombo.getSelectionModel().select(task.getStatus());
        }
    }

    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (taskService == null) {
            showError("Service indisponible", "Le service n'est pas prêt.");
            return;
        }

        String title = titleField.getText();
        String description = descriptionArea.getText();
        String status = statusCombo.getValue();

        try {
            if (task == null) {
                Task newTask = new Task();
                newTask.setTitle(title);
                newTask.setDescription(description);
                newTask.setStatus(status);
                taskService.createTask(newTask);
            } else {
                task.setTitle(title);
                task.setDescription(description);
                task.setStatus(status);
                taskService.updateTask(task);
            }
            if (onSave != null) {
                onSave.run();
            }
            closeWindow();
        } catch (RuntimeException e) {
            showError("Erreur", e.getMessage());
        }
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(title);
        alert.showAndWait();
    }
}
