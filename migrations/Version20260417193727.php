<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260417193727 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE affectation (idAffectation INT AUTO_INCREMENT NOT NULL, dateAffectation DATE NOT NULL COMMENT \'(DC2Type:date_immutable)\', dateRetour DATE DEFAULT NULL COMMENT \'(DC2Type:date_immutable)\', note LONGTEXT DEFAULT NULL, idMateriel INT NOT NULL, idEmploye INT NOT NULL, INDEX IDX_F4DD61D34B719ACA (idMateriel), INDEX IDX_F4DD61D3E8BDB84B (idEmploye), PRIMARY KEY(idAffectation)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('CREATE TABLE employe (idEmploye INT AUTO_INCREMENT NOT NULL, nom VARCHAR(50) NOT NULL, prenom VARCHAR(50) NOT NULL, poste VARCHAR(50) NOT NULL, email VARCHAR(100) DEFAULT NULL, PRIMARY KEY(idEmploye)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('CREATE TABLE messenger_messages (id BIGINT AUTO_INCREMENT NOT NULL, body LONGTEXT NOT NULL, headers LONGTEXT NOT NULL, queue_name VARCHAR(190) NOT NULL, created_at DATETIME NOT NULL COMMENT \'(DC2Type:datetime_immutable)\', available_at DATETIME NOT NULL COMMENT \'(DC2Type:datetime_immutable)\', delivered_at DATETIME DEFAULT NULL COMMENT \'(DC2Type:datetime_immutable)\', INDEX IDX_75EA56E0FB7336F0E3BD61CE16BA31DBBF396750 (queue_name, available_at, delivered_at, id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('ALTER TABLE affectation ADD CONSTRAINT FK_F4DD61D34B719ACA FOREIGN KEY (idMateriel) REFERENCES materiel (idMateriel) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE affectation ADD CONSTRAINT FK_F4DD61D3E8BDB84B FOREIGN KEY (idEmploye) REFERENCES employe (idEmploye) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE materiel DROP FOREIGN KEY FK_MATERIEL_PRODUIT');
        $this->addSql('ALTER TABLE materiel CHANGE dateAchat dateAchat DATE NOT NULL COMMENT \'(DC2Type:date_immutable)\'');
        $this->addSql('DROP INDEX idx_materiel_produit ON materiel');
        $this->addSql('CREATE INDEX IDX_18D2B091391C87D5 ON materiel (idProduit)');
        $this->addSql('ALTER TABLE materiel ADD CONSTRAINT FK_MATERIEL_PRODUIT FOREIGN KEY (idProduit) REFERENCES produit (idProduit) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE produit CHANGE dateExpiration dateExpiration DATE DEFAULT NULL COMMENT \'(DC2Type:date_immutable)\'');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE affectation DROP FOREIGN KEY FK_F4DD61D34B719ACA');
        $this->addSql('ALTER TABLE affectation DROP FOREIGN KEY FK_F4DD61D3E8BDB84B');
        $this->addSql('DROP TABLE affectation');
        $this->addSql('DROP TABLE employe');
        $this->addSql('DROP TABLE messenger_messages');
        $this->addSql('ALTER TABLE materiel DROP FOREIGN KEY FK_18D2B091391C87D5');
        $this->addSql('ALTER TABLE materiel CHANGE dateAchat dateAchat DATE NOT NULL');
        $this->addSql('DROP INDEX idx_18d2b091391c87d5 ON materiel');
        $this->addSql('CREATE INDEX IDX_MATERIEL_PRODUIT ON materiel (idProduit)');
        $this->addSql('ALTER TABLE materiel ADD CONSTRAINT FK_18D2B091391C87D5 FOREIGN KEY (idProduit) REFERENCES produit (idProduit) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE produit CHANGE dateExpiration dateExpiration DATE DEFAULT NULL');
    }
}
