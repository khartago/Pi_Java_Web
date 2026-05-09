-- Migration: Révisions de diagnostic (réouverture)
-- Run in schema 3a19

ALTER TABLE diagnostique ADD COLUMN num_revision INT NOT NULL DEFAULT 1;
CREATE INDEX idx_diag_probleme_revision ON diagnostique(id_probleme, num_revision);
