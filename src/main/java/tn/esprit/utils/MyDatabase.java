package tn.esprit.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDatabase {

    private final String url = "jdbc:mysql://localhost:3306/agriculture_db";
    private final String user = "root";
    private final String password = "";

    private Connection connection;

    private static MyDatabase instance;

    private MyDatabase() {
        try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Connected to database!");
        } catch (SQLException e) {
            System.out.println("❌ Database connection failed!");
            System.out.println(e.getMessage());
        }
    }

    public static MyDatabase getInstance() {
        if (instance == null) {
            instance = new MyDatabase();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
