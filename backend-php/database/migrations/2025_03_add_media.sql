CREATE TABLE IF NOT EXISTS media (
  id CHAR(36) NOT NULL PRIMARY KEY,
  owner_id CHAR(36) NOT NULL,
  entity_type VARCHAR(20) NOT NULL,
  entity_id CHAR(36) NOT NULL,
  relative_path VARCHAR(500) NOT NULL,
  mime_type VARCHAR(100) NOT NULL,
  bytes INT UNSIGNED NOT NULL,
  width INT UNSIGNED NOT NULL,
  height INT UNSIGNED NOT NULL,
  checksum CHAR(64) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME NULL,
  CONSTRAINT media_owner_fk FOREIGN KEY(owner_id) REFERENCES users(id),
  INDEX media_active_entity (owner_id, entity_type, entity_id, deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
