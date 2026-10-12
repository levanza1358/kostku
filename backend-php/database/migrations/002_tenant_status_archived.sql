-- Migrasi 002: izinkan status penghuni 'archived' (soft-delete/arsip penghuni).
-- DELETE /tenants/{id} kini mengarsipkan (soft-delete) penghuni, bukan menghapus baris,
-- sehingga riwayat pembayaran dan audit log tetap utuh dan tidak ada error FK 500.
-- Idempoten: aman dijalankan berulang kali.
--
-- Catatan MariaDB: CHECK yang dibuat inline pada kolom tidak dapat di-DROP CONSTRAINT,
-- jadi kolom diubah memakai MODIFY COLUMN dengan CHECK baru. Constraint bernama duplikat
-- (bila ada) dibuang lebih dulu.

ALTER TABLE tenants DROP CONSTRAINT IF EXISTS `status`;
ALTER TABLE tenants MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'active'
  CHECK (status IN ('active','moved_out','archived'));
