IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'TaskManagerDB')
BEGIN
    CREATE DATABASE TaskManagerDB;
END;
GO

USE TaskManagerDB;
GO

IF OBJECT_ID('dbo.tasks', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.tasks (
        id INT IDENTITY PRIMARY KEY,
        title NVARCHAR(150) NOT NULL,
        description NVARCHAR(500) NULL,
        status NVARCHAR(30) NOT NULL,
        created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        updated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME()
    );
END;
GO

INSERT INTO dbo.tasks (title, description, status)
VALUES
    (N'Revoir les bases Java', N'Relire la syntaxe, collections, exceptions.', N'En attente'),
    (N'Préparer fiches SQL', N'Revoir les jointures et index.', N'En cours'),
    (N'Exercices JavaFX', N'Créer un layout moderne.', N'En cours'),
    (N'Simulation orale', N'Préparer réponses aux questions fréquentes.', N'Validée'),
    (N'Planification révisions', N'Organiser le calendrier.', N'En attente'),
    (N'Relecture finale', N'Check-list des points faibles.', N'Annulée');
GO
