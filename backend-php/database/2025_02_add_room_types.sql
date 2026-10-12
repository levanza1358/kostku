-- Production migration: run once after backup, using the Kostku database.
CREATE TABLE IF NOT EXISTS room_types (
  id CHAR(36) NOT NULL PRIMARY KEY,
  property_id CHAR(36) NOT NULL,
  name VARCHAR(200) NOT NULL,
  capacity SMALLINT NOT NULL DEFAULT 1 CHECK (capacity >= 1),
  daily_rate DECIMAL(14,2) NOT NULL DEFAULT 0 CHECK (daily_rate >= 0),
  weekly_rate DECIMAL(14,2) NOT NULL DEFAULT 0 CHECK (weekly_rate >= 0),
  monthly_rate DECIMAL(14,2) NOT NULL DEFAULT 0 CHECK (monthly_rate >= 0),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT room_types_property_fk FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
SET @has_room_type_id := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='rooms' AND COLUMN_NAME='room_type_id');
SET @sql := IF(@has_room_type_id=0, 'ALTER TABLE rooms ADD COLUMN room_type_id CHAR(36) NULL', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @has_fk := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS WHERE CONSTRAINT_SCHEMA=DATABASE() AND TABLE_NAME='rooms' AND CONSTRAINT_NAME='rooms_room_type_fk');
SET @sql := IF(@has_fk=0, 'ALTER TABLE rooms ADD CONSTRAINT rooms_room_type_fk FOREIGN KEY (room_type_id) REFERENCES room_types(id) ON DELETE RESTRICT', 'SELECT 1'); PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
