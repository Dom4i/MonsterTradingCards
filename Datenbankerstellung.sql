-- Aktiviert die UUID-Erweiterung, falls nicht vorhanden:
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

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

CREATE TABLE packages (
                          package_id UUID PRIMARY KEY DEFAULT uuid_generate_v4()
);

-- Table for cards
CREATE TABLE cards (
                       card_id VARCHAR(255) PRIMARY KEY,
                       name VARCHAR(255) NOT NULL,
                       damage DOUBLE PRECISION NOT NULL,
                       element_type VARCHAR CHECK (element_type IN ('FIRE', 'WATER', 'NORMAL')),
                       card_type VARCHAR CHECK (card_type IN ('MONSTER', 'SPELL')),
                       package_id UUID,
                       FOREIGN KEY (package_id) REFERENCES packages(package_id) ON DELETE CASCADE
);