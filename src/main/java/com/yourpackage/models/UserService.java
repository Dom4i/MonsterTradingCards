package com.yourpackage.models;

import com.yourpackage.database.Database;
import com.fasterxml.jackson.databind.JsonNode;

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
        String sqlUserCards = """
    SELECT u.id AS user_id, u.password, u.name AS user_name, u.bio, u.image, u.token, u.score, u.coins,
           c.card_id, c.name AS card_name, c.damage, c.element_type, c.card_type
    FROM users u
    LEFT JOIN user_cards uc ON u.id = uc.user_id
    LEFT JOIN cards c ON uc.card_id = c.card_id
    WHERE u.username = ?
    """;

        String sqlDeckCards = """
    SELECT u.id AS user_id, u.password, u.name AS user_name, u.bio, u.image, u.token, u.score, u.coins,
           c.card_id, c.name AS card_name, c.damage, c.element_type, c.card_type
    FROM users u
    LEFT JOIN deck d ON u.id = d.user_id
    LEFT JOIN cards c ON d.card_id = c.card_id
    WHERE u.username = ?
    LIMIT 4
    """;

        try (Connection conn = Database.getInstance().connect()) {
            // Erste Abfrage: Alle Karten des Benutzers aus der user_cards-Tabelle holen
            User user = null;
            try (PreparedStatement pstmtUserCards = conn.prepareStatement(sqlUserCards)) {
                pstmtUserCards.setString(1, username);
                try (ResultSet rsUserCards = pstmtUserCards.executeQuery()) {

                    while (rsUserCards.next()) {
                        // Benutzerinformationen nur beim ersten Durchlauf abrufen und Benutzer erstellen
                        if (user == null) {
                            UUID id = (UUID) rsUserCards.getObject("user_id");
                            String password = rsUserCards.getString("password");
                            String name = rsUserCards.getString("user_name"); // Benutzername mit Alias abrufen
                            String bio = rsUserCards.getString("bio");
                            String image = rsUserCards.getString("image");
                            String token = rsUserCards.getString("token");
                            int score = rsUserCards.getInt("score");
                            int coins = rsUserCards.getInt("coins");

                            user = new User(id, username, password, name, bio, image, coins, score, token);
                        }

                        // Karteninformationen aus der user_cards-Tabelle abrufen und zur Liste hinzufügen
                        String cardId = rsUserCards.getString("card_id");
                        if (cardId != null) {
                            String cardName = rsUserCards.getString("card_name");
                            double damage = rsUserCards.getDouble("damage");
                            String elementType = rsUserCards.getString("element_type");
                            String cardType = rsUserCards.getString("card_type");

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
                } catch (SQLException e) {
                    // Fehler beim Abrufen der Benutzerkarten
                    System.err.println("Fehler beim Abrufen der Benutzerkarten: " + e.getMessage());
                    return null;
                }

                // Zweite Abfrage: Karten aus dem Deck holen
                try (PreparedStatement pstmtDeckCards = conn.prepareStatement(sqlDeckCards)) {
                    pstmtDeckCards.setString(1, username);
                    try (ResultSet rsDeckCards = pstmtDeckCards.executeQuery()) {

                        // Die Deckkarten hinzufügen
                        while (rsDeckCards.next()) {
                            String cardId = rsDeckCards.getString("card_id");
                            if (cardId != null) {
                                String cardName = rsDeckCards.getString("card_name");
                                double damage = rsDeckCards.getDouble("damage");
                                String elementType = rsDeckCards.getString("element_type");
                                String cardType = rsDeckCards.getString("card_type");

                                Card card;
                                if ("MONSTER".equals(cardType)) {
                                    card = new MonsterCard(cardId, cardName, damage, elementType, cardType);
                                } else if ("SPELL".equals(cardType)) {
                                    card = new SpellCard(cardId, cardName, damage, elementType, cardType);
                                } else {
                                    continue;
                                }
                                user.addCardToDeck(card);
                            }
                        }
                    } catch (SQLException e) {
                        // Fehler beim Abrufen der Deckkarten
                        System.err.println("Fehler beim Abrufen der Deckkarten: " + e.getMessage());
                        return null;
                    }
                } catch (SQLException e) {
                    System.err.println("Fehler beim Vorbereiten der Deck-Abfrage: " + e.getMessage());
                    return null;
                }

            } catch (SQLException e) {
                // Fehler beim Vorbereiten der Benutzerkarten-Abfrage
                System.err.println("Fehler beim Vorbereiten der Benutzerkarten-Abfrage: " + e.getMessage());
                return null;
            }

            return user; // Gibt den Benutzer mit allen Karten aus der user_cards- und deck-Tabelle zurück

        } catch (SQLException e) {
            // Fehler beim Verbindungsaufbau zur Datenbank
            System.err.println("Fehler beim Verbindungsaufbau zur Datenbank: " + e.getMessage());
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
                ORDER BY created_at ASC
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

                        // Jetzt müssen wir die 5 Karten des Pakets dem Benutzer zuweisen
                        // Karten im Paket abrufen
                        String selectCardsSql = "SELECT card_id FROM cards WHERE package_id = ?";
                        try (PreparedStatement selectCardsStmt = conn.prepareStatement(selectCardsSql)) {
                            selectCardsStmt.setObject(1, packageId);
                            ResultSet cardsRs = selectCardsStmt.executeQuery();

                            while (cardsRs.next()) {
                                String cardId = cardsRs.getString("card_id");

                                // Karte dem Benutzer zuweisen (in user_cards-Tabelle einfügen)
                                String insertUserCardSql = "INSERT INTO user_cards (user_id, card_id) VALUES (?, ?)";
                                try (PreparedStatement insertUserCardStmt = conn.prepareStatement(insertUserCardSql)) {
                                    insertUserCardStmt.setObject(1, user.getId());
                                    insertUserCardStmt.setString(2, cardId);
                                    insertUserCardStmt.executeUpdate();
                                }
                            }
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


    public boolean updateUserDeck(User user, JsonNode jsonNode) {
        if (jsonNode.size() == 4) {
            try (Connection conn = Database.getInstance().connect()) {
                String checkOwnershipSql = """
            SELECT 1 FROM user_cards WHERE card_id = ? AND user_id = ?
            """;
                try (PreparedStatement stmt = conn.prepareStatement(checkOwnershipSql)) {
                    for (JsonNode cardNode : jsonNode) {
                        String cardId = cardNode.asText();  // Extrahiere die card_id aus dem JSON

                        // Setze die Parameter für die card_id und user_id
                        stmt.setString(1, cardId);
                        stmt.setObject(2, user.getId());

                        try (ResultSet rs = stmt.executeQuery()) {
                            if (!rs.next()) {
                                // Wenn keine Ergebnisse zurückgegeben werden, bedeutet das, dass die Karte nicht dem Benutzer gehört
                                System.out.println("test");
                                return false;
                            }
                        }

                        // Füge die Karte ins Deck ein, wenn die Besitzüberprüfung erfolgreich war
                        String insertDeckSql = "INSERT INTO deck (user_id, card_id) VALUES (?, ?)";
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertDeckSql)) {
                            insertStmt.setObject(1, user.getId());
                            insertStmt.setString(2, cardId);
                            insertStmt.executeUpdate();
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;  // Wenn das JSON keine Karten enthält
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
