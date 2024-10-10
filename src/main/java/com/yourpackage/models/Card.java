package com.yourpackage.models;

import java.util.UUID;

enum elementType {
    FIRE,
    WATER,
    NORMAL
}
public abstract class Card {
    private UUID id;  // UUID, die von der Datenbank generiert wird
    private String name;
    private double damage;
    private elementType elementType;



    // Konstruktor
    public Card(String name, double damage, elementType elementType) {
        this.name = name;
        this.damage = damage;
        this.elementType = elementType;
        // id wird nicht gesetzt, da sie von der Datenbank generiert wird
    }

    // Getter und Setter
    public UUID getId() {
        return id;
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

    @Override
    public String toString() {
        return "Card{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", damage=" + damage +
                ", elementType=" + elementType +
                '}';
    }
}
