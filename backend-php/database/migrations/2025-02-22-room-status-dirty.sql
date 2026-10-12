-- Backup the database before running. MySQL 8/MariaDB safe migration.
-- Run once by an operator; the guarded INFORMATION_SCHEMA check avoids repeating it.
SET @db := DATABASE();
SET @constraint := (SELECT CONSTRAINT_NAME FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA=@db AND TABLE_NAME='rooms' AND CONSTRAINT_TYPE='CHECK' LIMIT 1);
SET @sql := IF(@constraint IS NULL, 'SELECT 1', CONCAT('ALTER TABLE rooms DROP CONSTRAINT `', @constraint, '`'));
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
ALTER TABLE rooms ADD CONSTRAINT rooms_status_check CHECK (status IN ('vacant','occupied','maintenance','dirty'));
-- Existing rows are unchanged. dirty is displayed as Kotor; occupied is assigned by tenant assignment.
