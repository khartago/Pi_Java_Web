-- Migration: tables for emna features (traçabilité, favoris)
-- Run in schema 3a19.

-- Optional: add imagePath to produit if not present (emna Produit model)
-- ALTER TABLE produit ADD COLUMN IF NOT EXISTS imagePath VARCHAR(500) DEFAULT NULL;

-- Traçabilité: historique des événements produit
CREATE TABLE IF NOT EXISTS produit_historique (
    idHistorique INT AUTO_INCREMENT PRIMARY KEY,
    idProduit INT NOT NULL,
    typeEvenement VARCHAR(50) NOT NULL,
    quantiteAvant INT DEFAULT NULL,
    quantiteApres INT DEFAULT NULL,
    dateEvenement DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    commentaire VARCHAR(500) DEFAULT NULL,
    CONSTRAINT fk_historique_produit FOREIGN KEY (idProduit) REFERENCES produit(idProduit) ON DELETE CASCADE
);

-- Favoris / wishlist (idProduit only; extend with idUser if you add user binding)
CREATE TABLE IF NOT EXISTS favoris (
    idProduit INT NOT NULL,
    dateAjout DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (idProduit),
    CONSTRAINT fk_favoris_produit FOREIGN KEY (idProduit) REFERENCES produit(idProduit) ON DELETE CASCADE
);
