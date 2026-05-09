-- Migration: Snapshot meteo au moment de la creation du probleme
-- Run in schema 3a19

ALTER TABLE probleme ADD COLUMN meteo_snapshot TEXT NULL;
