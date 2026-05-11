<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Align DB with dual clients (Symfony + JavaFX):
 * - Reintroduce optional plantation columns used by the JavaFX mini-game (nullable defaults keep web-only rows valid).
 * - Restore produit_historique for product traceability (Java Traceabilite + future web API).
 */
final class Version20260511120000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Plantation game columns + produit_historique for web/JavaFX parity.';
    }

    public function up(Schema $schema): void
    {
        // --- Plantation: columns expected by JavaFX ProductionService / GameController ---
        $this->addSql('ALTER TABLE plantation ADD COLUMN IF NOT EXISTS stage INT NOT NULL DEFAULT 1');
        $this->addSql('ALTER TABLE plantation ADD COLUMN IF NOT EXISTS water_count INT NOT NULL DEFAULT 0');
        $this->addSql('ALTER TABLE plantation ADD COLUMN IF NOT EXISTS last_water_time BIGINT NOT NULL DEFAULT 0');
        $this->addSql("ALTER TABLE plantation ADD COLUMN IF NOT EXISTS status VARCHAR(50) NOT NULL DEFAULT 'ALIVE'");
        $this->addSql('ALTER TABLE plantation ADD COLUMN IF NOT EXISTS growth_speed DOUBLE PRECISION NOT NULL DEFAULT 1');
        $this->addSql('ALTER TABLE plantation ADD COLUMN IF NOT EXISTS slot_index INT NOT NULL DEFAULT 0');

        // --- Product history (matches Java ProduitHistoriqueDAO) ---
        $this->addSql('CREATE TABLE IF NOT EXISTS produit_historique (
            idHistorique INT AUTO_INCREMENT NOT NULL,
            idProduit INT NOT NULL,
            typeEvenement VARCHAR(50) NOT NULL,
            quantiteAvant INT DEFAULT NULL,
            quantiteApres INT DEFAULT NULL,
            dateEvenement DATETIME NOT NULL,
            commentaire VARCHAR(500) DEFAULT NULL,
            INDEX IDX_produit_historique_produit (idProduit),
            PRIMARY KEY(idHistorique)
        ) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');

        $this->addSql('ALTER TABLE produit_historique DROP FOREIGN KEY IF EXISTS fk_historique_produit');
        $this->addSql('ALTER TABLE produit_historique ADD CONSTRAINT fk_historique_produit FOREIGN KEY (idProduit) REFERENCES produit (idProduit) ON DELETE CASCADE');
    }

    public function down(Schema $schema): void
    {
        $this->addSql('ALTER TABLE produit_historique DROP FOREIGN KEY IF EXISTS fk_historique_produit');
        $this->addSql('DROP TABLE IF EXISTS produit_historique');

        $this->addSql('ALTER TABLE plantation DROP COLUMN IF EXISTS slot_index');
        $this->addSql('ALTER TABLE plantation DROP COLUMN IF EXISTS growth_speed');
        $this->addSql('ALTER TABLE plantation DROP COLUMN IF EXISTS status');
        $this->addSql('ALTER TABLE plantation DROP COLUMN IF EXISTS last_water_time');
        $this->addSql('ALTER TABLE plantation DROP COLUMN IF EXISTS water_count');
        $this->addSql('ALTER TABLE plantation DROP COLUMN IF EXISTS stage');
    }
}
