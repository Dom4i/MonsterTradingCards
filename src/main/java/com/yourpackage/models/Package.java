package com.yourpackage.models;
import com.yourpackage.database.Database;
import com.fasterxml.jackson.core.JsonProcessingException; // Import für JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException; // Import für JsonMappingException
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.IOException; // Importiere IOException
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class Package {
    private List<Card> cards; // Liste von Karten
    private String packageId; // ID des Pakets
    private String name; // Name des Pakets

    public Package(String packageId, String name, List<Card> cards) {
        this.packageId = packageId;
        this.name = name;
        this.cards = cards;
    }

    // Getter und Setter
    public List<Card> getCards() {
        return cards;
    }

    public String getPackageId() {
        return packageId;
    }

    public String getName() {
        return name;
    }

    public static String createPackage(String jsonBody) {
        ObjectMapper objectMapper = new ObjectMapper();

        try (Connection conn = Database.connect()) {
            // Versuche, die Karten aus dem JSON zu lesen
            JsonNode cardsNode = objectMapper.readTree(jsonBody); // Der gesamte Body ist eine Liste von Karten

            // SQL-Befehl zum Einfügen von Karten
            String sqlCard = "INSERT INTO cards (id, name, damage, elementType, cardType) VALUES (?, ?, ?, ?, ?)";

            // Überprüfen, ob die Karten im JSON vorhanden sind
            if (cardsNode.isArray()) {
                for (JsonNode cardNode : cardsNode) {
                    String cardId = cardNode.get("Id") != null ? cardNode.get("Id").asText() : null;
                    String cardName = cardNode.get("Name") != null ? cardNode.get("Name").asText() : null;
                    double damage = cardNode.get("Damage") != null ? cardNode.get("Damage").asDouble() : 0.0;
                    String elementType = "Default"; // Setze einen Standardwert, falls nicht vorhanden
                    String cardType = "Default"; // Setze einen Standardwert, falls nicht vorhanden
                    System.out.println("Inserting card with ID: " + cardId + ", Name: " + cardName + ", Damage: " + damage);

                    // Überprüfen, ob alle Karteninformationen vorhanden sind
                    if (cardId == null || cardName == null) {
                        return "HTTP/1.1 400 Bad Request: Missing card information"; // Fehlende Karteninformationen
                    }

                    try (PreparedStatement insertCardStmt = conn.prepareStatement(sqlCard)) {
                        insertCardStmt.setString(1, cardId);
                        insertCardStmt.setString(2, cardName);
                        insertCardStmt.setDouble(3, damage);
                        insertCardStmt.setString(4, elementType); // Hier kannst du die Logik anpassen, um den Elementtyp festzulegen
                        insertCardStmt.setString(5, cardType); // Hier kannst du die Logik anpassen, um den Kartentyp festzulegen
                        insertCardStmt.executeUpdate();
                    }
                }
            } else {
                return "HTTP/1.1 400 Bad Request: Cards should be an array"; // Fehlende Karten
            }

            return "HTTP/1.1 201 Created"; // Alle Karten erfolgreich erstellt

        } catch (SQLException e) {
            e.printStackTrace();
            return "HTTP/1.1 500 Internal Server Error"; // Fehler bei der Datenbankoperation
        } catch (JsonMappingException e) {
            e.printStackTrace();
            return "HTTP/1.1 400 Bad Request: Invalid JSON mapping"; // Ungültige JSON-Mapping-Fehler
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "HTTP/1.1 400 Bad Request: JSON processing error"; // Fehler bei der Verarbeitung von JSON
        } catch (IOException e) {
            e.printStackTrace();
            return "HTTP/1.1 500 Internal Server Error"; // Allgemeiner IO-Fehler
        }
    }
}

