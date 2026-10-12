-- PRODUCTION MIGRATION: back up the database and verify the backup before running.
-- MySQL 8 / MariaDB. Run once during a maintenance window; do not skip the data update.
SET @db := DATABASE();

SET @has_dv := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='room_type_prices' AND COLUMN_NAME='duration_value');
SET @sql := IF(@has_dv=0, 'ALTER TABLE room_type_prices ADD COLUMN duration_value SMALLINT NULL AFTER occupancy', 'SELECT 1'); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @has_du := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='room_type_prices' AND COLUMN_NAME='duration_unit');
SET @sql := IF(@has_du=0, 'ALTER TABLE room_type_prices ADD COLUMN duration_unit VARCHAR(10) NULL AFTER duration_value', 'SELECT 1'); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

UPDATE room_type_prices SET duration_value=1, duration_unit=CASE billing_period WHEN 'daily' THEN 'day' WHEN 'weekly' THEN 'week' WHEN 'monthly' THEN 'month' ELSE 'month' END WHERE duration_value IS NULL OR duration_unit IS NULL;

ALTER TABLE room_type_prices DROP INDEX room_type_prices_unique;
ALTER TABLE room_type_prices MODIFY duration_value SMALLINT NOT NULL, MODIFY duration_unit VARCHAR(10) NOT NULL;
ALTER TABLE room_type_prices ADD CONSTRAINT room_type_prices_unique UNIQUE (room_type_id, occupancy, duration_value, duration_unit);

SET @has_bp := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='room_type_prices' AND COLUMN_NAME='billing_period');
SET @sql := IF(@has_bp=1, 'ALTER TABLE room_type_prices DROP COLUMN billing_period', 'SELECT 1'); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
