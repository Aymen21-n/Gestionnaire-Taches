package com.app.taskmanager.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(DBUtil.class);
    private static final String PROPERTIES_FILE = "/application.properties";

    private static final String url;
    private static final String user;
    private static final String password;

    static {
        Properties properties = new Properties();
        try (InputStream input = DBUtil.class.getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                throw new IllegalStateException("application.properties introuvable dans les ressources.");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new ExceptionInInitializerError("Impossible de charger application.properties: " + e.getMessage());
        }

        url = properties.getProperty("db.url");
        user = properties.getProperty("db.user");
        password = properties.getProperty("db.password");
    }

    private DBUtil() {
    }

    public static Connection getConnection() throws SQLException {
        LOGGER.debug("Ouverture d'une connexion SQL Server");
        return DriverManager.getConnection(url, user, password);
    }
}
