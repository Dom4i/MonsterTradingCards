package com.yourpackage.models;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PostRequestHandler {
    private final UserService userService;

    public PostRequestHandler(UserService userService) {
        this.userService = userService;
    }

    public String handlePostRequest(String path, JsonNode jsonNode, String authorization) {
        switch (path) {
            case "/users":
                return handleUserCreation(jsonNode);

            case "/sessions":
                return handleUserLogin(jsonNode);

            case "/packages":
                return handleCreatePackage(jsonNode, authorization);
            case "/transactions/packages":
                return "HTTP/1.1 501 Method Not Implemented";
            default:
                return "HTTP/1.1 404 Not Found\n\nThe requested resource was not found.";
        }
    }

    // Methode zur Benutzerregistrierung
    private  String handleUserCreation(JsonNode jsonNode) {
        if (jsonNode.has("Username") && jsonNode.has("Password")) {
            String username = jsonNode.get("Username").asText();
            String password = jsonNode.get("Password").asText();
            User newUser = new User(username, password, 20, 100);

            // Verwende die Methode, um den Benutzer zu erstellen
            boolean success = userService.createUserInDatabase(newUser);

            if (success) {
                return "HTTP/1.1 201";
            } else {
                return "HTTP/1.1 409 Conflict - User already exists.";
            }
        } else {
            return "HTTP/1.1 400 Bad Request\n\nMissing Username or Password.";
        }
    }

    // Methode zum Benutzereinloggen
    private  String handleUserLogin(JsonNode jsonNode) {
        if (jsonNode.has("Username") && jsonNode.has("Password")) {
            String username = jsonNode.get("Username").asText();
            String password = jsonNode.get("Password").asText();
            User user = new User(username, password);
            UserService userService = new UserService();
            // Versuche, den Benutzer einzuloggen
            boolean success = userService.loginUser(user);

            if (success) {
                String token = username + "-mtcgToken"; // Generiere den Token
                return "HTTP/1.1 200 OK\n\nToken: " + token;
            } else {
                return "HTTP/1.1 401 Unauthorized\n\nInvalid username or password.";
            }
        } else {
            return "HTTP/1.1 400 Bad Request\n\nMissing Username or Password.";
        }
    }

    public  String handleCreatePackage(JsonNode jsonNode, String authorization) {
        try {
            // Überprüfen, ob die Anfrage aus genau 5 Karten besteht
            if (!jsonNode.isArray() || jsonNode.size() != 5) {
                return "HTTP/1.1 400 Bad Request\n\nA package must contain exactly 5 cards.";
            }
            // Admin-Authentifizierung prüfen
            String username = "admin";
            if (!RequestHelper.isAuthorized(authorization, username)) {
                return "HTTP/1.1 401 Unauthorized";
            }
            // Generiere eine Package-ID
            UUID packageId = UUID.randomUUID();

            // Füge das Paket in die Datenbank ein
            Package cardPackage = new Package(packageId);
            boolean packageAdded = cardPackage.addPackageToDatabase();
            if (!packageAdded) {
                return "HTTP/1.1 409 Conflict - Package already exists.";
            }
            List<Card> packageCards = new ArrayList<>();

            // Iteriere über die Karten und füge jede zu ihrer entsprechenden Tabelle hinzu
            for (JsonNode cardNode : jsonNode) {
                if (cardNode.has("Id") && cardNode.has("Name") && cardNode.has("Damage")) {
                    String cardId = cardNode.get("Id").asText();
                    String cardName = cardNode.get("Name").asText();
                    double cardDamage = cardNode.get("Damage").asDouble();
                    // Bestimme den Elementtyp basierend auf dem Namen oder einer anderen Eigenschaft
                    String cardElement = RequestHelper.determineElementType(cardName);
                    Card card;

                    // Bestimme, ob es sich um eine MonsterCard oder SpellCard handelt
                    if (RequestHelper.isMonsterCard(cardName)) {
                        // MonsterCard erstellen
                        String cardType = "MONSTER";
                        card = new MonsterCard(cardId, cardName, cardDamage, cardElement, cardType);
                    } else {
                        // SpellCard erstellen
                        String cardType = "SPELL";
                        card = new SpellCard(cardId, cardName, cardDamage, cardElement, cardType);
                    }
                    // Karte in der Datenbank speichern und zur Liste hinzufügen
                    boolean cardCreated = card.createCard(packageId); // Übergabe der packageId an die Karte
                    if (!cardCreated) {
                        return "HTTP/1.1 400 Bad Request\n\nCard already exists.";
                    }
                    packageCards.add(card);
                } else {
                    return "HTTP/1.1 400 Bad Request\n\nEach card must contain Id, Name, and Damage.";
                }
            }
            return "HTTP/1.1 201 Package Created";
        } catch (Exception e) {
            e.printStackTrace();
            return "HTTP/1.1 500 Internal Server Error\n\nAn error occurred while creating the package.";
        }
    }


}
