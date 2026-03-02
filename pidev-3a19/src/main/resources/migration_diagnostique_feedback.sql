-- Migration: Farmer feedback on diagnostic (résolu / non résolu)
-- Run in schema 3a19 (skip if columns already exist)

ALTER TABLE diagnostique ADD COLUMN feedback_fermier VARCHAR(20) NULL;
ALTER TABLE diagnostique ADD COLUMN feedback_commentaire TEXT NULL;
ALTER TABLE diagnostique ADD COLUMN date_feedback DATETIME NULL;
