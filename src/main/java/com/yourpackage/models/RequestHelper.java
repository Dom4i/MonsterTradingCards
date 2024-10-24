package com.yourpackage.models;

public class RequestHelper {


    // Helper Funktionen
    public static String determineElementType(String cardName) {
        if (cardName.toLowerCase().contains("fire") || cardName.toLowerCase().contains("dragon")) {
            return "FIRE";
        } else if (cardName.toLowerCase().contains("water")) {
            return "WATER";
        } else {
            return "NORMAL";
        }
    }

    public static boolean isMonsterCard(String cardName) {
        // Logik zur Bestimmung, ob die Karte eine MonsterCard ist
        return cardName.toLowerCase().contains("goblin") || cardName.toLowerCase().contains("dragon") || cardName.toLowerCase().contains("ork");
    }


    public static boolean isAuthorized(String authHeader, String username) {
        // Überprüfen, ob der Header mit "Bearer" beginnt und den Token enthält
        return authHeader != null && authHeader.startsWith("Bearer ") &&
                authHeader.equals("Bearer " + username + "-mtcgToken"); // Hier Token überprüfen
    }


    // Methode zum Auslesen des Authorization-Headers
    public static String getAuthorizationHeader(String header) {
        String[] lines = header.split("\n");
        for (String line : lines) {
            if (line.startsWith("Authorization: ")) {
                return line.substring("Authorization: ".length()).trim();
            }
        }
        return null; // Kein Authorization-Header gefunden
    }
}
