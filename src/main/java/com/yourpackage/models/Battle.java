package com.yourpackage.models;

import com.yourpackage.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Battle {
    private final List<User> players;
    private final BattleLog battleLog;

    public Battle() {
        this.players = new ArrayList<>();
        this.battleLog = new BattleLog();
    }

    public int getPlayers() {
        return players.size();
    }

    public void addPlayer(User user) {
        this.players.add(user);
    }

    public void startBattle() {
        if (players.size() < 2) {
            battleLog.addEntry("Not enough players to start the battle.");
            return;
        }

        battleLog.addEntry("Battle started between:");
        for (User player : players) {
            battleLog.addEntry(" - " + player.getUsername());
        }

        int player1Wins = 0;
        int player2Wins = 0;
        boolean threetozero = false;

        for (int round = 1; round <= 5; round++) {
            battleLog.addEntry("\nRound " + round + " started!");

            String roundWinner = playRound();

            if (roundWinner.equals(players.get(0).getUsername())) {
                player1Wins++;
                battleLog.addEntry(players.get(0).getUsername() + " wins this round.");
            } else if (roundWinner.equals(players.get(1).getUsername())) {
                player2Wins++;
                battleLog.addEntry(players.get(1).getUsername() + " wins this round.");
            } else {
                battleLog.addEntry("It's a draw! No winner this round.");
            }
            battleLog.addEntry("Current Score: " + players.get(0).getUsername() + " " + player1Wins + " | " + players.get(1).getUsername() + " " + player2Wins);

            if (player1Wins >= 3 || player2Wins >= 3) {
                break;
            }
        }

        if (player1Wins >= 3) {
            if (player2Wins == 0) {
                threetozero = true; //Unique Feature
            }
            battleLog.addEntry(players.get(0).getUsername() + " wins the battle!");
            if (threetozero) {
                updateScoresThreeToZero(players.get(0), players.get(1));
            } else {
                updateScores(players.get(0), players.get(1));
            }
            exchangeCards(players.get(0).getId(), players.get(1).getId());
        } else if (player2Wins >= 3) {
            if (player1Wins == 0) {
                threetozero = true; //Unique Feature
            }
            battleLog.addEntry(players.get(1).getUsername() + " wins the battle!");
            if (threetozero) {
                updateScoresThreeToZero(players.get(1), players.get(0));
            } else {
                updateScores(players.get(1), players.get(0));
            }
            exchangeCards(players.get(1).getId(), players.get(0).getId());
        } else {
            battleLog.addEntry("It's a draw! No player wins the battle.");
        }

        System.out.println("Battle Log: ");
        for (String logEntry : battleLog.getLogs()) {
            System.out.println(logEntry);
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        players.clear();
        battleLog.clear();
    }

    public String playRound() {
        User player1 = players.get(0);
        User player2 = players.get(1);

        Card card1 = getRandomCardFromDeck(player1);
        Card card2 = getRandomCardFromDeck(player2);

        battleLog.addEntry(player1.getUsername() + " plays " + card1);
        battleLog.addEntry(player2.getUsername() + " plays " + card2);

        String fight = checkFight(card1, card2);
        battleLog.addEntry("\nThis is a " + fight);

        if (fight.equals("specialfight")) {
            if (checkNames(card1, card2, player1, player2).equals(player1.getUsername())) {
                return player1.getUsername();
            }
            if (checkNames(card2, card1, player2, player1).equals(player2.getUsername())) {
                return player2.getUsername();
            }
        } else if (fight.equals("elementfight")) {
            card1.setDamage(checkElementals(card1, card2));
            card2.setDamage(checkElementals(card2, card1));
        }

        if (card1.getDamage() > card2.getDamage()) {
            return player1.getUsername();
        } else if (card1.getDamage() < card2.getDamage()) {
            return player2.getUsername();
        } else {
            return "draw";
        }
    }

    private void updateScores(User winner, User loser) {
        // Update die Scores in den User-Objekten
        winner.setScore(winner.getScore() + 3);
        loser.setScore(loser.getScore() - 5);

        // Schreibe die Änderungen in die Datenbank
        updateScoreInDatabase(winner);
        updateScoreInDatabase(loser);

        // Fülle das Deck des Verlierers wieder auf
        refillLoserDeck(loser);
        // Logge die Änderung
        battleLog.addEntry("Scores updated: " + winner.getUsername() + " gains 3 points, " + loser.getUsername() + " loses 5 points.");
    }
    private void updateScoresThreeToZero(User winner, User loser) {
        // Update die Scores in den User-Objekten
        winner.setScore(winner.getScore() + 5);
        loser.setScore(loser.getScore() - 5);

        // Schreibe die Änderungen in die Datenbank
        updateScoreInDatabase(winner);
        updateScoreInDatabase(loser);

        // Fülle das Deck des Verlierers wieder auf
        refillLoserDeck(loser);
        // Logge die Änderung
        battleLog.addEntry("Three to zero! Thats extra score for " + winner.getUsername() + "!");
        battleLog.addEntry("Scores updated: " + winner.getUsername() + " gains 5 points, " + loser.getUsername() + " loses 5 points.");
    }

    public void exchangeCards(UUID winnerId, UUID loserId) {
        try {
            // 1. Karten des Verlierers aus dessen Deck holen (nur IDs)
            List<String> loserCardIds = getCardIdsFromDeck(loserId);

            if (loserCardIds.size() == 4) {  // Sicherstellen, dass nur 4 Karten abgerufen wurden
                // 2. Karten in den Stack (user_cards) des Gewinners verschieben
                addCardsToUserStack(winnerId, loserCardIds);

                // 3. Karten aus dem Deck des Verlierers löschen
                removeCardsFromDeck(loserId, loserCardIds);
                removeCardsFromStack(loserId, loserCardIds);
                // 4. Protokolliere im Battlelog, welche Karten übernommen wurden (nur IDs)
                StringBuilder battleLogEntry = new StringBuilder(loserCardIds.size() + " cards transferred from " + loserId + " to " + winnerId + ": ");
                battleLogEntry.append(String.join(", ", loserCardIds));
                battleLog.addEntry(battleLogEntry.toString());  // Hinzufügen des Protokolleintrags
            } else {
                // Falls weniger oder mehr Karten als erwartet gefunden wurden
                System.out.println("Error: Verlierer hat nicht genau 4 Karten im Deck.");
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Fehlerlogging
        }
    }


    public Card getRandomCardFromDeck(User user) {
        List<Card> deck = user.getDeck();
        if (deck.isEmpty()) {
            battleLog.addEntry(user.getUsername() + " has no cards left!");
            return null;
        }
        Random random = new Random();
        return deck.get(random.nextInt(deck.size()));
    }

    public String checkFight(Card card1, Card card2) {
        if (card1.getCardType().equals("MONSTER") && card2.getCardType().equals("MONSTER")) {
            return "monsterfight";
        }
        if ((card1.getName().equals("WaterGoblin") && card2.getName().equals("Dragon")) || card1.getName().equals("Dragon") && card2.getName().equals("Watergoblin")) {
            return "specialfight";
        }
        if ((card1.getName().equals("Knight") && card2.getName().equals("WaterSpell")) || card1.getName().equals("WaterSpell") && card2.getName().equals("Knight")) {
            return "specialfight";
        }
        if ((card1.getName().equals("FireElf") && card2.getName().equals("Dragon")) || card1.getName().equals("Dragon") && card2.getName().equals("FireElf")) {
            return "specialfight";
        }
        //Unique specialfight
        if ((card1.getName().equals("WaterGoblin") && card2.getName().equals("WaterSpell")) || card1.getName().equals("WaterSpell") && card2.getName().equals("WaterGoblin")) {
            return "specialfight";
        }
        else {
            return "elementfight";
        }
    }

    private String checkNames(Card card1, Card card2, User player1, User player2) {
        if (card1.getName().equals("Dragon") && card2.getName().equals("WaterGoblin")) {
            battleLog.addEntry("Goblins are too afraid of Dragons to attack.");
            battleLog.addEntry("Player " + player1.getUsername() + " with card " + card1.getName() + " won!");
            return player1.getUsername();
        }
        if (card1.getName().equals("WaterSpell") && card2.getName().equals("Knight")) {
            battleLog.addEntry("The armor of Knights is so heavy that WaterSpells make them drown them instantly.");
            battleLog.addEntry("Player " + player1.getUsername() + " with card " + card1.getName() + " won!");
            return player1.getUsername();
        }
        if (card1.getName().equals("FireElf") && card2.getName().equals("Dragon")) {
            battleLog.addEntry("The FireElves know Dragons since they were little and can evade their attacks.");
            battleLog.addEntry("Player " + player1.getUsername() + " with card " + card1.getName() + " won!");
            return player1.getUsername();
        }
        //Unique Feature
        if (card1.getName().equals("WaterGoblin") && card2.getName().equals("WaterSpell")) {
            battleLog.addEntry("The WaterGoblin can glide over the Water and evades the WaterSpell");
            battleLog.addEntry("Player " + player1.getUsername() + " with card " + card1.getName() + " won!");
            return player1.getUsername();
        } else
            return player2.getUsername();
    }

    private double checkElementals(Card card1, Card card2) {
        if (card1.getElementType().equals("WATER") && card2.getElementType().equals("FIRE")) {
            battleLog.addEntry(card1.getElementType() + " is effective against " + card2.getElementType() + ". Damage is doubled to " + card1.getDamage()*2);
            return card1.getDamage()*2;
        }
        if (card1.getElementType().equals("FIRE") && card2.getElementType().equals("WATER")) {
            battleLog.addEntry(card1.getElementType() + " is not effective against " + card2.getElementType() + ". Damage is halved to " + card1.getDamage()/2);
            return card1.getDamage()/2;
        }
        if (card1.getElementType().equals("FIRE") && card2.getElementType().equals("NORMAL")) {
            battleLog.addEntry(card1.getElementType() + " is effective against " + card2.getElementType() + ". Damage is doubled to " + card1.getDamage()*2);
            return card1.getDamage()*2;
        }
        if (card1.getElementType().equals("NORMAL") && card2.getElementType().equals("FIRE")) {
            battleLog.addEntry(card1.getElementType() + " is not effective against " + card2.getElementType() + ". Damage is halved to " + card1.getDamage()/2);
            return card1.getDamage()/2;
        }
        if (card1.getElementType().equals("NORMAL") && card2.getElementType().equals("WATER")) {
            battleLog.addEntry(card1.getElementType() + " is effective against " + card2.getElementType() + ". Damage is doubled to " + card1.getDamage()*2);
            return card1.getDamage()*2;
        }
        if (card1.getElementType().equals("WATER") && card2.getElementType().equals("NORMAL")) {
            battleLog.addEntry(card1.getElementType() + " is not effective against " + card2.getElementType() + ". Damage is halved to " + card1.getDamage()/2);
            return card1.getDamage()/2;
        }
        battleLog.addEntry("No effects");
        return card1.getDamage();
    }

    private void updateScoreInDatabase(User user) {
        String query = "UPDATE users SET score = ? WHERE id = ?";

        try (Connection conn = Database.getInstance().connect(); // Verbindung zur Datenbank
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, user.getScore()); // Neuer Score
            stmt.setObject(2, user.getId()); // Benutzer-ID
            stmt.executeUpdate(); // Ausführen der Update-Anweisung
        } catch (SQLException e) {
            e.printStackTrace(); // Fehler protokollieren
            battleLog.addEntry("Error updating score for user " + user.getUsername() + ": " + e.getMessage());
        }
    }


    private List<String> getCardIdsFromDeck(UUID userId) throws SQLException {
        String query = "SELECT c.card_id FROM deck d " +
                "JOIN cards c ON d.card_id = c.card_id " +
                "WHERE d.user_id = ? " +
                "LIMIT 4";  // Stelle sicher, dass nur 4 Karten abgerufen werden

        List<String> cardIds = new ArrayList<>();

        try (Connection conn = Database.getInstance().connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setObject(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                cardIds.add(rs.getString("card_id")); // Hole die card_id
            }
        }
        return cardIds;
    }


    public void addCardsToUserStack(UUID winnerId, List<String> cardIds) throws SQLException {
        String query = "INSERT INTO user_cards (user_id, card_id) VALUES (?, ?)";

        try (Connection conn = Database.getInstance().connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            for (String cardId : cardIds) {
                stmt.setObject(1, winnerId);
                stmt.setString(2, cardId);
                stmt.addBatch(); // Batch für mehrere Karten
            }
            stmt.executeBatch();
        }
    }

    public void removeCardsFromDeck(UUID loserId, List<String> cardIds) throws SQLException {
        String query = "DELETE FROM deck WHERE user_id = ? AND card_id = ?";

        try (Connection conn = Database.getInstance().connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            for (String cardId : cardIds) {
                stmt.setObject(1, loserId);
                stmt.setString(2, cardId);
                stmt.addBatch(); // Batch für mehrere Karten
            }
            stmt.executeBatch();
        }
    }

    public void removeCardsFromStack(UUID loserId, List<String> cardIds) throws SQLException {
        String query = "DELETE FROM user_cards WHERE user_id = ? AND card_id = ?";

        try (Connection conn = Database.getInstance().connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            for (String cardId : cardIds) {
                stmt.setObject(1, loserId);
                stmt.setString(2, cardId);
                stmt.addBatch(); // Batch für mehrere Karten
            }
            stmt.executeBatch();
        }
    }

    private void refillLoserDeck(User loser) {
        String selectQuery = """
        SELECT card_id
        FROM user_cards
        WHERE user_id = ?
        AND card_id NOT IN (
            SELECT card_id
            FROM deck
            WHERE user_id = ?
        )
        LIMIT 4
    """;

        String insertQuery = """
        INSERT INTO deck (user_id, card_id)
        VALUES (?, ?)
    """;

        try (Connection conn = Database.getInstance().connect();
             PreparedStatement selectStmt = conn.prepareStatement(selectQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {

            // Karten aus dem Stack auswählen
            selectStmt.setObject(1, loser.getId());
            selectStmt.setObject(2, loser.getId());
            ResultSet rs = selectStmt.executeQuery();

            // Karten ins Deck einfügen
            while (rs.next()) {
                String cardId = rs.getString("card_id");
                insertStmt.setObject(1, loser.getId());
                insertStmt.setString(2, cardId);
                insertStmt.executeUpdate();
            }

            battleLog.addEntry("Deck for user " + loser.getUsername() + " refilled with 4 cards.");
        } catch (SQLException e) {
            e.printStackTrace();
            battleLog.addEntry("Error refilling deck for user " + loser.getUsername() + ": " + e.getMessage());
        }
    }

}
