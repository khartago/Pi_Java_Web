USE 3a19;

DROP TABLE IF EXISTS `commettable`;
CREATE TABLE IF NOT EXISTS `commettable` (
                                             `idComment` int NOT NULL,
                                             `idArticle` int NOT NULL,
                                             `DateComment` timestamp NOT NULL,
                                             `idUser` int NOT NULL,
                                             `texte` mediumtext COLLATE utf8mb4_unicode_ci NOT NULL,
                                             PRIMARY KEY (`idComment`),
    KEY `ArticleLink` (`idArticle`)
    ) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
COMMIT;