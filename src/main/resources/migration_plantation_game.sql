-- Migration: add game columns to plantation table (for front-office game)
-- Run this once if plantation exists without game columns.
-- Ignore "Duplicate column" errors if already applied.

ALTER TABLE plantation ADD COLUMN stage INT DEFAULT 1;
ALTER TABLE plantation ADD COLUMN water_count INT DEFAULT 0;
ALTER TABLE plantation ADD COLUMN last_water_time BIGINT DEFAULT 0;
ALTER TABLE plantation ADD COLUMN status VARCHAR(50) DEFAULT 'ALIVE';
ALTER TABLE plantation ADD COLUMN growth_speed DOUBLE DEFAULT 1.0;
ALTER TABLE plantation ADD COLUMN slot_index INT DEFAULT 0;
