package com.yourpackage.models;
import com.yourpackage.database.Database;

import java.sql.PreparedStatement;
import java.sql.*;
import java.util.UUID;


public abstract class Card {
    private String id;
    private String name;
    private final double damage;
    private String elementType;
    private String cardType;



    // Konstruktor
    public Card(String id, String name, double damage, String elementType, String cardType) {
        this.id = id;
        this.name = name;
        this.damage = damage;
        this.elementType = elementType;
        this.cardType = cardType;
    }

    // Methode zum Hinzufügen einer Card in die Datenbank
    public boolean createCard(UUID packageId) {
        try (Connection conn = Database.getInstance().connect()) {
            String insertCardSql = "INSERT INTO cards (card_id, name, damage, element_type, card_type, package_id) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertCardSql)) {
                stmt.setString(1, id);
                stmt.setString(2, name);
                stmt.setDouble(3, damage);
                stmt.setString(4, elementType);
                stmt.setString(5, cardType);
                stmt.setObject(6, packageId); // setObject für UUID verwenden
                stmt.executeUpdate();
                return true; // Karte erfolgreich erstellt
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Fehler bei der Datenbankoperation
        }
    }




    // Getter und Setter
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getDamage() {
        return damage;
    }

    public String getElementType() {
        return elementType;
    }

    public void setElementType(String elementType) {
        this.elementType = elementType;
    }

    public String getCardType() {return cardType;}

    public void setCardType(String cardType) {this.cardType = cardType;}


    @Override
    public String toString() {
        return "Card{" +
                "id=" + id +
                ", name=" + name +
                ", damage=" + damage +
                ", elementType=" + elementType +
                ", cardType=" + cardType +
                '}';
    }
}
