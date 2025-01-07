package com.yourpackage.models;

import com.yourpackage.database.Database;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {

    @Test
    void testCreateCard() {
        // Arrange: Erstellen eines Test-PackageId und einer Testkarte
        UUID packageId = UUID.randomUUID(); // Zufälliges Package-ID
        String testCardId = UUID.randomUUID().toString(); // Zufälliges Card-ID
        Card testCard = new MonsterCard(testCardId, "TestMonster", 25.5, "FIRE", "MONSTER");

        // Package in die Datenbank einfügen
        try (Connection conn = Database.getInstance().connect()) {
            String insertPackageSql = "INSERT INTO packages (package_id) VALUES (?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertPackageSql)) {
                stmt.setObject(1, packageId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            fail("Failed to insert test package: " + e.getMessage());
        }

        // Act: Karte in die Datenbank einfügen
        boolean creationSuccess = testCard.createCard(packageId);

        // Assert: Überprüfen, ob die Karte in der Datenbank vorhanden ist
        assertTrue(creationSuccess, "Card should be created successfully.");
        try (Connection conn = Database.getInstance().connect()) {
            String query = "SELECT * FROM cards WHERE card_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, testCardId);
                try (ResultSet rs = stmt.executeQuery()) {
                    assertTrue(rs.next(), "Card should exist in the database.");
                    assertEquals("TestMonster", rs.getString("name"), "Card name should match.");
                    assertEquals(25.5, rs.getDouble("damage"), "Card damage should match.");
                    assertEquals("FIRE", rs.getString("element_type"), "Card element type should match.");
                    assertEquals("MONSTER", rs.getString("card_type"), "Card type should match.");
                }
            }
        } catch (SQLException e) {
            fail("Database query failed: " + e.getMessage());
        }

        // Cleanup: Karte und Package aus der Datenbank löschen
        try (Connection conn = Database.getInstance().connect()) {
            // Karte löschen
            String deleteCardSql = "DELETE FROM cards WHERE card_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteCardSql)) {
                stmt.setString(1, testCardId);
                stmt.executeUpdate();
            }
            // Package löschen
            String deletePackageSql = "DELETE FROM packages WHERE package_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deletePackageSql)) {
                stmt.setObject(1, packageId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            fail("Failed to clean up test data: " + e.getMessage());
        }
    }
}