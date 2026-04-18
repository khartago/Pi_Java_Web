<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260418193128 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('CREATE TABLE recommandation (idRecommandation INT AUTO_INCREMENT NOT NULL, priorite INT NOT NULL, raison LONGTEXT DEFAULT NULL, actif TINYINT(1) NOT NULL, idProduit INT NOT NULL, idMateriel INT NOT NULL, INDEX IDX_C7782A28391C87D5 (idProduit), INDEX IDX_C7782A284B719ACA (idMateriel), UNIQUE INDEX uniq_produit_materiel (idProduit, idMateriel), PRIMARY KEY(idRecommandation)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('ALTER TABLE recommandation ADD CONSTRAINT FK_C7782A28391C87D5 FOREIGN KEY (idProduit) REFERENCES produit (idProduit) ON DELETE CASCADE');
        $this->addSql('ALTER TABLE recommandation ADD CONSTRAINT FK_C7782A284B719ACA FOREIGN KEY (idMateriel) REFERENCES materiel (idMateriel) ON DELETE CASCADE');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE recommandation DROP FOREIGN KEY FK_C7782A28391C87D5');
        $this->addSql('ALTER TABLE recommandation DROP FOREIGN KEY FK_C7782A284B719ACA');
        $this->addSql('DROP TABLE recommandation');
    }
}
