<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260404135157 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE diagnostique (id INT AUTO_INCREMENT NOT NULL, cause LONGTEXT NOT NULL, solution_proposee LONGTEXT NOT NULL, date_diagnostique DATETIME NOT NULL, resultat VARCHAR(100) NOT NULL, medicament LONGTEXT DEFAULT NULL, approuve TINYINT DEFAULT 0 NOT NULL, num_revision INT DEFAULT 1 NOT NULL, feedback_fermier VARCHAR(20) DEFAULT NULL, feedback_commentaire LONGTEXT DEFAULT NULL, date_feedback DATETIME DEFAULT NULL, id_probleme INT NOT NULL, id_admin_diagnostiqueur INT DEFAULT NULL, INDEX IDX_38C9AFE92A693B81 (id_probleme), INDEX IDX_38C9AFE9BBB786C9 (id_admin_diagnostiqueur), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('CREATE TABLE plantation (id INT AUTO_INCREMENT NOT NULL, nom_plant VARCHAR(100) NOT NULL, variete VARCHAR(100) NOT NULL, quantite INT NOT NULL, date_plante DATE NOT NULL, saison VARCHAR(50) NOT NULL, etat VARCHAR(50) NOT NULL, PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('CREATE TABLE probleme (id INT AUTO_INCREMENT NOT NULL, type VARCHAR(100) NOT NULL, description LONGTEXT NOT NULL, gravite VARCHAR(50) NOT NULL, date_detection DATETIME NOT NULL, etat VARCHAR(50) NOT NULL, photos LONGTEXT DEFAULT NULL, id_plantation INT DEFAULT NULL, id_produit INT DEFAULT NULL, meteo_snapshot LONGTEXT DEFAULT NULL, id_utilisateur INT DEFAULT NULL, id_admin_assignee INT DEFAULT NULL, INDEX IDX_7AB2D71450EAE44 (id_utilisateur), INDEX IDX_7AB2D714D55B460C (id_admin_assignee), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('CREATE TABLE production (id_production INT AUTO_INCREMENT NOT NULL, quantite_produite DOUBLE PRECISION NOT NULL, date_recolte DATE NOT NULL, qualite VARCHAR(100) NOT NULL, etat VARCHAR(100) NOT NULL, plantation_id INT NOT NULL, INDEX IDX_D3EDB1E019E5826C (plantation_id), PRIMARY KEY (id_production)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('CREATE TABLE utilisateur (id INT AUTO_INCREMENT NOT NULL, nom VARCHAR(100) NOT NULL, email VARCHAR(150) NOT NULL, mot_de_passe VARCHAR(255) NOT NULL, role VARCHAR(50) NOT NULL, UNIQUE INDEX UNIQ_1D1C63B3E7927C74 (email), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('CREATE TABLE messenger_messages (id BIGINT AUTO_INCREMENT NOT NULL, body LONGTEXT NOT NULL, headers LONGTEXT NOT NULL, queue_name VARCHAR(190) NOT NULL, created_at DATETIME NOT NULL, available_at DATETIME NOT NULL, delivered_at DATETIME DEFAULT NULL, INDEX IDX_75EA56E0FB7336F0E3BD61CE16BA31DBBF396750 (queue_name, available_at, delivered_at, id), PRIMARY KEY (id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci`');
        $this->addSql('ALTER TABLE diagnostique ADD CONSTRAINT FK_38C9AFE92A693B81 FOREIGN KEY (id_probleme) REFERENCES probleme (id) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE diagnostique ADD CONSTRAINT FK_38C9AFE9BBB786C9 FOREIGN KEY (id_admin_diagnostiqueur) REFERENCES utilisateur (id) ON DELETE SET NULL');
        $this->addSql('ALTER TABLE probleme ADD CONSTRAINT FK_7AB2D71450EAE44 FOREIGN KEY (id_utilisateur) REFERENCES utilisateur (id) ON DELETE SET NULL');
        $this->addSql('ALTER TABLE probleme ADD CONSTRAINT FK_7AB2D714D55B460C FOREIGN KEY (id_admin_assignee) REFERENCES utilisateur (id) ON DELETE SET NULL');
        $this->addSql('ALTER TABLE production ADD CONSTRAINT FK_D3EDB1E019E5826C FOREIGN KEY (plantation_id) REFERENCES plantation (id)');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE diagnostique DROP FOREIGN KEY FK_38C9AFE92A693B81');
        $this->addSql('ALTER TABLE diagnostique DROP FOREIGN KEY FK_38C9AFE9BBB786C9');
        $this->addSql('ALTER TABLE probleme DROP FOREIGN KEY FK_7AB2D71450EAE44');
        $this->addSql('ALTER TABLE probleme DROP FOREIGN KEY FK_7AB2D714D55B460C');
        $this->addSql('ALTER TABLE production DROP FOREIGN KEY FK_D3EDB1E019E5826C');
        $this->addSql('DROP TABLE diagnostique');
        $this->addSql('DROP TABLE plantation');
        $this->addSql('DROP TABLE probleme');
        $this->addSql('DROP TABLE production');
        $this->addSql('DROP TABLE utilisateur');
        $this->addSql('DROP TABLE messenger_messages');
    }
}
