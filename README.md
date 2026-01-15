# Gestionnaire de Tâches - Préparation Examens

Application JavaFX de gestion de tâches pour la préparation d'examens, connectée à SQL Server via JDBC.

## Prérequis
- Java 21+
- Maven 3.9+
- SQL Server (local ou distant)
- Pilote JDBC Microsoft SQL Server (géré par Maven)

## Initialisation de la base de données
1. Ouvrez SQL Server Management Studio (SSMS) ou Azure Data Studio.
2. Exécutez le script `db/init.sql` pour créer la base `TaskManagerDB`, la table `tasks` et insérer des données de démonstration.

## Configuration des identifiants
Modifiez `src/main/resources/application.properties` :

```
db.url=jdbc:sqlserver://localhost:1433;databaseName=TaskManagerDB;encrypt=true;trustServerCertificate=true

db.user=sa

db.password=yourStrong(!)Password
```

## Lancer l'application
Depuis la racine du projet :

```
mvn clean javafx:run
```

## Fonctionnalités
- CRUD complet sur les tâches via SQL Server.
- Recherche par titre ou description.
- Filtres par statut avec mise à jour immédiate.
- Statistiques en bas de page mises à jour en temps réel.
- Interface moderne et simple inspirée d'un tableau de gestion.

## Structure du projet
```
src/main/java/com/app/taskmanager/
  MainApp.java
  model/Task.java
  dao/TaskDao.java, TaskDaoImpl.java
  service/TaskService.java
  util/DBUtil.java
  ui/controller/MainController.java, TaskFormController.java
src/main/resources/
  fxml/main-view.fxml
  fxml/task-form.fxml
  css/styles.css
  application.properties
  logback.xml

db/init.sql
```
