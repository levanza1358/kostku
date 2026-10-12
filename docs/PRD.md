# Product Requirements Document — Kostku

**Status:** Current product baseline
**Platform:** Android
**Target:** Pemilik dan pengelola kos

## 1. Ringkasan

Kostku adalah aplikasi Android Kotlin + Jetpack Compose Material 3 untuk mengelola lokasi kos, tipe/harga kamar, kamar, penghuni, dan pembayaran melalui API produksi native PHP + PDO + MySQL/MariaDB.

## 2. Tujuan dan batasan

Kostku memusatkan data operasional, mempercepat pencatatan penghuni/pembayaran, menampilkan okupansi dan ringkasan, serta menjaga isolasi data per akun. Payment gateway, aplikasi penghuni, booking online, chat internal, akuntansi/pajak lengkap, dan WhatsApp otomatis tetap di luar cakupan saat ini.

## 3. Navigasi saat ini

Bottom navigation memiliki tepat empat tujuan: `Penghuni`, `Lokasi`, `Pembayaran`, dan `Pengaturan`. Detail penghuni adalah halaman penuh tanpa bottom navigation dengan tab `Informasi`, `Riwayat Pembayaran`, dan `Log Perubahan Data`. Pengaturan memiliki subhalaman `Ringkasan`, `Profile`, `Kategori`, `Tema`, `Tentang Aplikasi`, `Open Source`, `Bantuan`, `Privasi`, dan `Keluar`.

## 4. Requirements saat ini

### AUTH

- Login Google wajib dan Google ID token diverifikasi backend.
- Backend menerbitkan access/refresh token; access token diperbarui otomatis setelah 401.
- Sesi dipulihkan melalui `/me`; refresh yang gagal menghapus sesi dan menampilkan pesan sesi berakhir dalam Bahasa Indonesia.
- Logout menghapus sesi dan token.

### DATA DAN LOKASI

- Lokasi mendukung nama, cabang, alamat, dan deskripsi; nama/alamat wajib.
- Data berasal dari server, bukan local-only atau mock. Setelah mutasi sukses, snapshot dimuat ulang.
- Gagal jaringan mempertahankan input dan menampilkan retry/feedback Bahasa Indonesia.

### KAMAR

- **Urutan wajib: lokasi → tipe kamar → harga → nomor kamar.** Tipe kamar dibuat sebelum kamar, dan **harga wajib dibuat sebelum nomor kamar**. Backend menolak pembuatan kamar (placeholder tersendiri) bila tipe kamar belum punya harga; aplikasi hanya menampilkan tipe kamar yang sudah berharga pada form "Tambah Banyak Kamar".
- Harga dinormalisasi berdasarkan okupansi dan durasi Hari/Minggu/Bulan/Tahun.
- Tambah Banyak Kamar mendukung urutan angka atau custom list, 1–100 nama, validasi duplikat, dan transaksi atomik.
- Status kamar: `vacant`/Tersedia, `occupied`/Terisi, `maintenance`/Maintenance, `dirty`/Kotor.

### PENGHUNI

- Nama wajib; nomor HP opsional.
- Penempatan memerlukan lokasi, tipe kamar, kamar Tersedia, dan harga.
- **Prasyarat wajib sebelum menambah penghuni:** aplikasi memblokir/mengarahkan menambah penghuni hingga rantai **lokasi → tipe kamar → harga → nomor kamar** lengkap. Pada form penempatan, **hanya tipe kamar yang lengkap (sudah punya harga DAN sudah punya nomor kamar)** yang muncul di dropdown "Tipe kamar"; tipe yang belum lengkap disembunyikan, dan muncul pesan bila lokasi belum punya tipe lengkap.
- Move-in default waktu perangkat. Move-out dihitung dari durasi harga, dan tanggalnya dapat diedit dengan mengetuk tanggal. Tidak ada custom move-out toggle atau pemilih waktu keluar terpisah.
- Pembayaran awal default Lunas, dengan pilihan **metode** dan **kategori pemasukan**. Penempatan **selalu** menulis satu baris pembayaran (nominal penuh bila Lunas; nominal sebagian/nol sebagai `partial`/`pending` bila belum lunas), lalu perubahan kamar ke Terisi — semuanya transaksional.
- **Perpanjang kos** memperbarui `move_out_date` dan `monthly_rent` penghuni **dan** mencatat satu baris pemasukan (diskon, status, jumlah bayar, metode, kategori) secara transaksional (`POST /tenants/{id}/extend`), sehingga setiap perpanjangan masuk ke data pemasukan/pembayaran dan tercatat sebagai aksi `extended` di log perubahan.
- Detail penghuni menampilkan informasi dan riwayat pembayaran. Log perubahan data kini bersumber dari audit log backend (`tenant_audit_log`, `GET /tenants/{id}/audit`).

### PEMBAYARAN

- Layar Pembayaran memiliki tiga tab: **Semua**, **Pemasukan**, **Pengeluaran**. Tab **Semua** menggabungkan pemasukan dan pengeluaran dalam satu buku besar (terbaru lebih dulu) beserta ringkasan total pemasukan dan total pengeluaran. Satu FAB **+** memunculkan menu **Pemasukan**/**Pengeluaran** (pop-up, bukan bottom sheet).
- Pemasukan (pembayaran penghuni) dicatat manual, memiliki periode, nominal, tanggal, metode, dan status. Riwayat dapat dilihat/difilter. Tiap baris punya menu titik-tiga (**⋮**) berisi **Edit** dan **Hapus**; penghapusan dikonfirmasi dan menghapus catatan secara permanen.
- **Pengeluaran** mencakup **biaya operasional** (listrik/air, perbaikan, kebersihan, gaji, internet, dll.) **dan refund/pengembalian ke penghuni** (terhubung ke penghuni tertentu). Dapat diedit dan dihapus; mendukung bukti foto opsional.
- **Kategori pemasukan & pengeluaran dapat diatur sendiri** oleh pemilik melalui `Pengaturan → Kategori` (global per akun). Kategori dipakai sebagai pilihan di dialog pencatatan; catatan menyimpan nama kategori sehingga mengubah daftar tidak mengubah catatan lama.
- Bukti pembayaran/pengeluaran mendukung satu foto. Foto tenant dan satu foto kamar juga didukung.
- Alur foto: Galeri/Kamera → crop preview → kompresi maksimal 1280px dan sekitar 500KB → upload media ke VPS.

### FOTO DAN MEDIA

- Foto tenant, satu foto kamar, dan foto bukti pembayaran menggunakan alur media yang sama: Galeri/Kamera → crop preview → kompresi maksimal 1280px dan sekitar 500KB → upload media ke VPS.
- Log perubahan data memakai audit log backend (`tenant_audit_log`) yang mencatat event buat, ubah, pindah, perpanjang, dan keluar penghuni.

## 5. Aturan non-fungsional

Semua API produksi menggunakan HTTPS pada `https://kostku.miraya.my.id/v1/`; Android tidak mengakses database langsung. UI memakai Bahasa Indonesia dan mendukung loading, content, empty, validasi, network error, serta success state. Secret tidak di-commit/log. MySQL/MariaDB tetap privat dan dibackup.

## 6. Acceptance criteria

MVP berjalan bila pengguna dapat login Google, memulihkan sesi, membuat lokasi/tipe/harga/kamar, menambah dan melihat detail penghuni, mencatat pembayaran, memindahkan/menandai keluar (tandai keluar mengarsipkan penghuni otomatis ke riwayat Penghuni keluar lalu menanyakan status kamar baru: Tersedia / Kotor / Maintenance), logout, melihat log perubahan penghuni, dan hanya melihat data yang diizinkan server.

## 7. Deployment note

Backend produksi berada di `backend-php/`. File `index.php` produksi harus diunggah manual ke server.

## 8. Open questions / future

Admin/staff multi-role, notifikasi jatuh tempo, dan payment gateway dapat direncanakan kemudian. Audit log penghuni (backend + UI) sudah tersedia.
