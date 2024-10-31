package com.yourpackage.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Database {
    protected static Database instance = null;
    private String url = "jdbc:postgresql://localhost:5432/MonsterTradingCard";
    private String user = "postgres";
    private String password = "12345";

    protected Database(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database("jdbc:postgresql://localhost:5432/MonsterTradingCard", "postgres", "12345");
        }
        return instance;
    }

    public Connection connect() {

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
            //System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
        return conn;
    }
}
