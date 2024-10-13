package com.yourpackage.models;

import com.yourpackage.database.Database;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID; // Importiere für UUID
import java.sql.*;


public class UserService {



    public boolean createUserInDatabase(User user) {
        try (Connection conn = Database.connect()) {
            String checkSql = "SELECT COUNT(*) FROM users WHERE username = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, user.getUsername());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return false; // Benutzer existiert bereits
                }
            }

            String sql = "INSERT INTO users (username, password, coins, score) VALUES (?, ?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(sql)) {
                insertStmt.setString(1, user.getUsername());
                insertStmt.setString(2, user.getPassword());
                insertStmt.setInt(3, user.getCoins());
                insertStmt.setInt(4, user.getScore());
                insertStmt.executeUpdate();
                return true; // Benutzer erfolgreich erstellt
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean loginUser(User user) {
        try (Connection conn = Database.connect()) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, user.getUsername());
                pstmt.setString(2, user.getPassword());
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) { // Benutzer gefunden
                    // Generiere einen neuen Token
                    //String token = UUID.randomUUID().toString(); So könnte man einen richtigen Token generieren,
                    String token = user.getUsername() + "-mtcgToken"; //hier reicht dieser hardcoded Token
                    // Speichere den Token in der Datenbank
                    String updateSql = "UPDATE users SET token = ? WHERE username = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setString(1, token);
                        updateStmt.setString(2, user.getUsername());
                        updateStmt.executeUpdate(); // Aktualisiere den Token in der DB
                    }
                    return true; // Erfolgreich eingeloggt
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return false; // Benutzer nicht gefunden
    }


    // Statische Methode, um einen Benutzer aus der Datenbank abzurufen
    public User getUserFromDatabase(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                UUID id = (UUID) rs.getObject("id"); // UUID von der Datenbank abrufen
                String password = rs.getString("password");
                String name = rs.getString("name");
                String bio = rs.getString("bio");
                String image = rs.getString("image");
                String token = rs.getString("token");
                int score = rs.getInt("score");
                int coins = rs.getInt("coins");

                // Erstelle ein neues User-Objekt und gebe es zurück
                return new User(id, username, password, name, bio, image, coins, score, token);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null; // Benutzer wurde nicht gefunden
    }

    public void updateUserData(User user, JsonNode jsonNode) {
        if (jsonNode.has("Name")) {
            user.setName(jsonNode.get("Name").asText());
        }
        if (jsonNode.has("Bio")) {
            user.setBio(jsonNode.get("Bio").asText());
        }
        if (jsonNode.has("Image")) {
            user.setImage(jsonNode.get("Image").asText());
        }

        try (Connection conn = Database.connect()) {
            String sql = "UPDATE users SET name = ?, bio = ?, image = ? WHERE username = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, user.getName());
                pstmt.setString(2, user.getBio());
                pstmt.setString(3, user.getImage());
                pstmt.setString(4, user.getUsername());
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String toJson(User user) {
        return "{"
                + "\"id\":\"" + user.getId() + "\", " // UUID des Benutzers
                + "\"username\":\"" + user.getUsername() + "\", "
                + "\"password\":\"" + user.getPassword() + "\", " // Achte auf Sicherheit
                + "\"coins\":" + user.getCoins() + ", "
                + "\"score\":" + user.getScore() + ", " // Angenommen, du hast eine Methode getScore()
                + "\"token\":\"" + user.getToken() + "\", " // Angenommen, du hast eine Methode getToken()
                + "\"name\":\"" + user.getName() + "\", "
                + "\"bio\":\"" + user.getBio() + "\", "
                + "\"image\":\"" + user.getImage() + "\""
                + "}";
    }


    public void addCardToStack(User user, Card card) {
        user.getCardStack().add(card);
    }

    public void addCardToDeck(User user, Card card) {
        if (user.getDeck().size() < 4) {
            user.getDeck().add(card);
        } else {
            System.out.println("Deck is full. Cannot add more cards.");
        }
    }
}
