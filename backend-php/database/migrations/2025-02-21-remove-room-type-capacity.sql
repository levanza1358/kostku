-- Room type capacity is intentionally not stored on the type.
-- Occupancy belongs to room_type_prices (for example: 1 person monthly, 2 people weekly).
-- Run once on an existing database after backing it up.
SET @has_capacity := (
  SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'room_types'
    AND COLUMN_NAME = 'capacity'
);
SET @sql := IF(@has_capacity = 1, 'ALTER TABLE room_types DROP COLUMN capacity', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
