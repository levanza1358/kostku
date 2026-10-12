-- Migrasi 001: tabel audit log penghuni (tenant_audit_log).
-- Idempoten: aman dijalankan berulang kali pada database produksi yang sudah berisi data.
-- Jalankan setelah mengimpor schema.sql, atau pada database yang belum punya tabel ini.
-- Catatan: database/schema.sql sudah memuat tabel ini untuk instalasi baru; migrasi ini
-- disediakan untuk database yang sudah ada (lihat docs/DATABASE.md).

CREATE TABLE IF NOT EXISTS tenant_audit_log (
  id CHAR(36) NOT NULL PRIMARY KEY,
  owner_id CHAR(36) NOT NULL,
  tenant_id CHAR(36) NOT NULL,
  action VARCHAR(40) NOT NULL,
  details TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT tenant_audit_owner_fk FOREIGN KEY (owner_id) REFERENCES users(id),
  CONSTRAINT tenant_audit_tenant_fk FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
  INDEX tenant_audit_lookup (tenant_id, created_at),
  INDEX tenant_audit_owner (owner_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
