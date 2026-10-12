-- Add precise tenant placement timestamps. Run after backup; safe for existing MySQL/MariaDB databases.
SET @db := DATABASE();
SET @sql := (SELECT IF(COUNT(*)=0, 'ALTER TABLE tenants ADD COLUMN move_in_at DATETIME NULL AFTER move_in_date, ADD COLUMN move_out_at DATETIME NULL AFTER move_out_date', 'SELECT 1') FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='tenants' AND COLUMN_NAME='move_in_at');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @sql := (SELECT IF(COUNT(*)=0, 'ALTER TABLE tenants ADD COLUMN room_type_price_id CHAR(36) NULL AFTER move_out_at', 'SELECT 1') FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='tenants' AND COLUMN_NAME='room_type_price_id');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
ALTER TABLE tenants MODIFY phone VARCHAR(50) NOT NULL DEFAULT '';
SET @sql := (SELECT IF(COUNT(*)=0, 'CREATE INDEX tenants_move_in_at_idx ON tenants (move_in_at)', 'SELECT 1') FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='tenants' AND INDEX_NAME='tenants_move_in_at_idx');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
