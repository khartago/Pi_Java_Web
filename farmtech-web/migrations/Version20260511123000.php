<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Align produit_historique index name with Doctrine metadata (schema:validate).
 */
final class Version20260511123000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Rename produit_historique idProduit index to Doctrine default for schema sync.';
    }

    public function up(Schema $schema): void
    {
        $this->addSql('ALTER TABLE produit_historique DROP FOREIGN KEY IF EXISTS fk_historique_produit');
        $this->addSql('DROP INDEX IF EXISTS idx_produit_historique_produit ON produit_historique');
        $this->addSql('DROP INDEX IF EXISTS IDX_4487ECE1391C87D5 ON produit_historique');
        $this->addSql('CREATE INDEX IDX_4487ECE1391C87D5 ON produit_historique (idProduit)');
        $this->addSql('ALTER TABLE produit_historique ADD CONSTRAINT fk_historique_produit FOREIGN KEY (idProduit) REFERENCES produit (idProduit) ON DELETE CASCADE');
    }

    public function down(Schema $schema): void
    {
        $this->addSql('ALTER TABLE produit_historique DROP FOREIGN KEY IF EXISTS fk_historique_produit');
        $this->addSql('DROP INDEX IF EXISTS IDX_4487ECE1391C87D5 ON produit_historique');
        $this->addSql('CREATE INDEX IDX_produit_historique_produit ON produit_historique (idProduit)');
        $this->addSql('ALTER TABLE produit_historique ADD CONSTRAINT fk_historique_produit FOREIGN KEY (idProduit) REFERENCES produit (idProduit) ON DELETE CASCADE');
    }
}
