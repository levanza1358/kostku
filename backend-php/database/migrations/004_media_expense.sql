-- Migrasi 004: izinkan entity_type 'expense' pada tabel `media`, agar pengeluaran
-- dapat memiliki bukti foto (nota/kuitansi).
-- Idempoten: aman dijalankan berulang kali.
--
-- Catatan MariaDB: CHECK inline pada kolom tidak dapat di-DROP CONSTRAINT, jadi kolom
-- diubah memakai MODIFY COLUMN dengan CHECK baru.

ALTER TABLE media DROP CONSTRAINT IF EXISTS `entity_type`;
ALTER TABLE media MODIFY COLUMN entity_type VARCHAR(20) NOT NULL
  CHECK (entity_type IN ('tenant','room','payment','expense'));
