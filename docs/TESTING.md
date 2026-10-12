# Panduan Pengujian & Menjalankan — Kostku

Dokumen ini menjelaskan cara membuild, menjalankan, dan menguji aplikasi Android Kostku
serta backend PHP-nya.

## Prasyarat

- **Android Studio** (JDK bawaan `jbr-25` sudah cukup; proyek memakai Java 11 target bytecode).
- **Android SDK** dengan platform untuk `compileSdk 37` dan `minSdk 34`.
- Perangkat/emulator untuk menjalankan aplikasi (uji instrumen & foto memerlukan perangkat).
- **Backend** (opsional, untuk uji end-to-end): PHP 8.2+, MySQL 8/MariaDB, `pdo_mysql`.

## 1. Build & jalankan aplikasi

Dari akar proyek (Windows: `gradlew.bat`, Unix/macOS: `./gradlew`):

```bash
# Build debug APK
./gradlew assembleDebug

# Build + pasang ke perangkat/emulator yang terhubung
./gradlew installDebug

# Jalankan langsung dari Android Studio: Run ▶ (konfigurasi "app")
```

APK debug hasil build ada di `app/build/outputs/apk/debug/app-debug.apk`.

Pasang manual lewat adb:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.miraya.kostku/.MainActivity
```

## 2. Menjalankan unit test

Unit test berjalan di host (tanpa perangkat) dan menguji logika murni
(`UiFormat`, perhitungan tanggal keluar):

```bash
./gradlew testDebugUnitTest
```

Laporan HTML: `app/build/reports/tests/testDebugUnitTest/index.html`.

Menjalankan satu kelas saja:

```bash
./gradlew testDebugUnitTest --tests "com.miraya.kostku.UiFormatTest"
```

Test instrumen (butuh perangkat/emulator):

```bash
./gradlew connectedDebugAndroidTest
```

## 3. Melihat log

Semua diagnosa aplikasi memakai satu tag Logcat: **`Kostku`**.

```bash
adb logcat -s Kostku
```

Yang dicatat: nama metode, path, status HTTP, potongan body respons, ID entitas, ukuran byte,
dan keputusan UI. Token, password, dan header Authorization **tidak pernah** dicatat.

## 4. Menjalankan backend lokal (untuk uji end-to-end)

1. Buat database MySQL/MariaDB kosong bernama `kostku` (utf8mb4).
2. Impor skema:
   ```bash
   mysql -u <user> -p kostku < backend-php/database/schema.sql
   ```
   Jika database sudah berisi data dan belum punya tabel audit, jalankan migrasi:
   ```bash
   mysql -u <user> -p kostku < backend-php/database/migrations/001_tenant_audit_log.sql
   ```
3. Salin `backend-php/.env.example` menjadi `backend-php/.env` dan isi
   `DATABASE_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` (≥32 karakter acak),
   `GOOGLE_CLIENT_ID`, dan nilai `MEDIA_*`.
4. Sajikan via Apache/PHP-FPM. Uji endpoint kesehatan: `GET /v1/health` harus mengembalikan `{"data":{"status":"ok"}}`.
5. Untuk mengarahkan aplikasi ke server dev, ubah `BASE_URL` di
   `app/src/main/java/com/miraya/kostku/NetworkApi.kt` ke URL server Anda.

> Backend produksi berada di `backend-php/`. `public/index.php` **harus diunggah manual**
> ke server; perubahan lokal tidak otomatis terdeploy.

## 5. Login Google

Login Google wajib. Pastikan:
- `default_web_client_id` di `app/src/main/res/values/strings.xml` sama dengan
  Google **Web** client ID yang dipakai server (`GOOGLE_CLIENT_ID`).
- SHA-1 debug keystore terdaftar pada kredensial OAuth (agar ID token diterima).
- `app/google-services.json` sesuai project Firebase.

## 6. Checklist QA manual

Alur utama (di perangkat/emulator, dengan server aktif):

- [ ] **Login** Google → masuk ke `Penghuni`; matikan jaringan → tampil pesan error Bahasa Indonesia, bukan crash.
- [ ] **Urutan wajib**: di `Penghuni` tanpa data → muncul empty-state pengarah "buat lokasi, tipe kamar, harga, lalu nomor kamar" dan form tambah tidak terbuka. Setelah satu kamar dibuat, form tambah penghuni bisa dibuka.
- [ ] **Lokasi**: tambah (nama + alamat wajib), edit, buka detail → tab Dashboard/Kamar/Pembayaran; halaman **Pengaturan** lokasi memuat tipe kamar, nomor kamar, dan hapus lokasi.
- [ ] **Logo lokasi**: pada Pengaturan lokasi, pasang logo (Galeri/Kamera → crop) → logo tampil di kartu daftar lokasi; hapus logo → kembali ke ikon rumah.
- [ ] **Utilitas**: isi nomor token listrik & nomor PDAM di lokasi (Pengaturan) dan di kamar (Nomor kamar → Edit); keduanya opsional. Nol di depan tetap tersimpan (mis. `012345`).
- [ ] **Detail kamar**: kartu kamar hanya menampilkan **Detail** + **Edit**; Detail kamar menampilkan informasi + token listrik/PDAM (atau "Tidak tersedia"). Tidak ada tombol tambah/hapus di daftar kamar.
- [ ] **Kwitansi**: dari daftar pembayaran atau riwayat pembayaran penghuni → buka Kwitansi; header memakai logo lokasi (atau ikon rumah); pembayaran `paid` menampilkan cap **LUNAS**, `partial` **SEBAGIAN**, `pending` **BELUM LUNAS**; tombol Bagikan & PDF membuka lembar berbagi.
- [ ] **Bottom nav**: hanya muncul di 4 tab utama; semua layar dalam (detail, form, dialog) tanpa bottom nav.
- [ ] **Tipe kamar**: buat tipe → tambah harga (okupansi + durasi hari/minggu/bulan/tahun) → duplikat ditolak.
- [ ] **Gating harga→kamar**: buat tipe kamar tanpa harga → form "Tambah Banyak Kamar" menampilkan petunjuk "Belum ada tipe kamar yang punya harga…" dan pemilih tipe nonaktif; setelah menambah harga, tipe itu muncul dan kamar bisa dibuat.
- [ ] **Gating tipe lengkap→penghuni**: buat tipe kamar yang punya harga namun belum ada nomor kamar → di "Tambah penghuni", tipe itu **tidak** muncul di dropdown "Tipe kamar" (hanya tipe yang sudah punya harga **dan** nomor kamar yang tampil).
- [ ] **Kamar**: tambah banyak kamar (urutan angka / daftar manual, 1–100) → status Tersedia/Terisi/Maintenance/Kotor benar; buka edit kamar → ubah nama/tipe/status.
- [ ] **Penghuni**: tambah (nama wajib, HP opsional) → tanggal keluar mengikuti satuan durasi; daftar bisa dicari & difilter (Semua/Aktif/Sudah keluar). Tiap baris menampilkan **chip sisa hari** (hijau ≥14, kuning 7–13, merah <7; "Lewat N hari" bila sudah lewat; abu bila tanpa tanggal keluar) — bukan label "Aktif/Jatuh tempo".
- [ ] **Penghuni → pemasukan awal**: tambah penghuni Lunas → muncul **1 baris** di tab Pemasukan (nominal penuh, metode & kategori sesuai pilihan). Tambah "Belum lunas" dengan jumlah 0 → tetap muncul 1 baris `pending` bernominal 0.
- [ ] **Perpanjang kos**: dari Kelola penghuni → pilih durasi/harga, isi pembayaran (Lunas/Belum lunas, metode, kategori) → setelah simpan, `Tanggal keluar` penghuni berubah **dan** muncul **1 baris pemasukan** baru di tab Pemasukan, plus entri **"Diperpanjang"** di `Log Perubahan Data`.
- [ ] **Detail penghuni**: tab Informasi (memuat **Utilitas kamar** saja; nilai kosong = "Tidak tersedia"), Riwayat Pembayaran, dan **Log Perubahan Data** menampilkan entri nyata setelah edit/pindah/perpanjang/keluar.
- [ ] **Tandai keluar**: dari Kelola penghuni → Penghuni keluar → "Tandai keluar hari ini" → muncul halaman **Status kamar** (chip Tersedia/Kotor/Maintenance, bawaan Tersedia). Setelah konfirmasi: penghuni hilang dari daftar aktif, muncul di **Pengaturan → Riwayat → Penghuni keluar** bulan berjalan, dan kamar berstatus sesuai pilihan (cek di tab Lokasi → Kamar). Tidak ada tombol arsip manual.
- [ ] **Hapus berkonfirmasi**: kamar, tipe kamar, dan harga menampilkan dialog konfirmasi; `Batal` tidak menghapus apa pun.
- [ ] **Pembayaran**: catat (periode + jumlah > 0), filter status, edit; menu titik-tiga (**⋮**) membuka **Edit**/**Hapus**; hapus dengan konfirmasi menghilangkan baris secara permanen.
- [ ] **Pembayaran → tab**: `Semua` menampilkan gabungan pemasukan+pengeluaran + kartu total; `Pemasukan` berfilter status; `Pengeluaran` berfilter jenis. Catat pengeluaran operasional (mis. Listrik/Air) **dan** refund ke penghuni → muncul di `Pengeluaran` dan `Semua`; edit lalu hapus (dengan konfirmasi).
- [ ] **Kategori (Pengaturan → Kategori)**: tab Pemasukan/Pengeluaran; tambah/ubah/hapus kategori (hapus dengan konfirmasi, `Batal` tidak menghapus). Buka *Catat pengeluaran* → dropdown Kategori menampilkan kategori pengaturan; buka *Catat pembayaran* → dropdown `Kategori (opsional)` menampilkan kategori pemasukan; simpan satu catatan dengan kategori baru → kategori tampil di baris/ledger. Menghapus/mengganti nama kategori **tidak** mengubah catatan lama.
- [ ] **Foto**: Galeri/Kamera → crop → `Lanjutkan`/`Ambil ulang`; ganti & hapus foto; preview remote menampilkan `Memuat foto…`/`Foto tidak dapat dimuat.`
- [ ] **Skenario gagal**: tolak jaringan saat simpan → form tetap terbuka dan nilai tidak hilang.
- [ ] **Keluar**: sesi & token terhapus, kembali ke layar login.
