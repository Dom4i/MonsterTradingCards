package com.yourpackage.database;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import java.sql.Connection;

public class DatabaseTest {

    @Test
    public void testConnection() {
        Connection conn = Database.connect();
        assertNotNull(conn, "Connection should not be null");
    }
}
