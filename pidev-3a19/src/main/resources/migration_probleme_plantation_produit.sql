-- Migration: Lier probleme a plantation et produit
-- Run in schema 3a19

ALTER TABLE probleme ADD COLUMN id_plantation INT NULL;
ALTER TABLE probleme ADD COLUMN id_produit INT NULL;
ALTER TABLE probleme ADD CONSTRAINT fk_probleme_plantation FOREIGN KEY (id_plantation) REFERENCES plantation(id) ON DELETE SET NULL;
ALTER TABLE probleme ADD CONSTRAINT fk_probleme_produit FOREIGN KEY (id_produit) REFERENCES produit(idProduit) ON DELETE SET NULL;
