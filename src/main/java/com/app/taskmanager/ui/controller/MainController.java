package com.app.taskmanager.ui.controller;

import com.app.taskmanager.model.Task;
import com.app.taskmanager.service.TaskService;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class MainController {
    @FXML
    private TextField searchField;
    @FXML
    private Button addButton;
    @FXML
    private ToggleButton filterAllButton;
    @FXML
    private ToggleButton filterPendingButton;
    @FXML
    private ToggleButton filterInProgressButton;
    @FXML
    private ToggleButton filterValidatedButton;
    @FXML
    private ToggleButton filterCanceledButton;

    @FXML
    private TableView<Task> tasksTable;
    @FXML
    private TableColumn<Task, Integer> idColumn;
    @FXML
    private TableColumn<Task, String> titleColumn;
    @FXML
    private TableColumn<Task, String> descriptionColumn;
    @FXML
    private TableColumn<Task, String> statusColumn;
    @FXML
    private TableColumn<Task, String> createdColumn;

    @FXML
    private Button markInProgressButton;
    @FXML
    private Button validateButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Label statsLabel;

    private final TaskService taskService = new TaskService();
    private final ObservableList<Task> tasks = FXCollections.observableArrayList();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.FRENCH);

    private String currentStatusFilter = null;

    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        setupSelectionBindings();
        setupSearch();
        refreshTasks();
    }

    private void setupTable() {
        tasksTable.setItems(tasks);
        idColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("description"));
        statusColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));
        createdColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() == null) {
                return new javafx.beans.property.SimpleStringProperty("");
            }
            return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCreatedAt().format(formatter));
        });

        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("status-attente", "status-encours", "status-validee", "status-annulee");
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    switch (item) {
                        case TaskService.STATUS_ATTENTE -> getStyleClass().add("status-attente");
                        case TaskService.STATUS_EN_COURS -> getStyleClass().add("status-encours");
                        case TaskService.STATUS_VALIDEE -> getStyleClass().add("status-validee");
                        case TaskService.STATUS_ANNULEE -> getStyleClass().add("status-annulee");
                        default -> {
                        }
                    }
                }
            }
        });

        tasksTable.setRowFactory(tableView -> {
            TableRow<Task> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openTaskDialog(row.getItem());
                }
            });
            MenuItem editItem = new MenuItem("Modifier");
            editItem.setOnAction(event -> openTaskDialog(row.getItem()));
            MenuItem deleteItem = new MenuItem("Supprimer");
            deleteItem.setOnAction(event -> handleDelete(row.getItem()));
            ContextMenu menu = new ContextMenu(editItem, deleteItem);
            row.contextMenuProperty().bind(Bindings.when(row.emptyProperty()).then((ContextMenu) null).otherwise(menu));
            return row;
        });
    }

    private void setupFilters() {
        ToggleGroup group = new ToggleGroup();
        filterAllButton.setToggleGroup(group);
        filterPendingButton.setToggleGroup(group);
        filterInProgressButton.setToggleGroup(group);
        filterValidatedButton.setToggleGroup(group);
        filterCanceledButton.setToggleGroup(group);
        filterAllButton.setSelected(true);

        group.selectedToggleProperty().addListener((obs, old, selected) -> {
            if (selected == null) {
                filterAllButton.setSelected(true);
                currentStatusFilter = null;
            } else if (selected == filterPendingButton) {
                currentStatusFilter = TaskService.STATUS_ATTENTE;
            } else if (selected == filterInProgressButton) {
                currentStatusFilter = TaskService.STATUS_EN_COURS;
            } else if (selected == filterValidatedButton) {
                currentStatusFilter = TaskService.STATUS_VALIDEE;
            } else if (selected == filterCanceledButton) {
                currentStatusFilter = TaskService.STATUS_ANNULEE;
            } else {
                currentStatusFilter = null;
            }
            refreshTasks();
        });
    }

    private void setupSelectionBindings() {
        markInProgressButton.disableProperty().bind(tasksTable.getSelectionModel().selectedItemProperty().isNull());
        validateButton.disableProperty().bind(tasksTable.getSelectionModel().selectedItemProperty().isNull());
        cancelButton.disableProperty().bind(tasksTable.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.disableProperty().bind(tasksTable.getSelectionModel().selectedItemProperty().isNull());
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, old, value) -> refreshTasks());
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        openTaskDialog(null);
    }

    @FXML
    private void handleMarkInProgress(ActionEvent event) {
        updateSelectedStatus(TaskService.STATUS_EN_COURS);
    }

    @FXML
    private void handleValidate(ActionEvent event) {
        updateSelectedStatus(TaskService.STATUS_VALIDEE);
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        updateSelectedStatus(TaskService.STATUS_ANNULEE);
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        Task selected = tasksTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            handleDelete(selected);
        }
    }

    private void handleDelete(Task task) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer cette tâche ?", ButtonType.OK, ButtonType.CANCEL);
        alert.setHeaderText("Confirmation");
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    taskService.deleteTask(task.getId());
                    refreshTasks();
                } catch (RuntimeException e) {
                    showError("Suppression impossible", e.getMessage());
                }
            }
        });
    }

    private void updateSelectedStatus(String status) {
        Task selected = tasksTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        selected.setStatus(status);
        try {
            taskService.updateTask(selected);
            refreshTasks();
        } catch (RuntimeException e) {
            showError("Mise à jour impossible", e.getMessage());
        }
    }

    private void refreshTasks() {
        try {
            String query = searchField.getText();
            List<Task> loaded = taskService.search(query, currentStatusFilter);
            tasks.setAll(loaded);
            updateStats(loaded);
        } catch (RuntimeException e) {
            showError("Erreur", e.getMessage());
        }
    }

    private void updateStats(List<Task> list) {
        long total = list.size();
        long pending = list.stream().filter(task -> TaskService.STATUS_ATTENTE.equals(task.getStatus())).count();
        long inProgress = list.stream().filter(task -> TaskService.STATUS_EN_COURS.equals(task.getStatus())).count();
        long validated = list.stream().filter(task -> TaskService.STATUS_VALIDEE.equals(task.getStatus())).count();
        long canceled = list.stream().filter(task -> TaskService.STATUS_ANNULEE.equals(task.getStatus())).count();
        statsLabel.setText(String.format("Total %d | En attente %d | En cours %d | Validées %d | Annulées %d",
                total, pending, inProgress, validated, canceled));
    }

    private void openTaskDialog(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/task-form.fxml"));
            Parent root = loader.load();
            TaskFormController controller = loader.getController();
            controller.setTaskService(taskService);
            controller.setTask(task);
            controller.setOnSave(this::refreshTasks);

            Stage stage = new Stage();
            stage.setTitle(task == null ? "Ajouter une tâche" : "Modifier la tâche");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) {
            showError("Erreur", "Impossible d'ouvrir le formulaire.");
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(title);
        alert.showAndWait();
    }
}
