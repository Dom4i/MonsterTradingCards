package com.yourpackage.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User testUser;

    @BeforeEach
    void setUp() {
        // Arrange: Erstelle einen Test-User
        testUser = new User("TestUser1", "password123", 20, 100);
    }

    @Test
    void testAddCardToStack() {
        // Arrange: Erstelle eine neue Karte
        MonsterCard card = new MonsterCard("1", "FireDragon", 50, "FIRE", "MONSTER");

        // Act: Füge die Karte zum Kartenstack hinzu
        testUser.addCardToStack(card);

        // Assert: Überprüfe, ob die Karte im Kartenstack enthalten ist
        assertTrue(testUser.getCardStack().contains(card), "Card should be added to the card stack.");
    }

    @Test
    void testAddCardToDeck() {
        // Arrange: Erstelle eine neue Karte
        MonsterCard card = new MonsterCard("1", "FireDragon", 50, "FIRE", "MONSTER");

        // Act: Füge die Karte zum Deck hinzu
        testUser.addCardToDeck(card);

        // Assert: Überprüfe, ob die Karte im Deck enthalten ist
        assertTrue(testUser.getDeck().contains(card), "Card should be added to the deck.");
    }

    @Test
    void testAddCardToFullDeck() {
        // Arrange: Fülle das Deck mit 4 Karten
        for (int i = 1; i <= 4; i++) {
            testUser.addCardToDeck(new MonsterCard(String.valueOf(i), "Card" + i, 10 * i, "NORMAL", "MONSTER"));
        }

        // Act: Versuche, eine zusätzliche Karte hinzuzufügen
        MonsterCard extraCard = new MonsterCard("5", "ExtraCard", 50, "FIRE", "MONSTER");
        testUser.addCardToDeck(extraCard);

        // Assert: Überprüfe, ob die zusätzliche Karte nicht im Deck enthalten ist
        assertFalse(testUser.getDeck().contains(extraCard), "Card should not be added to a full deck.");
        assertEquals(4, testUser.getDeck().size(), "Deck size should not exceed 4 cards.");
    }
}
