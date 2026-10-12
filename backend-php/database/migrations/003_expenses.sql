-- Migrasi 003: tabel `expenses` untuk pengeluaran kos.
-- Mendukung dua jenis: 'operational' (listrik, air, perbaikan, gaji, dll.) dan
-- 'refund' (pengembalian/refund ke penghuni, tenant_id terisi).
-- Idempoten: aman dijalankan berulang kali.

CREATE TABLE IF NOT EXISTS expenses (
  id CHAR(36) NOT NULL PRIMARY KEY,
  owner_id CHAR(36) NOT NULL,
  kind VARCHAR(20) NOT NULL DEFAULT 'operational' CHECK (kind IN ('operational','refund')),
  category VARCHAR(50),
  tenant_id CHAR(36) NULL,
  property_id CHAR(36) NULL,
  amount DECIMAL(14,2) NOT NULL CHECK (amount >= 0),
  spent_at DATETIME NOT NULL,
  method VARCHAR(20) NOT NULL DEFAULT 'cash' CHECK (method IN ('cash','transfer','other')),
  reference_number VARCHAR(200),
  notes TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT expenses_owner_fk FOREIGN KEY (owner_id) REFERENCES users(id),
  CONSTRAINT expenses_tenant_fk FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE SET NULL,
  CONSTRAINT expenses_property_fk FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE SET NULL,
  INDEX expenses_owner_spent (owner_id, spent_at),
  INDEX expenses_owner_kind (owner_id, kind)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
