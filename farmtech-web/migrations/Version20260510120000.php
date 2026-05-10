<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Blog / articles / commentaires — DDL matches Doctrine metadata (FK + index names, no orphan relations).
 *
 * Note: Article stores BlogID as a scalar only (no ORM association), so no article→blog foreign key.
 */
final class Version20260510120000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Create blog, article, and commentaire tables for the web blog module.';
    }

    public function up(Schema $schema): void
    {
        $this->addSql('CREATE TABLE IF NOT EXISTS blog (
            idBlog INT AUTO_INCREMENT NOT NULL,
            TitleBlog VARCHAR(255) NOT NULL,
            BlogTag VARCHAR(255) NOT NULL,
            idutilisateur INT NOT NULL,
            DateBlog DATETIME NOT NULL,
            PRIMARY KEY(idBlog),
            INDEX IDX_C0155143DBDD131C (idutilisateur),
            CONSTRAINT FK_C0155143DBDD131C FOREIGN KEY (idutilisateur) REFERENCES utilisateur (id)
        ) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');

        $this->addSql('CREATE TABLE IF NOT EXISTS article (
            ArticleID INT AUTO_INCREMENT NOT NULL,
            Titre VARCHAR(255) NOT NULL,
            texte LONGTEXT NOT NULL,
            Likes INT DEFAULT NULL,
            Dislikes INT DEFAULT NULL,
            edited TINYINT NOT NULL,
            BlogID INT NOT NULL,
            CreationDate DATETIME NOT NULL,
            PRIMARY KEY(ArticleID)
        ) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');

        $this->addSql('CREATE TABLE IF NOT EXISTS commentaire (
            idComment INT AUTO_INCREMENT NOT NULL,
            texte LONGTEXT NOT NULL,
            idArticle INT NOT NULL,
            idUser INT NOT NULL,
            datecomment DATETIME NOT NULL,
            PRIMARY KEY(idComment),
            INDEX IDX_67F068BC12836594 (idArticle),
            INDEX IDX_67F068BCFE6E88D7 (idUser),
            CONSTRAINT FK_67F068BC12836594 FOREIGN KEY (idArticle) REFERENCES article (ArticleID),
            CONSTRAINT FK_67F068BCFE6E88D7 FOREIGN KEY (idUser) REFERENCES utilisateur (id)
        ) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
    }

    public function down(Schema $schema): void
    {
        $this->addSql('DROP TABLE IF EXISTS commentaire');
        $this->addSql('DROP TABLE IF EXISTS article');
        $this->addSql('DROP TABLE IF EXISTS blog');
    }
}
