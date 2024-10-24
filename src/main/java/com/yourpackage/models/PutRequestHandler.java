package com.yourpackage.models;

import com.fasterxml.jackson.databind.JsonNode;

public class PutRequestHandler {
    private final UserService userService;

    public PutRequestHandler(UserService userService) {
        this.userService = userService;
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
}