package com.yourpackage.models;


import com.yourpackage.database.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class User {
    private String username;
    private String password;
    private int coins;
    private List<Card> cardStack; // Stack von Karten gesamt
    private List<Card> deck; // Deck mit den 4 Karten für den Kampf

    // Konstruktor
    public User(String username, String password, int coins) {
        this.username = username;
        this.password = password;
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

            // SQL-Befehl zur Benutzererstellung
            String sql = "INSERT INTO users (username, password, coins) VALUES (?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(sql)) {
                insertStmt.setString(1, this.username);
                insertStmt.setString(2, this.password);
                insertStmt.setInt(3, this.coins);
                insertStmt.executeUpdate();
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
                // Hier kannst du auch den Kartenstack und das Deck aus der Datenbank abrufen
                System.out.println("ID: " + rs.getInt("id"));
                System.out.println("Username: " + this.username);
                System.out.println("Coins: " + this.coins);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
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
    public String getUsername() {
        return username;
    }

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
