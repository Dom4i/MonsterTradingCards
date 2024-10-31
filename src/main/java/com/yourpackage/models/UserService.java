package com.yourpackage.models;

import com.yourpackage.database.Database;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID; // Importiere für UUID
import java.sql.*;


public class UserService {

    public boolean createUserInDatabase(User user) {
        try (Connection conn = Database.getInstance().connect()) {
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
        try (Connection conn = Database.getInstance().connect()) {
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

    public User getUserFromDatabase(String username) {
        String sql = """
        SELECT u.id AS user_id, u.password, u.name AS user_name, u.bio, u.image, u.token, u.score, u.coins,
               c.card_id, c.name AS card_name, c.damage, c.element_type, c.card_type
        FROM users u
        LEFT JOIN user_packages up ON u.id = up.user_id
        LEFT JOIN cards c ON up.package_id = c.package_id
        WHERE u.username = ?
    """;

        try (Connection conn = Database.getInstance().connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            User user = null;
            while (rs.next()) {
                // Benutzerinformationen nur beim ersten Durchlauf abrufen und Benutzer erstellen
                if (user == null) {
                    UUID id = (UUID) rs.getObject("user_id");
                    String password = rs.getString("password");
                    String name = rs.getString("user_name"); // Benutzername mit Alias abrufen
                    String bio = rs.getString("bio");
                    String image = rs.getString("image");
                    String token = rs.getString("token");
                    int score = rs.getInt("score");
                    int coins = rs.getInt("coins");

                    user = new User(id, username, password, name, bio, image, coins, score, token);
                }

                // Karteninformationen abrufen und zur Liste hinzufügen
                String cardId = rs.getString("card_id");
                if (cardId != null) {
                    String cardName = rs.getString("card_name"); // Kartenname mit Alias abrufen
                    double damage = rs.getDouble("damage");
                    String elementType = rs.getString("element_type");
                    String cardType = rs.getString("card_type");

                    Card card;
                    if ("MONSTER".equals(cardType)) {
                        card = new MonsterCard(cardId, cardName, damage, elementType, cardType);
                    } else if ("SPELL".equals(cardType)) {
                        card = new SpellCard(cardId, cardName, damage, elementType, cardType);
                    } else {
                        continue;
                    }
                    user.addCardToStack(card); // Karte hinzufügen

                }
            }
            return user; // Gibt den Benutzer mit allen Karten zurück
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null; // Benutzer wurde nicht gefunden
    }




    public boolean buyPackageForUser(User user) {
        try (Connection conn = Database.getInstance().connect()) {
            // Überprüfen, ob der Benutzer existiert und genug Münzen hat
            String sql = "SELECT coins FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, user.getUsername());
                pstmt.setString(2, user.getPassword());
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) { // Benutzer gefunden
                    int coins = rs.getInt("coins");

                    // Verfügbares Paket auswählen (das erste, das verfügbar ist)
                    String selectPackageSql = """
                    SELECT package_id FROM packages 
                    WHERE is_available = TRUE 
                    AND package_id NOT IN (SELECT package_id FROM user_packages WHERE user_id = ?)
                    ORDER BY package_id ASC
                    LIMIT 1
                """;
                    UUID packageId = null;
                    try (PreparedStatement selectPackageStmt = conn.prepareStatement(selectPackageSql)) {
                        selectPackageStmt.setObject(1, user.getId());
                        ResultSet packageRs = selectPackageStmt.executeQuery();
                        if (packageRs.next()) {
                            packageId = (UUID) packageRs.getObject("package_id");
                        }
                    }

                    // Wenn ein Paket gefunden wurde, kaufe es
                    if (packageId != null) {
                        // Münzen abziehen
                        int updatedCoins = coins - 5;
                        String updateSql = "UPDATE users SET coins = ? WHERE username = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setInt(1, updatedCoins);
                            updateStmt.setString(2, user.getUsername());
                            updateStmt.executeUpdate();
                        }

                        // Paket zu den Benutzerpaketen hinzufügen
                        String insertUserPackageSql = "INSERT INTO user_packages (user_id, package_id) VALUES (?, ?)";
                        try (PreparedStatement insertUserPackageStmt = conn.prepareStatement(insertUserPackageSql)) {
                            insertUserPackageStmt.setObject(1, user.getId());
                            insertUserPackageStmt.setObject(2, packageId);
                            insertUserPackageStmt.executeUpdate();
                        }

                        // Setze das Paket auf nicht verfügbar
                        String updatePackageSql = "UPDATE packages SET is_available = FALSE WHERE package_id = ?";
                        try (PreparedStatement updatePackageStmt = conn.prepareStatement(updatePackageSql)) {
                            updatePackageStmt.setObject(1, packageId);
                            updatePackageStmt.executeUpdate();
                        }

                        return true; // Erfolgreich gekauft
                    } else {
                        return false; // Keine Pakete mehr verfügbar
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return false; // Benutzer nicht gefunden oder kein Paket verfügbar
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

        try (Connection conn = Database.getInstance().connect()) {
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
