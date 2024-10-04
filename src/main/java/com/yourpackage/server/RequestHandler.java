package com.yourpackage.server;
import com.yourpackage.models.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
public class RequestHandler {

    public static String handleRequest(String method, String path, Socket clientSocket, BufferedReader in) {
        String response;
        switch (method) {
            case "GET":
                response = handleGetRequest(path);
                break;
            case "POST":
                response = handlePostRequest(path, clientSocket, in);
                break;
            case "PUT":
                response = handlePutRequest(path, clientSocket, in);
                break;
            default:
                response = "HTTP/1.1 405 Method Not Allowed";
                break;
        }
        return response;
    }

    private static String handleGetRequest(String path) {
        if (path.startsWith("/users/")) {
            String username = path.split("/")[2]; // Benutzername aus dem Pfad extrahieren

            return getUserData(username);
        }
        return "HTTP/1.1 404 Not Found";
    }

    private static String getUserData(String username) {
        // Hier sollte die Logik stehen, um Benutzerdaten aus der Datenbank zu holen
        User user = User.getUserFromDatabase(username); // Beispielmethode
        if (user != null) {
            return "HTTP/1.1 200\r\n" + user.toJson();
        }
        return "HTTP/1.1 404 Not Found";
    }

    public static String handlePostRequest(String path, Socket clientSocket, BufferedReader in) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String[] requestParts = readRequest(in);
            String body = requestParts[1]; // Body ist das zweite Element
            JsonNode jsonNode = objectMapper.readTree(body);

            // Überprüfen, ob die nötigen Felder vorhanden sind
            if (path.equals("/users")) {
                return handleUserCreation(jsonNode);
            } else if (path.equals("/sessions")) {
                return handleUserLogin(jsonNode);
            } else {
                return "HTTP/1.1 404 Not Found\n\nThe requested resource was not found.";
            }
        } catch (IOException e) {
            return "HTTP/1.1 400 Bad Request\n\nInvalid JSON format.";
        }
    }

    public static String handlePutRequest(String path, Socket clientSocket, BufferedReader in) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            String[] requestParts = readRequest(in);
            String body = requestParts[1]; // Body ist das zweite Element
            JsonNode jsonNode = objectMapper.readTree(body);

            // Überprüfen, ob der Pfad mit "/users/" beginnt
            if (path.startsWith("/users/")) {
                String username = path.split("/")[2]; // Extrahiere den Benutzernamen aus dem Pfad

                // Hole den Benutzer aus der Datenbank
                User user = User.getUserFromDatabase(username); // Holt den Benutzer basierend auf dem Benutzernamen

                if (user != null) {
                    // Aktualisiere die Benutzerdaten
                    user.updateData(jsonNode); // Aktualisiert die Benutzerinformationen

                    // Sende die aktualisierten Daten als JSON zurück
                    return "HTTP/1.1 204";
                } else {
                    return "HTTP/1.1 404 Not Found\n\nUser not found."; // Benutzer wurde nicht gefunden
                }
            } else {
                return "HTTP/1.1 404 Not Found\n\nThe requested resource was not found."; // Ungültiger Pfad
            }
        } catch (IOException e) {
            return "HTTP/1.1 400 Bad Request\n\nInvalid JSON format."; // Fehler beim Lesen des JSON
        }
    }



    // Methode zur Benutzerregistrierung
    private static String handleUserCreation(JsonNode jsonNode) {
        if (jsonNode.has("Username") && jsonNode.has("Password")) {
            String username = jsonNode.get("Username").asText();
            String password = jsonNode.get("Password").asText();
            User newUser = new User(username, password, 20);

            // Verwende die modifizierte Methode, um den Benutzer zu erstellen
            boolean success = newUser.createUserInDatabase();

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
    private static String handleUserLogin(JsonNode jsonNode) {
        if (jsonNode.has("Username") && jsonNode.has("Password")) {
            String username = jsonNode.get("Username").asText();
            String password = jsonNode.get("Password").asText();
            User user = new User(username, password, 0); // Coins sind hier irrelevant

            // Versuche, den Benutzer einzuloggen
            boolean success = user.loginUser();

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



    private static String[] readRequest(BufferedReader in) throws IOException {
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

