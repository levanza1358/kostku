# Rencana Implementasi — Kostku

**Status:** Disetujui (mock `mock/kostku-mock.html`). Implementasi bertahap, dokumentasi lebih dulu.
**Aturan:** setiap perubahan DB **hanya** lewat `backend-php/database/migrations/` bernomor; `schema.sql` selalu ikut diperbarui. Deploy produksi dilakukan manual dan terpisah (butuh izin). Jangan menyentuh data/properti milik akun di luar kebutuhan.
**Deploy produksi:** ✅ **selesai 2026-10-11** — migrasi 007 diterapkan, file backend diperbarui (md5 cocok), uji end-to-end 20/20 PASS.

## Ringkasan fitur yang diimplementasikan

| # | Fitur | Status | Catatan |
|---|-------|--------|---------|
| F1 | Detail lokasi **Pengaturan** terpisah | ✅ | Tab Dashboard/Kamar/Pembayaran + layar `LocationSettingsScreen` |
| F2 | Daftar **Kamar** hanya **Detail + Edit** | ✅ | `RoomListTab` + `RoomDetailScreen` |
| F3 | **Chip sisa hari** di daftar penghuni | ✅ | Hijau ≥14, kuning 7–13, merah <7/lewat, abu tanpa tanggal |
| F4 | **Logo lokasi** + tampil di kwitansi | ✅ | `media` entity `property`, fallback ikon rumah |
| F5 | **Token listrik & PDAM** opsional (lokasi + kamar) | ✅ | Detail penghuni menampilkan "Tidak tersedia" |
| F6 | **Kwitansi** (dari data pembayaran) | ✅ | `ReceiptScreen` + Bagikan/PDF native |
| F7 | Bottom nav hanya di tab utama | ✅ | Terverifikasi (sudah benar) |
| F8 | Kerapian UI sesuai mock | ✅ | `AppTextField`/`SaveButton`/komponen bersama |

## Kontrak data baru

- `properties.token_listrik` VARCHAR(60) NULL, `properties.pdam_number` VARCHAR(60) NULL.
- `rooms.token_listrik` VARCHAR(60) NULL, `rooms.pdam_number` VARCHAR(60) NULL.
- Utilitas **disimpan sebagai string** (bukan angka) agar nol di depan aman.
- **Tidak ada pewarisan** dari lokasi ke kamar. Detail penghuni menampilkan utilitas **kamar** dan **lokasi** terpisah; yang kosong ditulis **"Tidak tersedia"**.
- Logo lokasi = media `entity_type='property'` (satu foto aktif, aturan media yang sama).

---

## Fase 0 — Dokumentasi (dulu) ✅
- [x] `docs/IMPLEMENTATION-PLAN.md` (dokumen ini)
- [x] `docs/UX-UI-SPEC.md` — detail lokasi, chip sisa hari, logo, kwitansi, utilitas
- [x] `docs/API-SPEC.md` — field utilitas, `POST /media/property/{id}`, kwitansi turunan
- [x] `docs/DATABASE.md` — migrasi 007 + entitas media `property`
- [x] `docs/RULES.md` — aturan 30–32
- [x] `docs/CHANGELOG.md` — entri Unreleased
- [x] `docs/TESTING.md` — checklist QA
- [x] `docs/ROADMAP.md` — tautkan dokumen ini

## Fase 1 — Backend (`backend-php/`) ✅
- [x] `database/migrations/007_utilities_logo.sql`: 4 kolom baru + perluas CHECK `media.entity_type`.
- [x] `database/schema.sql`: sinkronkan (baseline untuk DB baru).
- [x] `config/bootstrap.php`: `mediaEntity`/`mediaUpload` menerima `entity_type='property'` (kategori folder `lokasi`); `insertSnapshot` menyertakan `logo_url`; helper `utilityValue`/`utilitiesFromBody`.
- [x] `public/index.php`:
  - [x] `POST /properties` & `PATCH /properties/{id}`: terima `token_listrik`, `pdam_number`.
  - [x] `POST /properties/{id}/rooms`, `PATCH /rooms/{id}`: terima `token_listrik`, `pdam_number` (batch = default NULL, diisi lewat edit).
  - [x] `POST/DELETE /media/property/{id}` (regex media diperluas).
  - [x] `DELETE /properties/{id}` ikut mem-purge media logo.
- [x] Snapshot: kolom baru otomatis ikut karena `properties`/`rooms` pakai `SELECT *`.

**Verifikasi:** `php -l` tidak tersedia lokal (tanpa PHP); ditinjau manual + lint editor. Impor migrasi & uji end-to-end menunggu deploy (manual, butuh izin).

## Fase 2 — Data layer Android ✅
- [x] `NetworkApi.kt`: `RemoteLocation` + `tokenListrik`,`pdamNumber`,`logoUrl`; `RemoteRoom` + `tokenListrik`,`pdamNumber`.
- [x] Parsing JSON `logo_url` (properties) & utilitas lewat helper `locationFrom`/`roomFrom`.
- [x] `uploadPhoto("property", id, file)` + reload; `updateProperty`/`updateRoom` menerima utilitas.

## Fase 3 — Detail lokasi sesuai mock ✅
- [x] `LocationDetailScreen`: tab **Dashboard / Kamar / Pembayaran** dipoles (okupansi, keuangan, jatuh tempo, perlu tindakan).
- [x] Daftar **Kamar**: tiap kartu **Detail** + **Edit** saja (tanpa tambah/hapus) + pencarian.
- [x] Layar **Detail kamar**: identitas + penghuni + utilitas + CTA Edit.
- [x] **Pengaturan** lokasi: Edit info + logo, **Tipe kamar**, **Nomor kamar**, utilitas, hapus lokasi.
- [x] **Edit nomor kamar**: nomor + tipe + status + token listrik/PDAM (opsional).
- [x] **Nomor kamar** (daftar) + tombol tambah.

## Fase 4 — Chip sisa hari + utilitas penghuni ✅
- [x] Daftar penghuni: chip = sisa hari berwarna.
- [x] Detail penghuni: bagian **Utilitas kamar** (token/PDAM), kosong → **"Tidak tersedia"**. (Utilitas lokasi tidak ditampilkan di sini — cukup di **Pengaturan** lokasi.)

## Fase 5 — Kwitansi ✅
- [x] Layar `ReceiptScreen` dari data `RemotePayment` (tanpa endpoint baru): header logo lokasi (fallback ikon rumah), identitas kos, penghuni/kamar/periode/metode, total.
- [x] Cap status: **LUNAS** hanya bila `status == paid`; selain itu **BELUM LUNAS**/**SEBAGIAN**.
- [x] **Bagikan** (teks) + **PDF** (native `PdfDocument` + `FileProvider` + `Intent`), tanpa pustaka baru.
- [x] Buka kwitansi dari daftar pembayaran (ledger "Semua" & tab "Pemasukan" via menu ⋮).

## Fase 6 — Verifikasi & rilis
- [x] Gate: `JAVA_HOME="/c/Program Files/Android/Android Studio/jbr" ./gradlew clean testDebugUnitTest assembleDebug` → **BUILD SUCCESSFUL**.
- [x] Unit test baru (sisa hari, label cap kwitansi, teks utilitas) — `UiFormatTest`.
- [x] Cek layar di emulator (Pixel_6a, ADB + `uiautomator dump`): daftar penghuni (chip sisa hari), detail lokasi (Dashboard/Kamar/Pembayaran), Kamar → Detail kamar (utilitas "Tidak tersedia"), Pengaturan → Nomor kamar, Edit lokasi (logo + utilitas), kwitansi (LUNAS, Bagikan, PDF → share sheet `kwitansi_<id>.pdf`). Bottom nav hanya di tab utama.
- [x] Salin APK ke `C:\Users\Miraya\Downloads\kostku-debug.apk`.
- [x] Finalisasi `CHANGELOG.md` + centang checklist.
- [x] Deploy backend produksi (migrasi 007 + bootstrap/index/schema) lalu uji end-to-end → **SELESAI 2026-10-11**.
  - Migrasi `007_utilities_logo.sql` diterapkan ke DB produksi (MariaDB 11.4): 4 kolom utilitas + CHECK `media.entity_type` menerima `property`.
  - `config/bootstrap.php`, `public/index.php`, `database/schema.sql`, `database/migrations/007_utilities_logo.sql` diunggah; **md5 server == lokal**.
  - Uji end-to-end 20/20 PASS (via `127.0.0.1`): utilitas lokasi & kamar tersimpan, `POST /media/property` + file terlayani, `/snapshot` memuat `token_listrik`/`pdam_number`/`logo_url`; properti **wasd** & **Miraya Kos** tidak berubah; seluruh data uji dibersihkan.
  - Rute baru `/v1/media/property/{id}` aktif di edge publik (401 auth, bukan 404). Backup server pra-deploy: `/tmp/kostku_backend_20261011_210455.tgz`.

**Perbaikan bug saat verifikasi:** urutan cabang navigasi di `App.kt` diperbaiki — dulu `locationSettings` mendahului `roomNumberLocation`/`locationEdit` sehingga membuka **Nomor kamar**/**Edit info** dari **Pengaturan** memantul balik ke Pengaturan. Kini cabang layar dalam (nomor kamar & edit lokasi) diperiksa sebelum cabang Pengaturan.

## Kriteria terima keseluruhan
1. Semua layar mock baru ada di app dan berjalan tanpa crash.
2. Utilitas & logo opsional: kosong tidak memecahkan tampilan; kwitansi tetap rapi.
3. Status "LUNAS" tidak pernah salah untuk pembayaran belum lunas.
4. Bottom nav hanya di 4 tab utama.
5. Build & unit test hijau; tanpa perubahan perilaku lain.
