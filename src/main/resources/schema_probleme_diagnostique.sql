-- Schéma pour les entités Probleme et Diagnostique
-- Base : 3a19

CREATE TABLE IF NOT EXISTS probleme (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_utilisateur INT NULL,
    type VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    gravite VARCHAR(50) NOT NULL,
    date_detection DATETIME NOT NULL,
    etat VARCHAR(50) NOT NULL,
    photos TEXT NULL,
    CONSTRAINT fk_probleme_utilisateur
        FOREIGN KEY (id_utilisateur)
        REFERENCES utilisateur(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS diagnostique (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_probleme INT NOT NULL,
    cause TEXT NOT NULL,
    solution_proposee TEXT NOT NULL,
    date_diagnostique DATETIME NOT NULL,
    resultat VARCHAR(100) NOT NULL,
    medicament TEXT NULL,
    approuve TINYINT(1) NOT NULL DEFAULT 0,
    CONSTRAINT fk_diagnostique_probleme
        FOREIGN KEY (id_probleme)
        REFERENCES probleme(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);


CREATE TABLE IF NOT EXISTS utilisateur (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

