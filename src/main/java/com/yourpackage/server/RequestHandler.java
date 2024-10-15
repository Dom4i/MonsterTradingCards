package com.yourpackage.server;
import com.yourpackage.models.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yourpackage.models.Package;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RequestHandler {

    private final UserService userService;

    public RequestHandler(UserService userService) {
        this.userService = new UserService(); // Einmalige Instanzierung
    }

    public String handleRequest(String method, String path, Socket clientSocket, BufferedReader in) throws IOException {
        String response;
        String authorization;
        JsonNode jsonNode;
        try {
            String[] parts = readRequest(in);
            String header = parts[0];
            String body = parts[1];
            jsonNode = new ObjectMapper().readTree(body);
            authorization = getAuthorizationHeader(header);

        } catch (IOException e) {
            return "HTTP/1.1 400 Bad Request\n\nInvalid JSON format.";
        }
        switch (method) {
            case "GET":
                response = handleGetRequest(path, authorization);
                break;
            case "POST":
                response = handlePostRequest(path, jsonNode, authorization); // Pass jsonNode
                break;
            case "PUT":
                    response = handlePutRequest(path, jsonNode); // Pass jsonNode
                break;
            case "DELETE":
                response = "HTTP/1.1 501 Method Not Implemented";
                break;
            default:
                response = "HTTP/1.1 405 Method Not Allowed";
                break;
        }
        return response;
    }


    private String handleGetRequest(String path, String authorization) {
        String[] pathParts = path.split("/");
        String username = pathParts[2]; // Benutzername aus dem Pfad extrahieren
        if (!isAuthorized(authorization, username)) {
            return "HTTP/1.1 401 Unauthorized";
        }
        // Überprüfen, ob der Pfad die Struktur "/users/{username}" hat
        if (pathParts.length == 3 && "users".equals(pathParts[1])) {
            return getUserData(username); // Benutzerdaten abrufen
        }

        switch (path) {
            case "/cards":
                return "HTTP/1.1 501 Method Not Implemented";

            case "/deck":
                return "HTTP/1.1 501 Method Not Implemented";

            case "/scoreboard":
                return "HTTP/1.1 501 Method Not Implemented";

            case "/stats":
                return "HTTP/1.1 501 Method Not Implemented";

            case "/traidings":
                return "HTTP/1.1 501 Method Not Implemented";

            default:
                return "HTTP/1.1 404 Not Found"; // Für alle anderen Pfade
        }
    }



    private String getUserData(String username) {
        User user = userService.getUserFromDatabase(username); // Beispielmethode

        if (user != null) {
            return "HTTP/1.1 200\r\n" + userService.toJson(user);
        }
        return "HTTP/1.1 404 Not Found";
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



    public String handlePutRequest(String path, JsonNode jsonNode) {
            String[] pathParts = path.split("/");

            if (pathParts.length < 2) {
                return "HTTP/1.1 404 Not Found\n\nThe requested resource was not found.";
            }

            switch (pathParts[1]) {
                case "users":
                    if (pathParts.length > 2) {
                        String username = pathParts[2]; // Extrahiere den Benutzernamen aus dem Pfad
                        return handleEditUserData(jsonNode, username);
                    }
                    return "HTTP/1.1 404 Not Found\n\nUsername is missing.";

                case "deck":
                    return "HTTP/1.1 501 Method Not Implemented"; // Noch nicht implementiert

                default:
                    return "HTTP/1.1 404 Not Found\n\nThe requested resource was not found.";
            }
        }

    // Methode zur Bearbeitung eines Users
    private String handleEditUserData(JsonNode jsonNode, String username) {

        User user = userService.getUserFromDatabase(username); // Holt den Benutzer basierend auf dem Benutzernamen
        if (user != null) {
            // Aktualisiere die Benutzerdaten
            userService.updateUserData(user, jsonNode); // Aktualisiert die Benutzerinformationen
            // Sende die aktualisierten Daten als JSON zurück
            return "HTTP/1.1 204";
        } else {
            return "HTTP/1.1 404 Not Found\n\nUser not found."; // Benutzer wurde nicht gefunden
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
            if (!isAuthorized(authorization, username)) {
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

    // Helper Funktionen
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

    // Methode zum Auslesen des Authorization-Headers
    private static String getAuthorizationHeader(String header) {
        String[] lines = header.split("\n");
        for (String line : lines) {
            if (line.startsWith("Authorization: ")) {
                return line.substring("Authorization: ".length()).trim();
            }
        }
        return null; // Kein Authorization-Header gefunden
    }

    private boolean isAuthorized(String authHeader, String username) {
        // Überprüfen, ob der Header mit "Bearer" beginnt und den Token enthält
        return authHeader != null && authHeader.startsWith("Bearer ") &&
                authHeader.equals("Bearer " + username + "-mtcgToken"); // Hier Token überprüfen
    }

    private String[] readRequest(BufferedReader in) throws IOException {
        StringBuilder requestHeaders = new StringBuilder();
        StringBuilder requestBody = new StringBuilder();

        // Header lesen
        String line;
        while ((line = in.readLine()) != null) {
            if (line.isEmpty()) {
                break; // Ende der Header
            }
            requestHeaders.append(line).append("\n");
        }

        // Content-Length ermitteln
        int contentLength = 0;
        for (String header : requestHeaders.toString().split("\n")) {
            if (header.startsWith("Content-Length: ")) {
                contentLength = Integer.parseInt(header.substring(16).trim());
            }
        }

        // Body lesen
        if (contentLength > 0) {
            char[] bodyBuffer = new char[contentLength];
            in.read(bodyBuffer, 0, contentLength);
            String body = new String(bodyBuffer);
            requestBody.append(body);
        }

        // Header und Body zurückgeben
        return new String[]{requestHeaders.toString(), requestBody.toString()};
    }
}

