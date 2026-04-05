USE 3a19;

DROP TABLE IF EXISTS `articletable`;
CREATE TABLE IF NOT EXISTS `articletable` (
                                              `ArticleID` int NOT NULL,
                                              `Titre` tinytext COLLATE utf8mb4_unicode_ci NOT NULL,
                                              `Texte` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                                              `CreationDate` datetime NOT NULL,
                                              `Likes` int NOT NULL,
                                              `Dislikes` int NOT NULL,
                                              `Edited` tinyint(1) NOT NULL,
    `BlogID` int NOT NULL,
    PRIMARY KEY (`ArticleID`)
    ) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
COMMIT;