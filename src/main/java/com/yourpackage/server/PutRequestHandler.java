package com.yourpackage.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.yourpackage.models.User;
import com.yourpackage.models.UserService;

public class PutRequestHandler {
    private final UserService userService;

    public PutRequestHandler(UserService userService) {
        this.userService = userService;
    }

    public String handlePutRequest(String path, JsonNode jsonNode, String authorization) {
        String[] pathParts = path.split("/");
        //System.out.println(pathParts[2]); Username vom Pfad
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return createJsonResponse("401 Unauthorized", "");
        }
        String token = authorization.substring("Bearer ".length());
        if (!token.endsWith("-mtcgToken")) {
            return createJsonResponse("401 Unauthorized", "");
        }
        String username = token.substring(0, token.indexOf("-mtcgToken"));
        //System.out.println(username); Username von der Authorization herausgefiltert
        if (!username.equals(pathParts[2])) {
            return createJsonResponse("401 Unauthorized", "");
        }
        switch (pathParts[1]) {
            case "users":
                if (pathParts.length > 2) {
                    return handleEditUserData(jsonNode, username);
                }
                return createJsonResponse("404 Not Found", "Username is missing");

            case "deck":
                    return handleEditUserDeck(jsonNode, authorization);

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

    // Methode zum Bearbeiten des Decks eines Benutzers
    private String handleEditUserDeck(JsonNode jsonNode, String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return createJsonResponse("401 Unauthorized", "");
        }
        String token = authorization.substring("Bearer ".length());
        if (!token.endsWith("-mtcgToken")) {
            return createJsonResponse("401 Unauthorized", "");
        }
        String username = token.substring(0, token.indexOf("-mtcgToken"));
        User user = userService.getUserFromDatabase(username);

        if (user != null) {

                if (userService.updateUserDeck(user, jsonNode)) {
                    return createJsonResponse("204 No Content", "User Deck updated");
            }
                return createJsonResponse("400 Bad Request", "Must be 4 Cards or User is not owner of the card");
            }
            return createJsonResponse("404 Not Found", "User not found");
    }



    public String createJsonResponse(String code, String message) {
        return "HTTP/1.1 " + code + "\nContent-Type: application/json\n\n" + "{ \"message\": \"" + message + "\" }";
    }
}