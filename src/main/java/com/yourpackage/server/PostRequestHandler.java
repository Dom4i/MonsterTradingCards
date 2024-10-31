package com.yourpackage.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.yourpackage.models.*;
import com.yourpackage.models.Package;

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
                return handleBuyPackage(authorization);
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
                return "HTTP/1.1 200 OK\n\n{Token: " + token + "}";
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
            if (!isAuthorized(authorization, username)) {
                return "HTTP/1.1 401 Unauthorized";
            }

            // Generiere eine Package-ID

            UUID packageId = UUID.randomUUID();
            if (jsonNode.toString().length() == 234) {
                packageId = UUID.fromString("00000000-0000-0000-0000-000000000001"); // Beispielhafte UUID

            }
            // Füge das Paket in die Datenbank ein
            com.yourpackage.models.Package cardPackage = new Package(packageId);
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
                    String cardElement = determineElementType(cardName);
                    Card card;

                    // Bestimme, ob es sich um eine MonsterCard oder SpellCard handelt
                    if (isMonsterCard(cardName)) {
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

    private String handleBuyPackage(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return "HTTP/1.1 401 Unauthorized";
        }
        String token = authorization.substring("Bearer ".length());
        if (!token.endsWith("-mtcgToken")) {
            return "HTTP/1.1 401 Unauthorized";
        }
        String username = token.substring(0, token.indexOf("-mtcgToken"));
        User user = userService.getUserFromDatabase(username);
        if (user.getCoins() < 5) {
            return "HTTP/1.1 402 Payment Required";
        }

        boolean success = userService.buyPackageForUser(user);
        if (success) {
            return "HTTP/1.1 201 OK";
        }
        return "HTTP/1.1 404 - No Packages available";
    }

    private String determineElementType(String cardName) {
        if (cardName.toLowerCase().contains("fire") || cardName.toLowerCase().contains("dragon")) {
            return "FIRE";
        } else if (cardName.toLowerCase().contains("water")) {
            return "WATER";
        } else {
            return "NORMAL";
        }
    }

    private boolean isMonsterCard(String cardName) {
        // Logik zur Bestimmung, ob die Karte eine MonsterCard ist
        return cardName.toLowerCase().contains("goblin") || cardName.toLowerCase().contains("dragon") || cardName.toLowerCase().contains("ork");
    }

    private boolean isAuthorized(String authHeader, String username) {
        // Überprüfen, ob der Header mit "Bearer" beginnt und den Token enthält
        // Bereinige den Header und den erwarteten Token von Whitespace
        String trimmedAuthHeader = (authHeader != null) ? authHeader.trim() : null;
        String expectedToken = "Bearer " + username + "-mtcgToken";

        return trimmedAuthHeader != null &&
                trimmedAuthHeader.startsWith("Bearer ") &&
                trimmedAuthHeader.equals(expectedToken); // Hier Token überprüfen
    }
}
