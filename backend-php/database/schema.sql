-- Kostku complete MySQL 8 / MariaDB schema.
-- Import this file into a fresh or empty `kostku` database to create the current schema.
-- It includes properties, room types, rooms, tenants, payments, expenses, categories, and ownership constraints.
-- For an existing production database, use the numbered migrations instead of relying on
-- CREATE TABLE IF NOT EXISTS to alter already-existing tables.
-- IDs are CHAR(36) UUID strings. The PHP API supplies UUIDs generated with the UUID v4 format;
-- MySQL/MariaDB installations may also generate values with UUID() when inserting directly.

CREATE TABLE IF NOT EXISTS users (
  id CHAR(36) NOT NULL PRIMARY KEY,
  google_subject VARCHAR(255) NOT NULL UNIQUE,
  email VARCHAR(320) NOT NULL,
  display_name VARCHAR(200) NOT NULL,
  photo_url TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS properties (
  id CHAR(36) NOT NULL PRIMARY KEY,
  owner_id CHAR(36) NOT NULL,
  name VARCHAR(200) NOT NULL,
  branch VARCHAR(200) NOT NULL DEFAULT '',
  address TEXT NOT NULL,
  description TEXT,
  notes TEXT,
  token_listrik VARCHAR(60) NULL,
  pdam_number VARCHAR(60) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT properties_owner_fk FOREIGN KEY (owner_id) REFERENCES users(id),
  INDEX properties_owner_created (owner_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS room_types (
  id CHAR(36) NOT NULL PRIMARY KEY,
  property_id CHAR(36) NOT NULL,
  name VARCHAR(200) NOT NULL,
  daily_rate DECIMAL(14,2) NOT NULL DEFAULT 0 CHECK (daily_rate >= 0),
  weekly_rate DECIMAL(14,2) NOT NULL DEFAULT 0 CHECK (weekly_rate >= 0),
  monthly_rate DECIMAL(14,2) NOT NULL DEFAULT 0 CHECK (monthly_rate >= 0),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT room_types_property_fk FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE RESTRICT,
  CONSTRAINT room_types_property_name_unique UNIQUE (property_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS room_type_prices (
  id CHAR(36) NOT NULL PRIMARY KEY,
  room_type_id CHAR(36) NOT NULL,
  occupancy SMALLINT NOT NULL CHECK (occupancy >= 1),
  duration_value SMALLINT NOT NULL CHECK (duration_value >= 1),
  duration_unit VARCHAR(10) NOT NULL CHECK (duration_unit IN ('day','week','month','year')),
  amount DECIMAL(14,2) NOT NULL CHECK (amount >= 0),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT room_type_prices_type_fk FOREIGN KEY (room_type_id) REFERENCES room_types(id) ON DELETE RESTRICT,
  CONSTRAINT room_type_prices_unique UNIQUE (room_type_id, occupancy, duration_value, duration_unit)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS rooms (
  id CHAR(36) NOT NULL PRIMARY KEY,
  property_id CHAR(36) NOT NULL,
  room_type_id CHAR(36) NULL,
  name VARCHAR(100) NOT NULL,
  monthly_rent DECIMAL(14,2) NOT NULL CHECK (monthly_rent >= 0),
  status VARCHAR(20) NOT NULL DEFAULT 'vacant' CHECK (status IN ('vacant','occupied','maintenance','dirty')),
  notes TEXT,
  token_listrik VARCHAR(60) NULL,
  pdam_number VARCHAR(60) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT rooms_property_fk FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE RESTRICT,
  CONSTRAINT rooms_room_type_fk FOREIGN KEY (room_type_id) REFERENCES room_types(id) ON DELETE RESTRICT,
  CONSTRAINT rooms_property_name_unique UNIQUE (property_id, name),
  INDEX rooms_property_type (property_id, room_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS tenants (
  id CHAR(36) NOT NULL PRIMARY KEY,
  owner_id CHAR(36) NOT NULL,
  room_id CHAR(36) NOT NULL,
  full_name VARCHAR(200) NOT NULL,
  phone VARCHAR(50) NOT NULL,
  email VARCHAR(320),
  identity_number VARCHAR(100),
  move_in_date DATE NOT NULL,
  move_out_date DATE,
  move_in_at DATETIME NULL,
  move_out_at DATETIME NULL,
  room_type_price_id CHAR(36) NULL,
  monthly_rent DECIMAL(14,2) NOT NULL CHECK (monthly_rent >= 0),
  due_day SMALLINT NOT NULL CHECK (due_day BETWEEN 1 AND 31),
  status VARCHAR(20) NOT NULL DEFAULT 'active' CHECK (status IN ('active','moved_out','archived')),
  notes TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT tenants_owner_fk FOREIGN KEY (owner_id) REFERENCES users(id),
  CONSTRAINT tenants_room_fk FOREIGN KEY (room_id) REFERENCES rooms(id),
  CONSTRAINT tenants_price_fk FOREIGN KEY (room_type_price_id) REFERENCES room_type_prices(id) ON DELETE RESTRICT,
  INDEX tenants_owner_created (owner_id, created_at),
  INDEX tenants_room_status (room_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS payments (
  id CHAR(36) NOT NULL PRIMARY KEY,
  owner_id CHAR(36) NOT NULL,
  tenant_id CHAR(36) NOT NULL,
  room_id CHAR(36) NOT NULL,
  period_month DATE NOT NULL,
  amount DECIMAL(14,2) NOT NULL CHECK (amount >= 0),
  paid_at DATETIME NOT NULL,
  method VARCHAR(20) NOT NULL CHECK (method IN ('cash','transfer','other')),
  status VARCHAR(20) NOT NULL DEFAULT 'paid' CHECK (status IN ('pending','partial','paid','late','cancelled')),
  category VARCHAR(100),
  reference_number VARCHAR(200),
  notes TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT payments_owner_fk FOREIGN KEY (owner_id) REFERENCES users(id),
  CONSTRAINT payments_tenant_fk FOREIGN KEY (tenant_id) REFERENCES tenants(id),
  CONSTRAINT payments_room_fk FOREIGN KEY (room_id) REFERENCES rooms(id),
  INDEX payments_owner_paid (owner_id, paid_at),
  INDEX payments_tenant_status (tenant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS media (
  id CHAR(36) NOT NULL PRIMARY KEY,
  owner_id CHAR(36) NOT NULL,
  entity_type VARCHAR(20) NOT NULL CHECK (entity_type IN ('tenant','room','payment','expense','property')),
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
  CONSTRAINT media_owner_fk FOREIGN KEY (owner_id) REFERENCES users(id),
  INDEX media_active_entity (owner_id, entity_type, entity_id, deleted_at),
  INDEX media_entity_lookup (entity_type, entity_id, deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Pengeluaran kos: biaya operasional umum MAUPUN pengembalian/refund ke penghuni.
-- kind: 'operational' (listrik, air, perbaikan, gaji, dll.) atau 'refund' (kembali ke penghuni).
-- category bebas diisi untuk operational; tenant_id wajib untuk refund.
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

-- Riwayat perubahan data penghuni (audit log). Ditulis oleh API pada setiap mutasi tenant.
-- details menyimpan JSON ringkas (mis. daftar field yang berubah) sebagai TEXT.
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

-- Kategori pemasukan/pengeluaran yang dapat diatur sendiri oleh pemilik (global per akun).
-- kind: 'income' (untuk pembayaran) atau 'expense' (untuk pengeluaran).
-- Catatan (payments.category / expenses.category) menyimpan NAMA kategori sebagai teks,
-- sehingga mengganti/menghapus kategori tidak mengubah catatan yang sudah ada.
CREATE TABLE IF NOT EXISTS categories (
  id CHAR(36) NOT NULL PRIMARY KEY,
  owner_id CHAR(36) NOT NULL,
  kind VARCHAR(10) NOT NULL CHECK (kind IN ('income','expense')),
  name VARCHAR(100) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT categories_owner_fk FOREIGN KEY (owner_id) REFERENCES users(id),
  CONSTRAINT categories_owner_kind_name_unique UNIQUE (owner_id, kind, name),
  INDEX categories_owner_kind (owner_id, kind, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- Riwayat penghuni keluar. Snapshot yang dicatat saat penghuni ditandai keluar
-- (move_out_date diisi), sehingga riwayat tetap ada walau penghuni diarsipkan/diubah.
CREATE TABLE IF NOT EXISTS tenant_move_out_history (
  id CHAR(36) NOT NULL PRIMARY KEY,
  owner_id CHAR(36) NOT NULL,
  tenant_id CHAR(36) NOT NULL,
  full_name VARCHAR(200) NOT NULL,
  phone VARCHAR(50) NOT NULL DEFAULT '',
  room_id CHAR(36) NULL,
  room_name VARCHAR(200) NULL,
  move_in_date DATE NULL,
  move_out_date DATE NOT NULL,
  monthly_rent DECIMAL(14,2) NOT NULL DEFAULT 0,
  notes TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT tmoh_owner_fk FOREIGN KEY (owner_id) REFERENCES users(id),
  CONSTRAINT tmoh_tenant_fk FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
  INDEX tmoh_owner_date (owner_id, move_out_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- Status kamar: vacant (Tersedia), occupied (Terisi), maintenance (Perbaikan), dirty (Kotor).
-- Status pembayaran: pending (Menunggu), partial (Sebagian), paid (Lunas), late (Terlambat), cancelled (Dibatalkan).
-- Tabel media hanya menyimpan metadata; file foto disimpan di filesystem VPS.
-- Struktur file: media/{owner_id}/{penghuni|kamar|pembayaran|pengeluaran|lokasi}/{YYYY}/{MM}/{nama-acak}.{ext}
-- Kolom photo_url pada respons API dibentuk dari MEDIA_PUBLIC_BASE_URL + media.relative_path
-- dan bernilai NULL bila tidak ada media aktif (deleted_at IS NULL).
-- Catatan impor: schema.sql adalah satu-satunya sumber skema untuk saat ini.
-- Seluruh tabel memakai CREATE TABLE IF NOT EXISTS sehingga aman diimpor berulang kali
-- ke database kosong. Jika suatu saat database production sudah berisi data dan perlu
-- perubahan struktur, baru buat folder database/migrations/ dengan file migrasi bernomor.
