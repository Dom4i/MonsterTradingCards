package com.yourpackage.models;

public class Card {
    private String id;          // Eindeutige ID für die Karte
    private String name;        // Name der Karte
    private double damage;      // Schaden, den die Karte verursacht
    private elementType elementType; // Elementtyp (z.B. Wasser, Feuer, etc.)
    private CardType cardType;  // Kartentyp (Monster oder Zauber)

    // Enum für den Kartentyp
    public enum CardType {
        MONSTER,
        SPELL
    }
    public enum elementType {
        WATER,
        FIRE,
        NORMAL
    }

    // Konstruktor
    public Card(String id, String name, double damage, elementType elementType, CardType cardType) {
        this.id = id;
        this.name = name;
        this.damage = damage;
        this.elementType = elementType;
        this.cardType = cardType;
    }

    // Getter und Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public elementType getElementType() {
        return elementType;
    }

    public void setElementType(elementType elementType) {
        this.elementType = elementType;
    }

    public CardType getCardType() {
        return cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    // Überschreiben der toString-Methode für eine bessere Ausgabe
    @Override
    public String toString() {
        return "Card{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", damage=" + damage +
                ", elementType='" + elementType + '\'' +
                ", cardType=" + cardType +
                '}';
    }
}
