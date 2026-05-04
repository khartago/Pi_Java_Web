<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Brings the database in line with the current Doctrine mapping (diff from schema:update --dump-sql),
 * using SQL compatible with MariaDB 10.4 (no RENAME INDEX).
 */
final class Version20260503190000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Schema sync: ORM mapping alignment (orphan table, index names, column types).';
    }

    public function up(Schema $schema): void
    {
        $this->addSql('DROP TABLE IF EXISTS produit_historique');

        $this->addSql('ALTER TABLE affectation CHANGE dateRetour dateRetour DATE DEFAULT NULL');

        $this->addSql('ALTER TABLE diagnostique CHANGE feedback_fermier feedback_fermier VARCHAR(20) DEFAULT NULL, CHANGE date_feedback date_feedback DATETIME DEFAULT NULL');

        $this->addSql('ALTER TABLE diagnostique DROP FOREIGN KEY IF EXISTS fk_diagnostique_probleme');
        $this->addSql('DROP INDEX IF EXISTS fk_diagnostique_probleme ON diagnostique');
        $this->addSql('CREATE INDEX IDX_38C9AFE92A693B81 ON diagnostique (id_probleme)');
        $this->addSql('ALTER TABLE diagnostique ADD CONSTRAINT fk_diagnostique_probleme FOREIGN KEY (id_probleme) REFERENCES probleme (id) ON DELETE CASCADE');

        $this->addSql('ALTER TABLE employe CHANGE email email VARCHAR(100) DEFAULT NULL');

        $this->addSql('ALTER TABLE plantation DROP COLUMN IF EXISTS stage, DROP COLUMN IF EXISTS water_count, DROP COLUMN IF EXISTS last_water_time, DROP COLUMN IF EXISTS status, DROP COLUMN IF EXISTS growth_speed, DROP COLUMN IF EXISTS slot_index, CHANGE nomPlant nomPlant VARCHAR(100) NOT NULL, CHANGE variete variete VARCHAR(100) NOT NULL, CHANGE saison saison VARCHAR(50) NOT NULL, CHANGE etat etat VARCHAR(50) NOT NULL');

        $this->addSql('ALTER TABLE probleme DROP FOREIGN KEY IF EXISTS fk_probleme_plantation');
        $this->addSql('ALTER TABLE probleme DROP FOREIGN KEY IF EXISTS fk_probleme_produit');
        $this->addSql('DROP INDEX IF EXISTS fk_probleme_plantation ON probleme');
        $this->addSql('DROP INDEX IF EXISTS fk_probleme_produit ON probleme');

        $this->addSql('ALTER TABLE production CHANGE quantiteProduite quantiteProduite DOUBLE PRECISION NOT NULL');

        $this->addSql('ALTER TABLE produit CHANGE dateExpiration dateExpiration DATE DEFAULT NULL, CHANGE imagePath imagePath VARCHAR(255) DEFAULT NULL, CHANGE prix prix DOUBLE PRECISION DEFAULT NULL');

        $this->addSql('ALTER TABLE messenger_messages CHANGE delivered_at delivered_at DATETIME DEFAULT NULL');
    }

    public function down(Schema $schema): void
    {
        $this->throwIrreversibleMigrationException();
    }
}
