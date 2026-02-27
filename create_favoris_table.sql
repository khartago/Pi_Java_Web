-- Script SQL pour créer la table FAVORIS
-- À exécuter dans phpMyAdmin ou MySQL Workbench

CREATE TABLE IF NOT EXISTS favoris (
    idFavoris INT AUTO_INCREMENT PRIMARY KEY,
    idProduit INT NOT NULL,
    dateAjout TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_produit (idProduit),
    FOREIGN KEY (idProduit) REFERENCES produit(idProduit) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Index pour optimisation
CREATE INDEX idx_favoris_date ON favoris(dateAjout DESC);
CREATE INDEX idx_favoris_produit ON favoris(idProduit);

-- Exemple de données (optionnel)
-- INSERT INTO favoris (idProduit) VALUES (1), (2), (3);

