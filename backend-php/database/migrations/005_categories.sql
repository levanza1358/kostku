-- Migrasi 005: kategori pemasukan/pengeluaran yang bisa diatur sendiri oleh pemilik.
-- - Tabel `categories` (global per akun; kind 'income' | 'expense').
-- - Kolom `payments.category` (kategori untuk pemasukan/pembayaran).
-- Idempoten: aman dijalankan berulang kali.

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

ALTER TABLE payments ADD COLUMN IF NOT EXISTS category VARCHAR(100) NULL AFTER status;
