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
            return "HTTP/1.1 401 Unauthorized";
        }
        String token = authorization.substring("Bearer ".length());
        if (!token.endsWith("-mtcgToken")) {
            return "HTTP/1.1 401 Unauthorized";
        }
        String username = token.substring(0, token.indexOf("-mtcgToken"));
        User user = userService.getUserFromDatabase(username);
        // Überprüfen, ob der Pfad die Struktur "/users/{username}" hat
        if (pathParts.length == 3 && "users".equals(pathParts[1])) {
            return getUserData(user); // Benutzerdaten abrufen
        }
        switch (path) {
            case "/cards":
                return getCardStack(user);

            case "/deck":
                return getCardDeck(user);

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

    private String getCardStack(User user) {
        if (user != null) {
            return "HTTP/1.1 200\r\n" + cardToJson(user.getCardStack(), user.getUsername());
        }
        return "HTTP/1.1 404 Not Found";
    }


    private String getUserData(User user) {

        if (user != null) {
            String userJson = userDataToJson(user);
            return "HTTP/1.1 200\r\n" + userJson;
        }
        return "HTTP/1.1 404 Not Found";
    }

    private String getCardDeck(User user) {
        if (user != null) {
            String userJson = cardToJson(user.getDeck(), user.getUsername());
            return "HTTP/1.1 200\r\n" + userJson;
        }
        return "HTTP/1.1 404 Not Found";
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
        jsonBuilder.append("{\n") // Neue Zeile für den Haupt-JSON-Block
                .append("  \"username\":\"").append(username).append("\",\n")
                .append("  \"cards\": [\n");


        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            jsonBuilder.append("    {") // Einrückung für die Kartenobjekte
                    .append("\n      \"card_id\":\"").append(card.getId()).append("\",\n")
                    .append("      \"name\":\"").append(card.getName()).append("\",\n")
                    .append("      \"damage\":").append(card.getDamage()).append(",\n")
                    .append("      \"element_type\":\"").append(card.getElementType()).append("\",\n")
                    .append("      \"card_type\":\"").append(card.getCardType()).append("\"\n")
                    .append("    }");

            if (i < cards.size() - 1) {
                jsonBuilder.append(",\n"); // Komma zwischen den Karten hinzufügen und neue Zeile
            } else {
                jsonBuilder.append("\n"); // Neue Zeile nach der letzten Karte
            }
        }

        jsonBuilder.append("  ]\n") // Einrückung für den Karten-Array
                .append("}");
        return jsonBuilder.toString();
    }


}

