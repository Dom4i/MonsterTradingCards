package com.yourpackage.server;

import com.yourpackage.models.Card;
import com.yourpackage.models.User;
import com.yourpackage.models.UserService;

import java.util.List;

public class GetRequestHandler {
    private final UserService userService;

    public GetRequestHandler(UserService userService) {
        this.userService = userService;
    }

    public String handleGetRequest(String path, String authorization) {
        String[] pathParts = path.split("/");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return createJsonResponse("401 Unauthorized", "");
        }
        String token = authorization.substring("Bearer ".length());
        if (!token.endsWith("-mtcgToken")) {
            return createJsonResponse("401 Unauthorized", "");
        }
        String username = token.substring(0, token.indexOf("-mtcgToken"));
        User user = userService.getUserFromDatabase(username);
        // Überprüfen, ob der Pfad die Struktur "/users/{username}" hat
        if (pathParts.length == 3 && "users".equals(pathParts[1])) {
            return getUserData(user); // Benutzerdaten abrufen
        }
        switch (path) {
            case "/cards":
                return printCardStack(user);

            case "/deck":
                return printCardDeck(user);

            case "/scoreboard":
                return "HTTP/1.1 501 Method Not Implemented";

            case "/stats":
                return "HTTP/1.1 501 Method Not Implemented";

            case "/traidings":
                return "HTTP/1.1 501 Method Not Implemented";

            default:
                return createJsonResponse("404 Not Found", "Path not found"); // Für alle anderen Pfade
        }
    }

    private String printCardStack(User user) {
        if (user != null) {
            return createJsonResponse("200 OK", cardToJson(user.getCardStack(), user.getUsername()));
        }
        return createJsonResponse("404 Not Found", "User not found");
    }


    private String getUserData(User user) {
        if (user != null) {
            String userJson = userDataToJson(user);
            return createJsonResponse("200 OK", userJson);
        }
        return createJsonResponse("404 Not Found", "User not found");
    }

    private String printCardDeck(User user) {
        if (user != null) {
            String userJson = cardToJson(user.getDeck(), user.getUsername());
            return createJsonResponse("200 OK", userJson);
        }
        return createJsonResponse("404 Not Found", "User not found");
    }

    private boolean isAuthorized(String authHeader, String username) {
        // Überprüfen, ob der Header mit "Bearer" beginnt und den Token enthält
        return authHeader != null && authHeader.startsWith("Bearer ") &&
                authHeader.equals("Bearer " + username + "-mtcgToken"); // Hier Token überprüfen
    }

    public String userDataToJson(User user) {
        return "{"
                + "\"id\":\"" + user.getId() + "\", "
                + "\"username\":\"" + user.getUsername() + "\", "
                + "\"password\":\"" + user.getPassword() + "\", "
                + "\"coins\":" + user.getCoins() + ", "
                + "\"score\":" + user.getScore() + ", "
                + "\"token\":\"" + user.getToken() + "\", "
                + "\"name\":\"" + user.getName() + "\", "
                + "\"bio\":\"" + user.getBio() + "\", "
                + "\"image\":\"" + user.getImage() + "\""
                + "}";
    }

    public String cardToJson(List<Card> cards, String username) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\r\n") // Neue Zeile für den Haupt-JSON-Block
                .append("  \"username\":\"").append(username).append("\",\r\n")
                .append("  \"cards\": [\r\n");

        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            jsonBuilder.append("    {") // Einrückung für die Kartenobjekte
                    .append("\r\n      \"card_id\":\"").append(card.getId()).append("\",\r\n")
                    .append("      \"name\":\"").append(card.getName()).append("\",\r\n")
                    .append("      \"damage\":").append(card.getDamage()).append(",\r\n")
                    .append("      \"element_type\":\"").append(card.getElementType()).append("\",\r\n")
                    .append("      \"card_type\":\"").append(card.getCardType()).append("\"\r\n")
                    .append("    }");

            if (i < cards.size() - 1) {
                jsonBuilder.append(",\r\n"); // Komma zwischen den Karten hinzufügen und neue Zeile
            } else {
                jsonBuilder.append("\r\n"); // Neue Zeile nach der letzten Karte
            }
        }

        jsonBuilder.append("  ]\r\n") // Einrückung für den Karten-Array
                .append("}");
        return jsonBuilder.toString();
    }
    public String createJsonResponse(String code, String message) {
        return "HTTP/1.1 " + code + "\nContent-Type: application/json\n\n" + "{ \"message\": \"" + message + "\" }";
    }
}

