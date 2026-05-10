-- ============================================================
--  Migration : table promotion
--  À exécuter dans la base 3a19 (phpMyAdmin ou client MySQL)
-- ============================================================

CREATE TABLE IF NOT EXISTS promotion (
    idPromotion      INT AUTO_INCREMENT PRIMARY KEY,
    nom              VARCHAR(255)   NOT NULL,
    -- POURCENTAGE | MONTANT_FIXE | QUANTITE_GRATUITE
    type             ENUM('POURCENTAGE','MONTANT_FIXE','QUANTITE_GRATUITE')
                     NOT NULL DEFAULT 'POURCENTAGE',
    valeur           DOUBLE         NOT NULL DEFAULT 0,   -- % ou montant fixe
    quantiteMin      INT            NOT NULL DEFAULT 1,   -- seuil déclencheur
    quantiteGratuite INT            NOT NULL DEFAULT 0,   -- pour QUANTITE_GRATUITE
    dateDebut        DATE           DEFAULT NULL,
    dateFin          DATE           DEFAULT NULL,
    -- 0 = applicable à tous les produits
    idProduit        INT            NOT NULL DEFAULT 0,
    CONSTRAINT fk_promotion_produit
        FOREIGN KEY (idProduit) REFERENCES produit(idProduit)
        ON DELETE CASCADE
        -- NOTE : si idProduit = 0 (global), la FK ne s'applique pas.
        -- Utilisez un trigger ou gérez-le côté applicatif.
);

-- Index pour accélérer findActiveForProduct
CREATE INDEX IF NOT EXISTS idx_promo_produit_dates
    ON promotion (idProduit, dateDebut, dateFin);

-- ============================================================
--  Données de démonstration
-- ============================================================

-- Promo 1 : -15% sur tous les produits (idProduit=0 → global)
-- Note : la FK bloque idProduit=0 si la contrainte est stricte.
-- Désactivez temporairement la FK ou supprimez-la pour les promos globales.
-- INSERT INTO promotion (nom, type, valeur, quantiteMin, dateDebut, dateFin, idProduit)
-- VALUES ('Soldes été -15%', 'POURCENTAGE', 15, 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 0);

-- Promo 2 : -5 TND sur le produit id=1
-- INSERT INTO promotion (nom, type, valeur, quantiteMin, dateDebut, dateFin, idProduit)
-- VALUES ('Remise fidélité', 'MONTANT_FIXE', 5.00, 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 15 DAY), 1);

-- Promo 3 : 3 achetés = 1 gratuit sur le produit id=2
-- INSERT INTO promotion (nom, type, valeur, quantiteMin, quantiteGratuite, dateDebut, dateFin, idProduit)
-- VALUES ('3 pour 2', 'QUANTITE_GRATUITE', 0, 3, 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY), 2);
