package com.yourpackage.models;

import com.yourpackage.database.Database;
import com.fasterxml.jackson.databind.JsonNode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID; //für UUID


public class User {
    private UUID id;  // UUID für die ID, die aus der Datenbank kommt
    private String username;
    private String password;
    private int coins;
    private String name;
    private String bio;
    private String image;
    private List<Card> cardStack; // Stack von Karten gesamt
    private List<Card> deck; // Deck mit den 4 Karten für den Kampf

    // Konstruktor
    public User( String username, String password, int coins) {
        this.username = username;
        this.password = password;
        this.coins = coins;
        this.cardStack = new ArrayList<>();
        this.deck = new ArrayList<>();
    }

    // Überladener Konstruktor für vollständige Benutzerdaten
    public User(UUID id, String username, String password, String name, String bio, String image, int coins) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.bio = bio;
        this.image = image;
        this.coins = coins;
        this.cardStack = new ArrayList<>();
        this.deck = new ArrayList<>();
    }

    public boolean createUserInDatabase() {
        try (Connection conn = Database.connect()) {
            // Überprüfen, ob der Benutzer bereits existiert
            String checkSql = "SELECT COUNT(*) FROM users WHERE username = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, this.username);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    return false; // Benutzer existiert bereits
                }
            }

            // SQL-Befehl zur Benutzererstellung, ID wird automatisch generiert
            String sql = "INSERT INTO users (username, password, coins) VALUES (?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(sql)) {
                insertStmt.setString(1, this.username);
                insertStmt.setString(2, this.password);
                insertStmt.setInt(3, this.coins);
                insertStmt.executeUpdate(); // ID wird automatisch generiert
                return true; // Benutzer erfolgreich erstellt
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Fehler bei der Datenbankoperation
        }
    }


    // Login-Methode
    public boolean loginUser() {
        try (Connection conn = Database.connect()) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, this.username);
                pstmt.setString(2, this.password);
                ResultSet rs = pstmt.executeQuery();
                return rs.next(); // Benutzer gefunden
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Fehler bei der Datenbankoperation
        }
    }

    // Methode zum Abrufen eines Benutzers
    public void getUser(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                this.username = rs.getString("username");
                this.coins = rs.getInt("coins");
                this.name = rs.getString("name"); // Angenommen, es gibt ein "name"-Feld in der DB
                this.bio = rs.getString("bio"); // Angenommen, es gibt ein "bio"-Feld in der DB
                this.image = rs.getString("image"); // Angenommen, es gibt ein "image"-Feld in der DB
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Username: " + this.username);
                System.out.println("Coins: " + this.coins);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // Statische Methode, um einen Benutzer aus der Datenbank abzurufen
    public static User getUserFromDatabase(String username) {
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
                int coins = rs.getInt("coins");

                // Erstelle ein neues User-Objekt und gebe es zurück
                User user = new User(id, username, password, name, bio, image, coins);
                return user;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null; // Benutzer wurde nicht gefunden
    }


    public void updateData(JsonNode jsonNode) {
        if (jsonNode.has("Name")) {
            this.name = jsonNode.get("Name").asText();
        }
        if (jsonNode.has("Bio")) {
            this.bio = jsonNode.get("Bio").asText();
        }
        if (jsonNode.has("Image")) {
            this.image = jsonNode.get("Image").asText();
        }

        // Implementiere die Logik, um die Änderungen in der Datenbank zu speichern
        try (Connection conn = Database.connect()) {
            String sql = "UPDATE users SET name = ?, bio = ?, image = ? WHERE username = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, this.name);
                pstmt.setString(2, this.bio);
                pstmt.setString(3, this.image);
                pstmt.setString(4, this.username);
                pstmt.executeUpdate(); // Update ausführen
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public String toJson() {
        return "{\"username\":\"" + username + "\", \"name\":\"" + name + "\", \"bio\":\"" + bio + "\", \"image\":\"" + image + "\"}";
    }


    // Methode zum Hinzufügen einer Karte zum Kartenstack
    public void addCardToStack(Card card) {
        this.cardStack.add(card);
    }

    // Methode zum Hinzufügen einer Karte zum Deck
    public void addCardToDeck(Card card) {
        if (deck.size() < 4) {
            this.deck.add(card);
        } else {
            System.out.println("Deck is full. Cannot add more cards.");
        }
    }

    // Methode zum Abrufen des Decks
    public List<Card> getDeck() {
        return deck;
    }

    // Weitere Methoden (updateUserCoins, deleteUser) ...

    // Getter und Setter für die Eigenschaften
    // Getter und Setter für die ID
    public UUID getId() {return id;}

    public void setId(UUID id) {this.id = id;}

    public String getUsername() {return username;}

    public void setUsername(String username) {
        this.username = username;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public List<Card> getCardStack() {
        return cardStack;
    }
}
