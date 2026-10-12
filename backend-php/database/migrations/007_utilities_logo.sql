-- Migrasi 007: utilitas kamar/lokasi (nomor token listrik & nomor PDAM) dan
-- dukungan logo lokasi kos melalui tabel `media`.
-- Idempoten: aman dijalankan berulang kali pada database produksi yang sudah berisi data.
--
-- Catatan:
-- - Kolom utilitas bersifat OPSIONAL (NULL bila belum diisi) dan disimpan sebagai
--   VARCHAR (bukan angka) supaya nomor dengan nol di depan / karakter pemisah tetap utuh.
-- - Tidak ada pewarisan nilai antar tingkat: utilitas kamar dan utilitas lokasi berdiri
--   sendiri-sendiri (lihat docs/RULES.md butir 30).
-- - Logo lokasi memakai tabel `media` dengan entity_type baru 'property'
--   (kategori folder 'lokasi'), sehingga pola upload/purge media yang ada dapat dipakai ulang.

ALTER TABLE properties ADD COLUMN IF NOT EXISTS token_listrik VARCHAR(60) NULL AFTER notes;
ALTER TABLE properties ADD COLUMN IF NOT EXISTS pdam_number VARCHAR(60) NULL AFTER token_listrik;

ALTER TABLE rooms ADD COLUMN IF NOT EXISTS token_listrik VARCHAR(60) NULL AFTER notes;
ALTER TABLE rooms ADD COLUMN IF NOT EXISTS pdam_number VARCHAR(60) NULL AFTER token_listrik;

-- Perluas CHECK entity_type pada tabel `media` agar menerima 'property' (logo lokasi).
-- Catatan MariaDB: CHECK inline pada kolom tidak dapat di-DROP CONSTRAINT, jadi kolom
-- diubah memakai MODIFY COLUMN dengan CHECK baru (pola sama seperti migrasi 004).
ALTER TABLE media DROP CONSTRAINT IF EXISTS `entity_type`;
ALTER TABLE media MODIFY COLUMN entity_type VARCHAR(20) NOT NULL
  CHECK (entity_type IN ('tenant','room','payment','expense','property'));
