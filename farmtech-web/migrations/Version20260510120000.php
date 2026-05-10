<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Blog / articles / commentaires (aligned with App\Entity\Blog, Article, Commentaire).
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
            DateBlog DATETIME NOT NULL COMMENT \'(DC2Type:datetime_immutable)\',
            PRIMARY KEY(idBlog),
            INDEX IDX_BLOG_UTILISATEUR (idutilisateur),
            CONSTRAINT FK_BLOG_UTILISATEUR FOREIGN KEY (idutilisateur) REFERENCES utilisateur (id) ON DELETE CASCADE
        ) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');

        $this->addSql('CREATE TABLE IF NOT EXISTS article (
            ArticleID INT AUTO_INCREMENT NOT NULL,
            Titre VARCHAR(255) NOT NULL,
            texte LONGTEXT NOT NULL,
            Likes INT DEFAULT NULL,
            Dislikes INT DEFAULT NULL,
            edited TINYINT(1) NOT NULL DEFAULT 0,
            BlogID INT NOT NULL,
            CreationDate DATETIME NOT NULL COMMENT \'(DC2Type:datetime_immutable)\',
            PRIMARY KEY(ArticleID),
            INDEX IDX_ARTICLE_BLOG (BlogID),
            CONSTRAINT FK_ARTICLE_BLOG FOREIGN KEY (BlogID) REFERENCES blog (idBlog) ON DELETE CASCADE
        ) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');

        $this->addSql('CREATE TABLE IF NOT EXISTS commentaire (
            idComment INT AUTO_INCREMENT NOT NULL,
            texte LONGTEXT NOT NULL,
            idArticle INT NOT NULL,
            idUser INT NOT NULL,
            datecomment DATETIME NOT NULL COMMENT \'(DC2Type:datetime_immutable)\',
            PRIMARY KEY(idComment),
            INDEX IDX_COMMENTAIRE_ARTICLE (idArticle),
            INDEX IDX_COMMENTAIRE_USER (idUser),
            CONSTRAINT FK_COMMENTAIRE_ARTICLE FOREIGN KEY (idArticle) REFERENCES article (ArticleID) ON DELETE CASCADE,
            CONSTRAINT FK_COMMENTAIRE_USER FOREIGN KEY (idUser) REFERENCES utilisateur (id) ON DELETE CASCADE
        ) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
    }

    public function down(Schema $schema): void
    {
        $this->addSql('DROP TABLE IF EXISTS commentaire');
        $this->addSql('DROP TABLE IF EXISTS article');
        $this->addSql('DROP TABLE IF EXISTS blog');
    }
}
