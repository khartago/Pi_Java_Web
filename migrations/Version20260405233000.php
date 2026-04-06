<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

final class Version20260405233000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Create legacy-compatible produit and materiel tables for the Symfony web port';
    }

    public function up(Schema $schema): void
    {
        $this->abortIf(
            $this->connection->getDatabasePlatform()->getName() !== 'mysql',
            'This migration can only be executed safely on mysql.',
        );

        $this->addSql(<<<'SQL'
            CREATE TABLE produit (
                idProduit INT AUTO_INCREMENT NOT NULL,
                nom VARCHAR(100) NOT NULL,
                quantite INT NOT NULL,
                unite VARCHAR(50) NOT NULL,
                dateExpiration DATE DEFAULT NULL,
                imagePath VARCHAR(255) DEFAULT NULL,
                PRIMARY KEY(idProduit)
            ) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB
        SQL);

        $this->addSql(<<<'SQL'
            CREATE TABLE materiel (
                idMateriel INT AUTO_INCREMENT NOT NULL,
                idProduit INT NOT NULL,
                nom VARCHAR(100) NOT NULL,
                etat VARCHAR(50) NOT NULL,
                dateAchat DATE NOT NULL,
                cout DOUBLE PRECISION NOT NULL,
                INDEX IDX_MATERIEL_PRODUIT (idProduit),
                PRIMARY KEY(idMateriel),
                CONSTRAINT FK_MATERIEL_PRODUIT FOREIGN KEY (idProduit) REFERENCES produit (idProduit) ON DELETE CASCADE
            ) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB
        SQL);
    }

    public function down(Schema $schema): void
    {
        $this->abortIf(
            $this->connection->getDatabasePlatform()->getName() !== 'mysql',
            'This migration can only be executed safely on mysql.',
        );

        $this->addSql('DROP TABLE materiel');
        $this->addSql('DROP TABLE produit');
    }
}
