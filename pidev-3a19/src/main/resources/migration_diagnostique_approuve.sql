-- Migration : ajout du champ approuve (diagnostic visible au fermier uniquement après acceptation admin)
ALTER TABLE diagnostique ADD COLUMN approuve TINYINT(1) NOT NULL DEFAULT 0;
