USE 3a19;

DROP TABLE IF EXISTS `blogtable`;
CREATE TABLE IF NOT EXISTS `blogtable` (
                                           `idBlog` int NOT NULL,
                                           `TitleBlog` tinytext COLLATE utf8mb4_unicode_ci NOT NULL,
                                           `idutilisateur` int NOT NULL,
                                           `DateBlog` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           PRIMARY KEY (`idBlog`),
    UNIQUE KEY `idutilisateur` (`idutilisateur`) USING BTREE
    ) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
COMMIT;