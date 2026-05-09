-- Tables Produit et Materiel pour le module Stock (base 3a19)
-- À exécuter dans la base 3a19 (phpMyAdmin ou client MySQL).

CREATE TABLE IF NOT EXISTS produit (
    idProduit INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    quantite INT NOT NULL DEFAULT 0,
    unite VARCHAR(50) DEFAULT NULL,
    dateExpiration DATE DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS materiel (
    idMateriel INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    etat VARCHAR(100) NOT NULL,
    dateAchat DATE DEFAULT NULL,
    cout DOUBLE NOT NULL DEFAULT 0,
    idProduit INT NOT NULL,
    CONSTRAINT fk_materiel_produit FOREIGN KEY (idProduit) REFERENCES produit(idProduit) ON DELETE CASCADE
);
