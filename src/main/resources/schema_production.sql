-- Schema pour le module Production (plantation + production)
-- Base : 3a19 (connexion JDBC pointe deja sur ce schema)

CREATE TABLE IF NOT EXISTS plantation (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nomPlant VARCHAR(255) NOT NULL,
    variete VARCHAR(255) NOT NULL,
    quantite INT NOT NULL,
    datePlante DATE NOT NULL,
    saison VARCHAR(100) NOT NULL,
    etat VARCHAR(50) NOT NULL DEFAULT 'EN_ATTENTE',
    stage INT DEFAULT 1,
    water_count INT DEFAULT 0,
    last_water_time BIGINT DEFAULT 0,
    status VARCHAR(50) DEFAULT 'ALIVE',
    growth_speed DOUBLE DEFAULT 1.0,
    slot_index INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS production (
    idProduction INT AUTO_INCREMENT PRIMARY KEY,
    quantiteProduite FLOAT NOT NULL,
    dateRecolte DATE NOT NULL,
    qualite VARCHAR(100) NOT NULL,
    etat VARCHAR(100) NOT NULL
);
