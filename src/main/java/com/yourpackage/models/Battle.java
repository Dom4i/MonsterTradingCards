package com.yourpackage.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

        // Siege der Spieler zählen
        int player1Wins = 0;
        int player2Wins = 0;

        // Fünf Runden spielen
        for (int round = 1; round <= 5; round++) {
            battleLog.addEntry("\nRound " + round + " started!");

            // Runde spielen und prüfen, wer gewonnen hat
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
                battleLog.addEntry("Current Score: " + players.get(0).getUsername() + " " +  player1Wins + " | " + players.get(1).getUsername() + " " + player2Wins);

            // Überprüfen, ob einer der Spieler bereits 3 Runden gewonnen hat
            if (player1Wins >= 3 || player2Wins >= 3) {
                break;
            }
        }

        // Überprüfen, wer insgesamt 3 oder mehr Runden gewonnen hat
        if (player1Wins >= 3) {
            battleLog.addEntry(players.get(0).getUsername() + " wins the battle!");
            // Hier später die Karten und den Score aktualisieren
        } else if (player2Wins >= 3) {
            battleLog.addEntry(players.get(1).getUsername() + " wins the battle!");
            // Hier später die Karten und den Score aktualisieren
        } else {
            battleLog.addEntry("It's a draw! No player wins the battle.");
        }

        // Ausgabe des Battle Logs mit Verzögerung
        System.out.println("Battle Log: ");
        for (String logEntry : battleLog.getLogs()) {
            System.out.println(logEntry);
            try {
                Thread.sleep(500); // Verzögerung von 1 Sekunde
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        players.clear();
        battleLog.clear();
    }

    private String playRound() {
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
            return player1.getUsername(); // Spieler 1 gewinnt
        } else if (card1.getDamage() < card2.getDamage()) {
            return player2.getUsername(); // Spieler 2 gewinnt
        } else {
            return ""; // Unentschieden
        }
    }


    private Card getRandomCardFromDeck(User user) {
        List<Card> deck = user.getDeck();
        if (deck.isEmpty()) {
            battleLog.addEntry(user.getUsername() + " has no cards left!");
            return null;
        }
        Random random = new Random();
        return deck.get(random.nextInt(deck.size()));
    }

    private String checkFight(Card card1, Card card2) {
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
}
