-- Migration: Assignation admin (probleme et diagnostique)
-- Run in schema 3a19

ALTER TABLE probleme ADD COLUMN id_admin_assignee INT NULL;
ALTER TABLE diagnostique ADD COLUMN id_admin_diagnostiqueur INT NULL;
ALTER TABLE probleme ADD CONSTRAINT fk_probleme_admin_assignee FOREIGN KEY (id_admin_assignee) REFERENCES utilisateur(id) ON DELETE SET NULL;
ALTER TABLE diagnostique ADD CONSTRAINT fk_diagnostique_admin FOREIGN KEY (id_admin_diagnostiqueur) REFERENCES utilisateur(id) ON DELETE SET NULL;
