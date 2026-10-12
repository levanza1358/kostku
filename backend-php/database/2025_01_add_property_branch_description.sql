-- Safe production migration for existing Kostku MySQL 8 / MariaDB databases.
-- Run once while deploying the matching PHP API. No secrets belong in this file.
SET @db := DATABASE();
SET @sql := IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='properties' AND COLUMN_NAME='branch')=0,'ALTER TABLE properties ADD COLUMN branch VARCHAR(200) NOT NULL DEFAULT '' AFTER name','SELECT 1'); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
SET @sql := IF((SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='properties' AND COLUMN_NAME='description')=0,'ALTER TABLE properties ADD COLUMN description TEXT NULL AFTER address','SELECT 1'); PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
