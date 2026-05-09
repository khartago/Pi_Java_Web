-- Migration : ajout du champ medicament à la table diagnostique (exécuter une fois si la table existe déjà)
ALTER TABLE diagnostique ADD COLUMN medicament TEXT NULL;
