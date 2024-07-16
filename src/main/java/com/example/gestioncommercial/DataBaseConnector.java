package com.example.gestioncommercial;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBaseConnector {
    private final String url = "jdbc:mysql://localhost:3306/gestioncommercial";
    private final String user = "root";
    private final String password = "my-secret-pw";
    private Connection connection;

    public DataBaseConnector() {
    }

    public Connection getConnection() {
        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
