package com.yourpackage.models;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.Socket;

public class GetRequestHandler {
    private final UserService userService;

    public GetRequestHandler(UserService userService) {
        this.userService = userService;
    }

    public String handleGetRequest(String path, String authorization) {
        String[] pathParts = path.split("/");
        String username = pathParts[2]; // Benutzername aus dem Pfad extrahieren
        if (!RequestHelper.isAuthorized(authorization, username)) {
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
        User user = userService.getUserFromDatabase(username);

        if (user != null) {
            return "HTTP/1.1 200\r\n" + userService.toJson(user);
        }
        return "HTTP/1.1 404 Not Found";
    }
}