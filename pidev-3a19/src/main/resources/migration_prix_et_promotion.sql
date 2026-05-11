-- ============================================================
--  Migration : structure identique Symfony + Java
--  Base : 3a19  |  phpMyAdmin ou MySQL CLI
-- ============================================================

-- 1. Ajouter prixUnitaire à produit (si absent)
ALTER TABLE produit
    ADD COLUMN IF NOT EXISTS prixUnitaire DOUBLE NOT NULL DEFAULT 0;

-- 2. Créer la table promotion avec la structure exacte Symfony
--    + colonne idProduit pour la liaison Java
CREATE TABLE IF NOT EXISTS promotion (
    idPromotion     INT AUTO_INCREMENT PRIMARY KEY,
    nom             VARCHAR(255)  NOT NULL,
    description     TEXT          DEFAULT NULL,
    typeReduction   VARCHAR(30)   NOT NULL DEFAULT 'pourcentage',
    valeurReduction DOUBLE        NOT NULL DEFAULT 0,
    dateDebut       DATE          DEFAULT NULL,
    dateFin         DATE          DEFAULT NULL,
    quantiteMin     INT           NOT NULL DEFAULT 1,
    cumulable       TINYINT(1)    NOT NULL DEFAULT 0,
    actif           TINYINT(1)    NOT NULL DEFAULT 1,
    idProduit       INT           DEFAULT NULL
);

-- 3. Si la table existait déjà (créée par Symfony), ajouter les colonnes manquantes
ALTER TABLE promotion ADD COLUMN IF NOT EXISTS idProduit       INT        DEFAULT NULL;
ALTER TABLE promotion ADD COLUMN IF NOT EXISTS cumulable       TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE promotion ADD COLUMN IF NOT EXISTS actif           TINYINT(1) NOT NULL DEFAULT 1;
ALTER TABLE promotion ADD COLUMN IF NOT EXISTS description     TEXT       DEFAULT NULL;

-- 4. Index
CREATE INDEX IF NOT EXISTS idx_promo_produit ON promotion (idProduit);
CREATE INDEX IF NOT EXISTS idx_promo_dates   ON promotion (dateDebut, dateFin);
