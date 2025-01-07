package com.yourpackage.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BattleTest {

    private User testUser;
    private User testUser2;
    private Battle battle;

    @BeforeEach
    void setUp() {
         testUser = new User("TestUser1", "12345", 20, 100);
         testUser2 = new User("TestUser2", "12345", 20, 100);
         battle = new Battle();
    }

    @Test
    void testBattleWithHigherDamage() {
        // Arrange: Erstelle zwei Monsterkarten mit unterschiedlichen Schadenswerten
        MonsterCard card1 = new MonsterCard("1", "FireDragon", 50, "FIRE",  "MONSTER");
        MonsterCard card2 = new MonsterCard("2", "WaterDragon",30, "WATER", "MONSTER");
        testUser.addCardToDeck(card1);
        testUser2.addCardToDeck(card2);

        // Act: Simuliere den Kampf
        battle.addPlayer(testUser);
        battle.addPlayer(testUser2);

        String winner = battle.playRound();

        // Assert: Überprüfe, ob die Karte mit dem höheren Schadenswert gewonnen hat
        assertEquals(winner, "TestUser1");
    }

    @Test
    void testBattleWithEvenDamage() {
        // Arrange: Erstelle zwei Monsterkarten mit gleichen Schadenswerten
        MonsterCard card1 = new MonsterCard("1", "FireDragon", 50, "FIRE",  "MONSTER");
        MonsterCard card2 = new MonsterCard("2", "WaterDragon",50, "WATER", "MONSTER");
        testUser.addCardToDeck(card1);
        testUser2.addCardToDeck(card2);

        // Act: Simuliere den Kampf
        battle.addPlayer(testUser);
        battle.addPlayer(testUser2);

        String winner = battle.playRound();

        // Assert: Überprüfe, ob ein draw rauskommt
        assertEquals(winner, "draw");
    }
    @Test
    void testBattleWithEvenDamageButAsSpellCard() {
        // Arrange: Erstelle zwei Monsterkarten mit gleichen Schadenswerten aber unterschiedlichen Elementen
        SpellCard card1 = new SpellCard("1", "FireDragon", 50, "FIRE",  "SPELL");
        MonsterCard card2 = new MonsterCard("2", "WaterDragon",50, "WATER", "MONSTER");
        testUser.addCardToDeck(card1);
        testUser2.addCardToDeck(card2);

        // Act: Simuliere den Kampf
        battle.addPlayer(testUser);
        battle.addPlayer(testUser2);

        String winner = battle.playRound();

        // Assert: Überprüfe, ob die Wasserkarte gewinnt
        assertEquals(winner, "TestUser2");
    }

    @Test
    void testBattleWithEvenDamageButAsSpecialFight() {
        // Arrange: Erstelle zwei Monsterkarten mit gleichen Schadenswerten aber unterschiedlichen Elementen
        SpellCard card1 = new SpellCard("1", "WaterGoblin", 50, "Water",  "SPELL");
        MonsterCard card2 = new MonsterCard("2", "Dragon",50, "FIRE", "MONSTER");
        testUser.addCardToDeck(card1);
        testUser2.addCardToDeck(card2);

        // Act: Simuliere den Kampf
        battle.addPlayer(testUser);
        battle.addPlayer(testUser2);

        String winner = battle.playRound();

        // Assert: Überprüfe, ob der Drache gegen den Goblin gewinnt, obwohl sie den selben Schaden haben
        assertEquals(winner, "TestUser2");
    }

    @Test
    void testBattleWithEvenDamageButAsNewSpecialFight() {
        // Arrange: Erstelle zwei Monsterkarten mit gleichen Schadenswerten aber unterschiedlichen Elementen
        SpellCard card1 = new SpellCard("1", "WaterGoblin", 5, "Water",  "SPELL");
        SpellCard card2 = new SpellCard("2", "WaterSpell",50, "FIRE", "Spell");
        testUser.addCardToDeck(card1);
        testUser2.addCardToDeck(card2);

        // Act: Simuliere den Kampf
        battle.addPlayer(testUser);
        battle.addPlayer(testUser2);

        String winner = battle.playRound();

        // Assert: Überprüfe, ob der Goblin gegen den WaterSpell gewinnt, obwohl er weniger Schaden hat
        assertEquals(winner, "TestUser1");
    }

    @Test
    void testCheckFightWithTwoMonsterCards() {
        // Arrange
        MonsterCard card1 = new MonsterCard("1", "FireDragon", 50, "FIRE", "MONSTER");
        MonsterCard card2 = new MonsterCard("2", "WaterGoblin", 30, "WATER", "MONSTER");

        // Act
        String result = battle.checkFight(card1, card2);

        // Assert
        assertEquals("monsterfight", result, "The fight between two monster cards should result in 'monsterfight'.");
    }

    @Test
    void testCheckFightWithMonsterAndSpellCard() {
        // Arrange
        MonsterCard card1 = new MonsterCard("1", "FireDragon", 50, "FIRE", "MONSTER");
        SpellCard card2 = new SpellCard("2", "WaterSpell", 40, "WATER", "SPELL");

        // Act
        String result = battle.checkFight(card1, card2);

        // Assert
        assertEquals("elementfight", result, "The fight between a monster card and a spell card should result in 'elementfight'.");
    }


    @Test
    void testGetRandomCardFromDeck() {
        // Arrange: Füge mehrere Karten in das Deck des Nutzers ein
        MonsterCard card1 = new MonsterCard("1", "FireDragon", 50, "FIRE", "MONSTER");
        MonsterCard card2 = new MonsterCard("2", "WaterDragon", 30, "WATER", "MONSTER");
        testUser.addCardToDeck(card1);
        testUser.addCardToDeck(card2);

        // Act: Ziehe eine zufällige Karte
        Card randomCard = battle.getRandomCardFromDeck(testUser);

        // Assert: Überprüfe, ob die zurückgegebene Karte im Deck des Nutzers existiert
        assertTrue(testUser.getDeck().contains(randomCard), "The returned card should exist in the user's deck.");
    }

    @Test
    void testGetRandomCardFromEmptyDeck() {
        // Act: Versuche, eine Karte aus einem leeren Deck zu ziehen
        Card randomCard = battle.getRandomCardFromDeck(testUser);

        // Assert: Die Methode sollte null zurückgeben
        assertNull(randomCard, "The method should return null when the deck is empty.");
    }

}
