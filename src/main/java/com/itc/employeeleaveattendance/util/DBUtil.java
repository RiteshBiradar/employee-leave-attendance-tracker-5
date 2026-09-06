package com.itc.employeeleaveattendance.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DBUtil {

    private static final String URL;
    private static final String USERNAME;
    private static final String PASSWORD;

    static {
        try {
            Properties properties = new Properties();

            try (InputStream input = DBUtil.class
                    .getClassLoader()
                    .getResourceAsStream("db.properties")) {

                if (input == null) {
                    throw new RuntimeException(
                            "db.properties not found in classpath"
                    );
                }

                properties.load(input);
            }

            URL = properties.getProperty("db.url");
            USERNAME = properties.getProperty("db.username");
            PASSWORD = properties.getProperty("db.password");

            if (URL == null || USERNAME == null || PASSWORD == null) {
                throw new RuntimeException(
                        "Database configuration is incomplete"
                );
            }

            // Explicitly load the Oracle driver.
            // This is required in Tomcat environments where the WebappClassLoader
            // might not automatically register SPI drivers with the system DriverManager.
            Class.forName("oracle.jdbc.OracleDriver");

        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to load database configuration", e
            );
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "Oracle JDBC driver not found in classpath", e
            );
        }
    }

    private DBUtil() {
        // Prevent object creation
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                URL,
                USERNAME,
                PASSWORD
        );
    }
}