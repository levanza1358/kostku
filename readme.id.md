# Kostku

Kostku adalah aplikasi Android untuk pemilik/pengelola kos untuk mengelola lokasi, kamar, penghuni, harga tipe kamar, pembayaran manual, pengeluaran, dan foto dengan data server.

## Kondisi saat ini

- Android menggunakan Kotlin, Jetpack Compose, dan Material 3.
- Backend produksi adalah PHP native + PDO dengan MySQL 8/MariaDB di `https://kostku.miraya.my.id/v1/`.
- Login Google wajib. Access/refresh token dipakai untuk pemulihan sesi dan kini **permanen**: refresh token berlaku 1 tahun dan diperbarui tiap dipakai. Gangguan jaringan sesaat tidak membuat logout — permintaan aman dicoba ulang otomatis dan sesi dipertahankan; hanya 401 yang mengakhiri sesi, dengan penanganan kedaluwarsa berbahasa Indonesia.
- Empat tujuan bottom navigation: `Penghuni`, `Lokasi`, `Pembayaran`, `Pengaturan`. Detail penghuni adalah halaman penuh tanpa bottom navigation dengan tab `Informasi`, `Riwayat Pembayaran`, dan `Log Perubahan Data`. Detail lokasi membuka pengelolaan kamar.
- Data bisnis berasal dari server. Setelah simpan berhasil, snapshot dimuat ulang; jika gagal, form tetap terbuka. CRUD memeriksa kepemilikan dan dependensi. Tidak ada data contoh atau CRUD lokal.
- Semua teks UI, validasi, loading, snackbar, error, dan empty state menggunakan Bahasa Indonesia.

## Fitur yang sudah diterapkan

Lokasi memiliki nama, cabang, alamat, dan deskripsi. Status kamar adalah Tersedia, Terisi, Maintenance, atau Kotor. Urutan pembuatan bersifat wajib: **lokasi → tipe kamar → harga → nomor kamar**. Tipe kamar dibuat lebih dahulu, harga dinormalisasi berdasarkan jumlah penghuni dan durasi Hari/Minggu/Bulan/Tahun, dan **nomor kamar hanya bisa dibuat untuk tipe kamar yang sudah punya harga** (server menolak bila belum ada harga; form hanya menampilkan tipe berharga). Tambah Banyak Kamar mendukung urutan angka atau daftar custom, 1–100 kamar, validasi duplikat, dan transaksi server.

Tambah penghuni hanya mewajibkan nama; nomor HP opsional. Karena penempatan butuh kamar dengan harga, **tambah penghuni baru bisa setelah rantai di atas lengkap** (minimal ada satu kamar). Tanggal masuk default ke waktu perangkat. Tanggal keluar dihitung dari durasi dan dapat diedit dengan mengetuk tanggal; tidak ada toggle custom move-out dan waktu keluar tetap mengikuti waktu masuk. Pembayaran awal default `Lunas`, dengan **metode** dan **kategori pemasukan**; penempatan **selalu** menulis satu baris pemasukan (nominal penuh bila Lunas, atau nominal sebagian/nol bila belum lunas). **Perpanjang kos** memperbarui masa sewa **dan mencatat satu baris pemasukan** dalam satu transaksi, sehingga setiap perpanjangan masuk ke data pemasukan dan log perubahan penghuni.

### Foto

Sumber foto selalu dipilih secara eksplisit melalui `Galeri` atau `Kamera`. Foto di-crop tengah secara deterministik, ditampilkan sebagai pratinjau, lalu harus dikonfirmasi dengan `Lanjutkan`; `Retake` membuang foto sementara. Orientasi EXIF diterapkan, sisi terpanjang diubah menjadi maksimal 1280 px, lalu foto dikompresi JPEG dengan target maksimal 500 KB. Foto asli/resolusi tinggi tidak diunggah atau disimpan.

### Pembayaran

Layar `Pembayaran` memiliki tiga tab: **Semua**, **Pemasukan**, dan **Pengeluaran**. Tab **Semua** menggabungkan pemasukan dan pengeluaran dalam satu buku besar (terbaru lebih dulu) beserta total pemasukan dan total pengeluaran. Satu FAB **+** memunculkan menu (Pemasukan/Pengeluaran, bukan bottom sheet). **Pemasukan** adalah pembayaran penghuni (dengan filter status). **Pengeluaran** mencakup biaya operasional maupun refund ke penghuni; tiap catatan bisa diedit/dihapus dan mendukung bukti foto opsional. **Kategori pemasukan & pengeluaran bisa diatur sendiri** lewat **Pengaturan → Kategori** (global per akun); catatan menyimpan nama kategori sehingga mengubah daftar tidak mengubah catatan lama.

Foto digunakan untuk penghuni, kamar, dan bukti pembayaran. Satu entitas hanya memiliki satu foto aktif. Penggantian menyimpan metadata foto baru lebih dahulu lalu menghapus file lama; penghapusan melakukan soft-delete metadata dan menghapus file. Pratinjau remote memiliki status memuat dan error `Foto tidak dapat dimuat.`

Entitas dibuat lebih dahulu, baru upload foto dijalankan. Jika upload gagal, entitas yang sudah dibuat tidak dibuat ulang otomatis; pengguna harus mengulangi operasi foto atau mengedit entitas yang ada. Kegagalan upload tidak me-rollback entitas.

Server memvalidasi kepemilikan, MIME aktual (JPEG/PNG/WebP), dimensi gambar, dan ukuran. Byte foto disimpan di filesystem VPS, bukan blob database; database hanya menyimpan metadata dan relative path.

Tab log perubahan penghuni kini terhubung ke audit log sungguhan: API mencatat event buat, ubah, pindah kamar, perpanjang, dan keluar di tabel `tenant_audit_log`, lalu aplikasi membacanya dari `GET /tenants/{id}/audit`. Pengaturan berisi `Ringkasan`, `Profile`, `Tema`, `Tentang Aplikasi`, `Open Source`, `Bantuan`, `Privasi`, dan `Keluar`.

## Deployment media

Impor `backend-php/database/schema.sql` (satu-satunya sumber skema; sudah termasuk tabel `media`) ke database yang masih kosong. Atur `MEDIA_STORAGE_ROOT`, `MEDIA_PUBLIC_BASE_URL`, `MEDIA_MAX_BYTES`, dan `MEDIA_MAX_SIDE` di environment server, siapkan direktori VPS yang writable, lalu deploy PHP dan aturan `.htaccess`. Pastikan `.env`, konfigurasi, dan database tetap terblokir. `backend-php/public/index.php` harus diunggah manual.

Lihat [README.md](README.md), [alur pengguna](docs/USER-FLOWS.md), dan [indeks docs](docs/).

Folder Node/PostgreSQL adalah legacy, bukan backend produksi.
