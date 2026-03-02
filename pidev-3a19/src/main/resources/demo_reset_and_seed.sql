-- =============================================================================
-- FARMTECH - Demo Reset & Seed
-- =============================================================================
-- This script: 1) Drops all tables  2) Creates fresh schema with all columns
--              3) Seeds realistic demo data for the whole project
-- Run in MySQL: mysql -u root -p 3a19 < demo_reset_and_seed.sql
-- Or in phpMyAdmin: select 3a19, paste and execute
-- =============================================================================

USE 3a19;

SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------------------------------
-- 1. DROP ALL TABLES (reverse dependency order)
-- -----------------------------------------------------------------------------
DROP TABLE IF EXISTS favoris;
DROP TABLE IF EXISTS produit_historique;
DROP TABLE IF EXISTS diagnostique;
DROP TABLE IF EXISTS materiel;
DROP TABLE IF EXISTS probleme;
DROP TABLE IF EXISTS production;
DROP TABLE IF EXISTS plantation;
DROP TABLE IF EXISTS produit;
DROP TABLE IF EXISTS utilisateur;

SET FOREIGN_KEY_CHECKS = 1;

-- -----------------------------------------------------------------------------
-- 2. CREATE SCHEMA (all tables with complete columns)
-- -----------------------------------------------------------------------------

-- Utilisateurs (admin + fermiers)
CREATE TABLE utilisateur (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

-- Produits (stock)
CREATE TABLE produit (
    idProduit INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    quantite INT NOT NULL DEFAULT 0,
    unite VARCHAR(50) DEFAULT NULL,
    dateExpiration DATE DEFAULT NULL,
    imagePath VARCHAR(500) DEFAULT NULL
);

-- Plantations (jeu + production)
CREATE TABLE plantation (
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

-- Production (récoltes)
CREATE TABLE production (
    idProduction INT AUTO_INCREMENT PRIMARY KEY,
    quantiteProduite FLOAT NOT NULL,
    dateRecolte DATE NOT NULL,
    qualite VARCHAR(100) NOT NULL,
    etat VARCHAR(100) NOT NULL
);

-- Matériels (liés aux produits)
CREATE TABLE materiel (
    idMateriel INT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    etat VARCHAR(100) NOT NULL,
    dateAchat DATE DEFAULT NULL,
    cout DOUBLE NOT NULL DEFAULT 0,
    idProduit INT NOT NULL,
    CONSTRAINT fk_materiel_produit FOREIGN KEY (idProduit) REFERENCES produit(idProduit) ON DELETE CASCADE
);

-- Problèmes (support fermiers)
CREATE TABLE probleme (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_utilisateur INT NULL,
    type VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    gravite VARCHAR(50) NOT NULL,
    date_detection DATETIME NOT NULL,
    etat VARCHAR(50) NOT NULL,
    photos TEXT NULL,
    id_plantation INT NULL,
    id_produit INT NULL,
    meteo_snapshot TEXT NULL,
    id_admin_assignee INT NULL,
    CONSTRAINT fk_probleme_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES utilisateur(id) ON DELETE SET NULL,
    CONSTRAINT fk_probleme_plantation FOREIGN KEY (id_plantation) REFERENCES plantation(id) ON DELETE SET NULL,
    CONSTRAINT fk_probleme_produit FOREIGN KEY (id_produit) REFERENCES produit(idProduit) ON DELETE SET NULL,
    CONSTRAINT fk_probleme_admin_assignee FOREIGN KEY (id_admin_assignee) REFERENCES utilisateur(id) ON DELETE SET NULL
);

-- Diagnostics (réponses aux problèmes)
CREATE TABLE diagnostique (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_probleme INT NOT NULL,
    cause TEXT NOT NULL,
    solution_proposee TEXT NOT NULL,
    date_diagnostique DATETIME NOT NULL,
    resultat VARCHAR(100) NOT NULL,
    medicament TEXT NULL,
    approuve TINYINT(1) NOT NULL DEFAULT 0,
    num_revision INT NOT NULL DEFAULT 1,
    feedback_fermier VARCHAR(20) NULL,
    feedback_commentaire TEXT NULL,
    date_feedback DATETIME NULL,
    id_admin_diagnostiqueur INT NULL,
    CONSTRAINT fk_diagnostique_probleme FOREIGN KEY (id_probleme) REFERENCES probleme(id) ON DELETE CASCADE,
    CONSTRAINT fk_diagnostique_admin FOREIGN KEY (id_admin_diagnostiqueur) REFERENCES utilisateur(id) ON DELETE SET NULL
);

CREATE INDEX idx_diag_probleme_revision ON diagnostique(id_probleme, num_revision);

-- Historique produit (traçabilité)
CREATE TABLE produit_historique (
    idHistorique INT AUTO_INCREMENT PRIMARY KEY,
    idProduit INT NOT NULL,
    typeEvenement VARCHAR(50) NOT NULL,
    quantiteAvant INT DEFAULT NULL,
    quantiteApres INT DEFAULT NULL,
    dateEvenement DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    commentaire VARCHAR(500) DEFAULT NULL,
    CONSTRAINT fk_historique_produit FOREIGN KEY (idProduit) REFERENCES produit(idProduit) ON DELETE CASCADE
);

-- Favoris
CREATE TABLE favoris (
    idProduit INT NOT NULL,
    dateAjout DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (idProduit),
    CONSTRAINT fk_favoris_produit FOREIGN KEY (idProduit) REFERENCES produit(idProduit) ON DELETE CASCADE
);

-- -----------------------------------------------------------------------------
-- 3. SEED DATA
-- -----------------------------------------------------------------------------

-- Utilisateurs (mot_de_passe = "demo123" en clair pour demo - à hasher en prod)
INSERT INTO utilisateur (nom, email, mot_de_passe, role) VALUES
('Admin FARMTECH', 'admin@farmtech.tn', 'demo123', 'ADMIN'),
('Dr. Karim Ben Salem', 'karim.bensalem@farmtech.tn', 'demo123', 'ADMIN'),
('Mohamed Amine', 'mohamed.amine@email.tn', 'demo123', 'FARMER'),
('Fatma Trabelsi', 'fatma.trabelsi@email.tn', 'demo123', 'FARMER'),
('Ali Mansour', 'ali.mansour@email.tn', 'demo123', 'FARMER');

-- Produits (imagePath = uploads/produits/xxx.png - run create_demo_images.ps1)
-- All products are plant/planting related
INSERT INTO produit (nom, quantite, unite, dateExpiration, imagePath) VALUES
('Tomates cerises', 150, 'kg', DATE_ADD(CURDATE(), INTERVAL 14 DAY), 'uploads/produits/tomates_cerises.png'),
('Pommes de terre Bintje', 500, 'kg', DATE_ADD(CURDATE(), INTERVAL 45 DAY), 'uploads/produits/pommes_de_terre.png'),
('Poivrons doux', 80, 'kg', DATE_ADD(CURDATE(), INTERVAL 10 DAY), 'uploads/produits/poivrons.png'),
('Engrais NPK', 200, 'sac', DATE_ADD(CURDATE(), INTERVAL 365 DAY), 'uploads/produits/engrais.png'),
('Fongicide cuivrique', 50, 'L', DATE_ADD(CURDATE(), INTERVAL 180 DAY), 'uploads/produits/fongicide.png'),
('Semences blé', 1000, 'kg', DATE_ADD(CURDATE(), INTERVAL 730 DAY), 'uploads/produits/semences_ble.png'),
('Plants de tomate', 200, 'unité', DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'uploads/produits/plants_tomate.png'),
('Semences de maïs', 500, 'kg', DATE_ADD(CURDATE(), INTERVAL 730 DAY), 'uploads/produits/semences_mais.png');

-- Plantations (jeu - 12 slots)
INSERT INTO plantation (nomPlant, variete, quantite, datePlante, saison, etat, stage, water_count, last_water_time, status, growth_speed, slot_index) VALUES
('Tomato', 'Cerise', 5, CURDATE(), 'Printemps', 'EN_ATTENTE', 1, 0, UNIX_TIMESTAMP() * 1000, 'ALIVE', 1.0, 0),
('Potato', 'Bintje', 10, CURDATE(), 'Printemps', 'EN_ATTENTE', 1, 0, UNIX_TIMESTAMP() * 1000, 'ALIVE', 1.0, 1),
('Papper', 'Doux', 3, CURDATE(), 'Été', 'EN_ATTENTE', 1, 0, UNIX_TIMESTAMP() * 1000, 'ALIVE', 1.0, 2),
('Tomato', 'Ronde', 4, CURDATE(), 'Printemps', 'EN_ATTENTE', 1, 0, UNIX_TIMESTAMP() * 1000, 'ALIVE', 1.0, 3),
('Potato', 'Charlotte', 8, CURDATE(), 'Printemps', 'EN_ATTENTE', 1, 0, UNIX_TIMESTAMP() * 1000, 'ALIVE', 1.0, 4),
('Papper', 'Piment', 2, CURDATE(), 'Été', 'EN_ATTENTE', 1, 0, UNIX_TIMESTAMP() * 1000, 'ALIVE', 1.0, 5),
('Tomato', 'Cœur de bœuf', 6, CURDATE(), 'Été', 'EN_ATTENTE', 1, 0, UNIX_TIMESTAMP() * 1000, 'ALIVE', 1.0, 6),
('Potato', 'Agata', 12, CURDATE(), 'Printemps', 'EN_ATTENTE', 1, 0, UNIX_TIMESTAMP() * 1000, 'ALIVE', 1.0, 7),
('Papper', 'Californien', 4, CURDATE(), 'Été', 'EN_ATTENTE', 1, 0, UNIX_TIMESTAMP() * 1000, 'ALIVE', 1.0, 8),
('Tomato', 'Roma', 7, CURDATE(), 'Printemps', 'EN_ATTENTE', 1, 0, UNIX_TIMESTAMP() * 1000, 'ALIVE', 1.0, 9),
('Potato', 'Mona Lisa', 9, CURDATE(), 'Printemps', 'EN_ATTENTE', 1, 0, UNIX_TIMESTAMP() * 1000, 'ALIVE', 1.0, 10),
('Papper', 'Vert', 5, CURDATE(), 'Été', 'EN_ATTENTE', 1, 0, UNIX_TIMESTAMP() * 1000, 'ALIVE', 1.0, 11);

-- Production (récoltes)
INSERT INTO production (quantiteProduite, dateRecolte, qualite, etat) VALUES
(120.5, DATE_SUB(CURDATE(), INTERVAL 3 DAY), 'Premium', 'Vendu'),
(85.0, DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'Standard', 'En stock'),
(45.0, CURDATE(), 'Premium', 'En attente');

-- Matériels
INSERT INTO materiel (nom, etat, dateAchat, cout, idProduit) VALUES
('Tracteur John Deere', 'Bon', '2023-03-15', 45000, 1),
('Pulvérisateur 500L', 'Neuf', '2024-01-10', 3500, 5),
('Semoir pneumatique', 'Bon', '2022-09-20', 12000, 6);

-- Problèmes (avec météo snapshot simulé, photos = chemins relatifs uploads/)
-- Photos: probleme 1 a 2 images, probleme 2 a 1 image (voir section Images ci-dessous)
-- Run create_demo_images.ps1 to create placeholder files in uploads/problemes/
INSERT INTO probleme (id_utilisateur, type, description, gravite, date_detection, etat, photos, id_plantation, id_produit, meteo_snapshot, id_admin_assignee) VALUES
(3, 'Maladie fongique', 'Taches brunes sur les feuilles de tomates, apparues après une période humide. Les plants du rang 3 sont les plus touchés.', 'Moyenne', DATE_SUB(NOW(), INTERVAL 5 DAY), 'DIAGNOSTIQUE_DISPONIBLE', 'problemes/demo_1_0.png;problemes/demo_1_1.png', 1, 1, '{"temp":22.5,"description":"Partiellement nuageux","humidity":65,"timestamp":"2025-02-28T10:30:00"}', 2),
(4, 'Ravageurs', 'Pucerons observés sur les poivrons. Colonies importantes sous les feuilles.', 'Faible', DATE_SUB(NOW(), INTERVAL 3 DAY), 'CLOTURE', 'problemes/demo_2_0.png', 3, 3, '{"temp":24.0,"description":"Ciel dégagé","humidity":55,"timestamp":"2025-03-01T14:00:00"}', 2),
(3, 'Carence nutritive', 'Jaunissement des feuilles basses sur pommes de terre. Sol argileux, peu drainé.', 'Élevée', DATE_SUB(NOW(), INTERVAL 2 DAY), 'EN_ATTENTE', NULL, 2, 2, '{"temp":19.0,"description":"Pluie","humidity":90,"timestamp":"2025-03-02T08:15:00"}', NULL),
(5, 'Maladie fongique', 'Mildiou suspecté sur tomates en serre. Humidité élevée ces derniers jours.', 'Critique', DATE_SUB(NOW(), INTERVAL 1 DAY), 'REOUVERT', 'problemes/demo_4_0.png', 4, 1, '{"temp":21.0,"description":"Couvert","humidity":85,"timestamp":"2025-03-02T16:45:00"}', 1);

-- Diagnostics
INSERT INTO diagnostique (id_probleme, cause, solution_proposee, date_diagnostique, resultat, medicament, approuve, num_revision, feedback_fermier, feedback_commentaire, date_feedback, id_admin_diagnostiqueur) VALUES
(1, 'Alternariose (Alternaria solani) favorisée par l''humidité et les températures douces.', 'Supprimer les feuilles atteintes. Appliquer un fongicide à base de cuivre (Bouillie bordelaise). Espacer les arrosages et éviter de mouiller le feuillage.', DATE_SUB(NOW(), INTERVAL 4 DAY), 'En attente', 'Bouillie bordelaise 20g/L', 1, 1, NULL, NULL, NULL, 2),
(2, 'Infestation de pucerons (Aphis spp.). Population en expansion rapide.', 'Pulvérisation de savon noir dilué (2%). Introduction de coccinelles si disponible. Suppression manuelle des foyers localisés.', DATE_SUB(NOW(), INTERVAL 2 DAY), 'Résolu', NULL, 1, 1, 'RESOLU', 'Savon noir efficace en 2 applications.', DATE_SUB(NOW(), INTERVAL 1 DAY), 2),
(3, 'Carence en magnésium et potassium. Sol compacté limitant l''absorption.', 'Apport d''engrais foliaire Mg-K. Améliorer le drainage (buttage). Analyse de sol recommandée.', NOW(), 'En cours', 'Engrais foliaire Mg-K', 0, 1, NULL, NULL, NULL, NULL),
(4, 'Mildiou (Phytophthora infestans). Conditions très favorables en serre.', 'Aération maximale. Traitement fongicide systémique. Réduction de l''humidité (arrosage au pied uniquement).', DATE_SUB(NOW(), INTERVAL 12 HOUR), 'En attente', 'Métalaxyl (dose selon AMM)', 1, 1, 'NON_RESOLU', 'Problème persiste après premier traitement.', DATE_SUB(NOW(), INTERVAL 2 HOUR), 1);

-- Révision pour problème 4 (réouverture)
INSERT INTO diagnostique (id_probleme, cause, solution_proposee, date_diagnostique, resultat, medicament, approuve, num_revision, feedback_fermier, feedback_commentaire, date_feedback, id_admin_diagnostiqueur) VALUES
(4, 'Mildiou résistant. Révision du protocole : double dose initiale + intervalle réduit.', 'Application Métalaxyl + Mancozèbe en alternance. Renouveler tous les 5 jours pendant 3 semaines. Contrôler l''hygrométrie en serre (<70%).', NOW(), 'En attente', 'Métalaxyl 2x + Mancozèbe en alternance', 0, 2, NULL, NULL, NULL, 1);

-- Historique produit (traçabilité)
INSERT INTO produit_historique (idProduit, typeEvenement, quantiteAvant, quantiteApres, dateEvenement, commentaire) VALUES
(1, 'ENTREE', NULL, 150, DATE_SUB(NOW(), INTERVAL 7 DAY), 'Récolte tomates cerises'),
(1, 'SORTIE', 150, 120, DATE_SUB(NOW(), INTERVAL 2 DAY), 'Vente coopérative'),
(2, 'ENTREE', NULL, 500, DATE_SUB(NOW(), INTERVAL 5 DAY), 'Récolte pommes de terre'),
(6, 'ENTREE', NULL, 1000, DATE_SUB(NOW(), INTERVAL 30 DAY), 'Commande semences');

-- Favoris
INSERT INTO favoris (idProduit, dateAjout) VALUES
(1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(3, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(8, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- -----------------------------------------------------------------------------
-- 4. IMAGES DEMO (plants and planting)
-- -----------------------------------------------------------------------------
-- Run 'mvn compile' then create_demo_images.ps1 to copy plant images:
--   - uploads/problemes/ : demo_1_0.png, demo_1_1.png, demo_2_0.png, demo_4_0.png
--   - uploads/produits/  : tomates_cerises.png, pommes_de_terre.png, etc.
-- -----------------------------------------------------------------------------

SELECT 'Demo reset and seed completed successfully.' AS status;
