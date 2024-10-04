package com.yourpackage.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Database {

    private static final String URL = "jdbc:postgresql://localhost:5432/MonsterTradingCard";
    private static final String USER = "postgres";
    private static final String PASSWORD = "12345";

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
        return conn;
    }

}
