-- Aktiviert die UUID-Erweiterung, falls nicht vorhanden:
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TABLE IF EXISTS user_packages;
DROP TABLE IF EXISTS deck;
DROP TABLE IF EXISTS user_cards;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS cards;
DROP TABLE IF EXISTS packages;



-- Erstelle die users-Tabelle
CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       username VARCHAR(255) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       coins integer NOT NULL,
                       score integer NOT NULL,
                       token VARCHAR(255),
                       name VARCHAR(255),
                       bio TEXT,
                       image TEXT
);

-- Erstelle die packages-Tabelle
CREATE TABLE packages (
                          package_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          is_available BOOLEAN DEFAULT TRUE
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
);

-- Erstelle die user_packages-Tabelle
CREATE TABLE user_packages (
                               user_id UUID REFERENCES users(id) ON DELETE CASCADE,
                               package_id UUID REFERENCES packages(package_id) ON DELETE CASCADE,
                               PRIMARY KEY (user_id, package_id)
);

-- Table for cards
CREATE TABLE cards (
                       card_id VARCHAR(255) PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       damage DOUBLE PRECISION NOT NULL,
                       element_type VARCHAR CHECK (element_type IN ('FIRE', 'WATER', 'NORMAL')),
                       card_type VARCHAR CHECK (card_type IN ('MONSTER', 'SPELL')),
                       package_id UUID,
                       FOREIGN KEY (package_id) REFERENCES packages(package_id) ON DELETE SET NULL  -- Setze auf NULL, wenn das Paket nicht mehr verfügbar ist
);

-- Table for deck
CREATE TABLE deck (
                      user_id UUID REFERENCES users(id) ON DELETE CASCADE,  -- Verweist auf den Benutzer
                      card_id VARCHAR(255) REFERENCES cards(card_id) ON DELETE CASCADE,  -- Verweist auf die Karte im Deck
                      PRIMARY KEY (user_id, card_id)  -- Ein Benutzer kann mehrere Karten in seinem Deck haben
);

CREATE TABLE user_cards (
                            user_id UUID REFERENCES users(id) ON DELETE CASCADE,  -- Verweist auf den Benutzer
                            card_id VARCHAR(255) REFERENCES cards(card_id) ON DELETE CASCADE,  -- Verweist auf die Karte
                            PRIMARY KEY (user_id, card_id)  -- Ein Benutzer kann mehrere Karten besitzen
);