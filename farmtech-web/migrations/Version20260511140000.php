<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Creates `favoris` when missing (older DBs never ran this DDL because it lived only in Version20260420112907::down()).
 */
final class Version20260511140000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Create favoris table for marketplace / JavaFX parity when absent.';
    }

    public function up(Schema $schema): void
    {
        $this->addSql('CREATE TABLE IF NOT EXISTS favoris (
            idProduit INT NOT NULL,
            dateAjout DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
            PRIMARY KEY(idProduit)
        ) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('ALTER TABLE favoris DROP FOREIGN KEY IF EXISTS fk_favoris_produit');
        $this->addSql('ALTER TABLE favoris ADD CONSTRAINT fk_favoris_produit FOREIGN KEY (idProduit) REFERENCES produit (idProduit) ON DELETE CASCADE');
    }

    public function down(Schema $schema): void
    {
        $this->addSql('ALTER TABLE favoris DROP FOREIGN KEY IF EXISTS fk_favoris_produit');
        $this->addSql('DROP TABLE IF EXISTS favoris');
    }
}
