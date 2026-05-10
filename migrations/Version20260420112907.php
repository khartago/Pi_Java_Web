<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260420112907 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // MariaDB < 10.5.2 has no RENAME INDEX; use DROP/ADD index + FK rebuild instead.
        // Composite idx_diag_probleme_revision backs fk_diagnostique_probleme — drop that FK before dropping the index.
        $this->addSql('ALTER TABLE diagnostique DROP FOREIGN KEY IF EXISTS fk_diagnostique_probleme');
        $this->addSql('DROP INDEX IF EXISTS idx_diag_probleme_revision ON diagnostique');
        $this->addSql('ALTER TABLE diagnostique CHANGE cause cause LONGTEXT NOT NULL, CHANGE solution_proposee solution_proposee LONGTEXT NOT NULL, CHANGE medicament medicament LONGTEXT DEFAULT NULL, CHANGE feedback_fermier feedback_fermier VARCHAR(20) DEFAULT NULL, CHANGE feedback_commentaire feedback_commentaire LONGTEXT DEFAULT NULL, CHANGE date_feedback date_feedback DATETIME DEFAULT NULL');
        $this->addSql('ALTER TABLE diagnostique DROP FOREIGN KEY IF EXISTS fk_diagnostique_admin');
        $this->addSql('DROP INDEX IF EXISTS fk_diagnostique_admin ON diagnostique');
        $this->addSql('CREATE INDEX IDX_38C9AFE9BBB786C9 ON diagnostique (id_admin_diagnostiqueur)');
        $this->addSql('ALTER TABLE diagnostique ADD CONSTRAINT fk_diagnostique_admin FOREIGN KEY (id_admin_diagnostiqueur) REFERENCES utilisateur (id) ON DELETE SET NULL');
        $this->addSql('ALTER TABLE diagnostique ADD CONSTRAINT fk_diagnostique_probleme FOREIGN KEY (id_probleme) REFERENCES probleme (id) ON DELETE CASCADE');

        $this->addSql('ALTER TABLE materiel DROP FOREIGN KEY IF EXISTS fk_materiel_produit');
        $this->addSql('DROP INDEX IF EXISTS fk_materiel_produit ON materiel');
        $this->addSql('ALTER TABLE materiel CHANGE nom nom VARCHAR(100) NOT NULL, CHANGE etat etat VARCHAR(50) NOT NULL, CHANGE dateAchat dateAchat DATE NOT NULL, CHANGE cout cout DOUBLE PRECISION NOT NULL');
        $this->addSql('CREATE INDEX IDX_18D2B091391C87D5 ON materiel (idProduit)');
        $this->addSql('ALTER TABLE materiel ADD CONSTRAINT fk_materiel_produit FOREIGN KEY (idProduit) REFERENCES produit (idProduit) ON DELETE CASCADE');

        $this->addSql('ALTER TABLE probleme DROP FOREIGN KEY IF EXISTS fk_probleme_utilisateur');
        $this->addSql('ALTER TABLE probleme DROP FOREIGN KEY IF EXISTS fk_probleme_admin_assignee');
        $this->addSql('DROP INDEX IF EXISTS fk_probleme_utilisateur ON probleme');
        $this->addSql('DROP INDEX IF EXISTS fk_probleme_admin_assignee ON probleme');
        $this->addSql('ALTER TABLE probleme CHANGE description description LONGTEXT NOT NULL, CHANGE photos photos LONGTEXT DEFAULT NULL, CHANGE meteo_snapshot meteo_snapshot LONGTEXT DEFAULT NULL');
        $this->addSql('CREATE INDEX IDX_7AB2D71450EAE44 ON probleme (id_utilisateur)');
        $this->addSql('CREATE INDEX IDX_7AB2D714D55B460C ON probleme (id_admin_assignee)');
        $this->addSql('ALTER TABLE probleme ADD CONSTRAINT fk_probleme_utilisateur FOREIGN KEY (id_utilisateur) REFERENCES utilisateur (id) ON DELETE SET NULL');
        $this->addSql('ALTER TABLE probleme ADD CONSTRAINT fk_probleme_admin_assignee FOREIGN KEY (id_admin_assignee) REFERENCES utilisateur (id) ON DELETE SET NULL');

        $this->addSql('ALTER TABLE produit ADD COLUMN IF NOT EXISTS prix DOUBLE PRECISION DEFAULT NULL, CHANGE nom nom VARCHAR(100) NOT NULL, CHANGE quantite quantite INT NOT NULL, CHANGE unite unite VARCHAR(50) NOT NULL, CHANGE dateExpiration dateExpiration DATE DEFAULT NULL, CHANGE imagePath imagePath VARCHAR(255) DEFAULT NULL');

        $this->addSql('ALTER TABLE utilisateur DROP INDEX IF EXISTS email');
        $this->addSql('CREATE UNIQUE INDEX UNIQ_1D1C63B3E7927C74 ON utilisateur (email)');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE favoris (idProduit INT NOT NULL, dateAjout DATETIME DEFAULT \'current_timestamp()\' NOT NULL, PRIMARY KEY (idProduit)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE plantation (id INT AUTO_INCREMENT NOT NULL, nomPlant VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, variete VARCHAR(255) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, quantite INT NOT NULL, datePlante DATE NOT NULL, saison VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, etat VARCHAR(50) CHARACTER SET utf8mb4 DEFAULT \'\'\'EN_ATTENTE\'\'\' NOT NULL COLLATE `utf8mb4_general_ci`, stage INT DEFAULT 1, water_count INT DEFAULT 0, last_water_time BIGINT DEFAULT 0, status VARCHAR(50) CHARACTER SET utf8mb4 DEFAULT \'\'\'ALIVE\'\'\' COLLATE `utf8mb4_general_ci`, growth_speed DOUBLE PRECISION DEFAULT \'1\', slot_index INT DEFAULT 0, PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE production (idProduction INT AUTO_INCREMENT NOT NULL, quantiteProduite FLOAT NOT NULL, dateRecolte DATE NOT NULL, qualite VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, etat VARCHAR(100) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, PRIMARY KEY (idProduction)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('CREATE TABLE produit_historique (idHistorique INT AUTO_INCREMENT NOT NULL, idProduit INT NOT NULL, typeEvenement VARCHAR(50) CHARACTER SET utf8mb4 NOT NULL COLLATE `utf8mb4_general_ci`, quantiteAvant INT DEFAULT NULL, quantiteApres INT DEFAULT NULL, dateEvenement DATETIME DEFAULT \'current_timestamp()\' NOT NULL, commentaire VARCHAR(500) CHARACTER SET utf8mb4 DEFAULT \'NULL\' COLLATE `utf8mb4_general_ci`, INDEX fk_historique_produit (idProduit), PRIMARY KEY (idHistorique)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_general_ci` ENGINE = InnoDB COMMENT = \'\' ');
        $this->addSql('ALTER TABLE favoris ADD CONSTRAINT `fk_favoris_produit` FOREIGN KEY (idProduit) REFERENCES produit (idProduit) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE produit_historique ADD CONSTRAINT `fk_historique_produit` FOREIGN KEY (idProduit) REFERENCES produit (idProduit) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE affectation DROP FOREIGN KEY FK_F4DD61D34B719ACA');
        $this->addSql('ALTER TABLE affectation DROP FOREIGN KEY FK_F4DD61D3E8BDB84B');
        $this->addSql('ALTER TABLE promotion_produit DROP FOREIGN KEY FK_71D81A1D139DF194');
        $this->addSql('ALTER TABLE promotion_produit DROP FOREIGN KEY FK_71D81A1DF347EFB');
        $this->addSql('ALTER TABLE recommandation DROP FOREIGN KEY FK_C7782A28391C87D5');
        $this->addSql('ALTER TABLE recommandation DROP FOREIGN KEY FK_C7782A284B719ACA');
        $this->addSql('DROP TABLE affectation');
        $this->addSql('DROP TABLE employe');
        $this->addSql('DROP TABLE promotion');
        $this->addSql('DROP TABLE promotion_produit');
        $this->addSql('DROP TABLE recommandation');
        $this->addSql('DROP TABLE messenger_messages');
        $this->addSql('ALTER TABLE diagnostique CHANGE cause cause TEXT NOT NULL, CHANGE solution_proposee solution_proposee TEXT NOT NULL, CHANGE medicament medicament TEXT DEFAULT NULL, CHANGE feedback_fermier feedback_fermier VARCHAR(20) DEFAULT \'NULL\', CHANGE feedback_commentaire feedback_commentaire TEXT DEFAULT NULL, CHANGE date_feedback date_feedback DATETIME DEFAULT \'NULL\'');
        $this->addSql('ALTER TABLE diagnostique DROP FOREIGN KEY fk_diagnostique_probleme');
        $this->addSql('CREATE INDEX idx_diag_probleme_revision ON diagnostique (id_probleme, num_revision)');
        $this->addSql('ALTER TABLE diagnostique ADD CONSTRAINT fk_diagnostique_probleme FOREIGN KEY (id_probleme) REFERENCES probleme (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE diagnostique RENAME INDEX idx_38c9afe9bbb786c9 TO fk_diagnostique_admin');
        $this->addSql('ALTER TABLE materiel CHANGE nom nom VARCHAR(255) NOT NULL, CHANGE etat etat VARCHAR(100) NOT NULL, CHANGE dateAchat dateAchat DATE DEFAULT \'NULL\', CHANGE cout cout DOUBLE PRECISION DEFAULT \'0\' NOT NULL');
        $this->addSql('ALTER TABLE materiel RENAME INDEX idx_18d2b091391c87d5 TO fk_materiel_produit');
        $this->addSql('ALTER TABLE probleme CHANGE description description TEXT NOT NULL, CHANGE photos photos TEXT DEFAULT NULL, CHANGE meteo_snapshot meteo_snapshot TEXT DEFAULT NULL');
        $this->addSql('ALTER TABLE probleme ADD CONSTRAINT `fk_probleme_plantation` FOREIGN KEY (id_plantation) REFERENCES plantation (id) ON DELETE SET NULL');
        $this->addSql('ALTER TABLE probleme ADD CONSTRAINT `fk_probleme_produit` FOREIGN KEY (id_produit) REFERENCES produit (idProduit) ON DELETE SET NULL');
        $this->addSql('CREATE INDEX fk_probleme_produit ON probleme (id_produit)');
        $this->addSql('CREATE INDEX fk_probleme_plantation ON probleme (id_plantation)');
        $this->addSql('ALTER TABLE probleme RENAME INDEX idx_7ab2d714d55b460c TO fk_probleme_admin_assignee');
        $this->addSql('ALTER TABLE probleme RENAME INDEX idx_7ab2d71450eae44 TO fk_probleme_utilisateur');
        $this->addSql('ALTER TABLE produit DROP prix, CHANGE nom nom VARCHAR(255) NOT NULL, CHANGE quantite quantite INT DEFAULT 0 NOT NULL, CHANGE unite unite VARCHAR(50) DEFAULT \'NULL\', CHANGE dateExpiration dateExpiration DATE DEFAULT \'NULL\', CHANGE imagePath imagePath VARCHAR(500) DEFAULT \'NULL\'');
        $this->addSql('ALTER TABLE utilisateur RENAME INDEX uniq_1d1c63b3e7927c74 TO email');
    }
}
