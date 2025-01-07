package com.yourpackage.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID; //für UUID


public class User {
    private UUID id;  // UUID für die ID, die aus der Datenbank kommt
    private String username;
    private String password;
    private int coins;
    private int score;
    private String token;
    private String name;
    private String bio;
    private String image;
    private List<Card> cardStack; // Stack von Karten gesamt
    private List<Card> deck; // Deck mit den 4 Karten für den Kampf

    // Konstruktor
    public User( String username, String password, int coins, int score) {
        this.username = username;
        this.password = password;
        this.coins = coins;
        this.score = score;
        this.cardStack = new ArrayList<>();
        this.deck = new ArrayList<>();
    }

    //Konstruktor für das login
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Überladener Konstruktor für vollständige Benutzerdaten
    public User(UUID id, String username, String password, String name, String bio, String image, int coins, int score, String token) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.bio = bio;
        this.image = image;
        this.coins = coins;
        this.score = score;
        this.token = token;
        this.cardStack = new ArrayList<>();
        this.deck = new ArrayList<>();
    }


    // Methode zum Hinzufügen einer Karte zum Kartenstack
    public void addCardToStack(Card card) {
        this.cardStack.add(card);
    }

    public void removeCardFromDeck(Card card) {
        this.deck.remove(card);
    }

    // Methode zum Hinzufügen einer Karte zum Deck
    public void addCardToDeck(Card card) {
        if (deck.size() < 4) {
            this.deck.add(card);
        } else {
            System.out.println("Deck is full. Cannot add more cards.");
        }
    }

    // Methode zum Abrufen des Decks
    public List<Card> getDeck() {
        return deck;
    }


    // Getter und Setter für die Eigenschaften

    public UUID getId() {return id;}

    public void setId(UUID id) {this.id = id;}

    public String getUsername() {return username;}

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {return password;}

    public void setPassword(String password) {this.password = password;}

    public int getScore() {return score;}

    public void setScore(int score) {this.score = score;}

    public String getToken() {return token;}

    public void setToken(String token) {this.token = token;}

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getBio() {return bio;}

    public void setBio(String bio) {this.bio = bio;}

    public String getImage() {return image;}

    public void setImage(String image) {this.image = image;}

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public List<Card> getCardStack() {
        return cardStack;
    }
}
