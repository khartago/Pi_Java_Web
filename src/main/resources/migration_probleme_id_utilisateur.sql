-- Lie le problème au fermier (utilisateur)
ALTER TABLE probleme
    ADD COLUMN id_utilisateur INT NULL,
    ADD CONSTRAINT fk_probleme_utilisateur
        FOREIGN KEY (id_utilisateur)
        REFERENCES utilisateur(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE;
