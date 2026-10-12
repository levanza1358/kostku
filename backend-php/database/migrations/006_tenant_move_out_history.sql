-- Migrasi 006: tabel `tenant_move_out_history` — riwayat penghuni keluar.
-- Setiap penghuni yang ditandai keluar dicatat di sini sebagai snapshot (nama, HP,
-- kamar, tanggal masuk/keluar, sewa) sehingga riwayat tetap ada walau penghuni
-- kemudian diarsipkan atau datanya berubah.
-- Idempoten: aman dijalankan berulang kali.

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

-- Backfill: isi riwayat dari penghuni yang sudah berstatus 'moved_out' sebelum migrasi ini,
-- supaya daftar tidak kosong. Idempoten karena diperiksa NOT EXISTS.

INSERT INTO tenant_move_out_history
  (id, owner_id, tenant_id, full_name, phone, room_id, room_name, move_in_date, move_out_date, monthly_rent, created_at)
SELECT UUID(), t.owner_id, t.id, t.full_name, t.phone, t.room_id, r.name, t.move_in_date,
       COALESCE(t.move_out_date, DATE(t.updated_at)), t.monthly_rent, t.updated_at
FROM tenants t
LEFT JOIN rooms r ON r.id = t.room_id
WHERE t.status = 'moved_out'
  AND NOT EXISTS (
    SELECT 1 FROM tenant_move_out_history h
    WHERE h.tenant_id = t.id AND h.owner_id = t.owner_id
  );
