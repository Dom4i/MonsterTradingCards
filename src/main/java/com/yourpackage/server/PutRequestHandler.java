package com.yourpackage.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.yourpackage.models.User;
import com.yourpackage.models.UserService;

public class PutRequestHandler {
    private final UserService userService;

    public PutRequestHandler(UserService userService) {
        this.userService = userService;
    }

    public String handlePutRequest(String path, JsonNode jsonNode) {
        String[] pathParts = path.split("/");

        if (pathParts.length < 2) {
            return createJsonResponse("404 Not Found", "The requested resource was not found");
        }
        switch (pathParts[1]) {
            case "users":
                if (pathParts.length > 2) {
                    String username = pathParts[2]; // Extrahiere den Benutzernamen aus dem Pfad
                    return handleEditUserData(jsonNode, username);
                }
                return createJsonResponse("404 Not Found", "Username is missing");

            case "deck":
                return "HTTP/1.1 501 Method Not Implemented"; // Noch nicht implementiert

            default:
                return createJsonResponse("404 Not Found", "The requested resource was not found");
        }
    }

    // Methode zur Bearbeitung eines Users
    private String handleEditUserData(JsonNode jsonNode, String username) {

        User user = userService.getUserFromDatabase(username); // Holt den Benutzer basierend auf dem Benutzernamen
        if (user != null) {
            // Aktualisiere die Benutzerdaten
            userService.updateUserData(user, jsonNode); // Aktualisiert die Benutzerinformationen
            // Sende die aktualisierten Daten als JSON zurück
            return createJsonResponse("204 No Content", "User data updated");
        } else {
            return createJsonResponse("404 Not Found", "User not found");
        }
    }
    public String createJsonResponse(String code, String message) {
        return "HTTP/1.1 " + code + "\nContent-Type: application/json\n\n" + "{ \"message\": \"" + message + "\" }";
    }
}