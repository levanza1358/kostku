# Perbandingan Mock vs Aplikasi — Kostku

**Sumber mock:** `mock/kostku-mock.html` (+ render `mock/kostku-mock.png`)
**Sumber aplikasi:** `app/src/main/java/com/miraya/kostku/`
**Terakhir diaudit:** 2026-10-12 (audit ulang frame-per-frame; mode gelap & terang, emulator Pixel_6a / Android 34+)

Dokumen ini melacak seberapa mirip tiap layar dengan mock. Tujuannya agar "sudah
100%" bisa dibuktikan frame-per-frame, bukan ditebak.

## Legenda

| Simbol | Arti |
|---|---|
| ✅ **Identik** | Sudah dicocokkan dengan mock (biasanya dilihat langsung di emulator). Tidak ada beda tata letak/label yang berarti. |
| ⚠️ **Sebagian** | Inti layar sama, tetapi ada **tambahan/penyesuaian fungsional** yang tidak ada di mock (mis. field pembayaran nyata, pilihan mode). Bukan sekadar beda visual. |
| ❔ **Belum diverifikasi** | Cocok menurut kode, **belum** dibandingkan dengan mata di emulator. Jangan anggap final sampai dicek. |

> Catatan: mode **Terang** (mock 8) memakai tema yang sama seperti mode gelap —
> sudah diverifikasi di emulator dan seluruhnya bertanda ✅.

## Ringkasan angka

| Status | Jumlah frame |
|---|---|
| ✅ Identik | **50** |
| ⚠️ Sebagian | **0** |
| ❔ Belum diverifikasi | **0** |
| **Total** | **50** |

---

## 1. Masuk

| # | Layar (mock) | Berkas | Status |
|---|---|---|---|
| 2 | Masuk | `ui/common/LoginScreen.kt` | ✅ |

## 2. Tab Penghuni

| # | Layar (mock) | Berkas | Status | Catatan |
|---|---|---|---|---|
| 3.1 | Daftar penghuni | `features/tenant/TenantListScreen.kt` | ✅ | Kolom cari + chip sisa hari. |
| 3.2 | Keadaan kosong | `features/tenant/TenantListScreen.kt` | ✅ | "Tekan tombol + untuk menambah penghuni pertama." |
| 3.3 | Detail — Informasi | `TenantDetailTabs.kt` | ✅ | Satu kartu + UTILITAS KAMAR/LOKASI. |
| 3.4 | Detail — Pembayaran | `TenantDetailTabs.kt` | ✅ | Kartu + kartu putus-putus kwitansi. |
| 3.5 | Detail — Log perubahan | `TenantDetailTabs.kt` | ✅ | |
| 3.6 | Sheet "Kelola penghuni" | `features/tenant/TenantActionSheet.kt` | ✅ | |
| 3.7 | Form tambah penghuni | `features/tenant/AddTenantScreen.kt` | ✅ | DATA PENGHUNI/PENEMPATAN/SEWA. |
| 3.8 | Form edit penghuni | `features/tenant/TenantEditScreen.kt` | ✅ | Tata letak datar: DATA PENGHUNI (Nama/HP) + FOTO PENGHUNI + catatan; CTA "Simpan perubahan". |
| 3.9 | Pindah kamar | `features/tenant/TenantMoveScreen.kt` | ✅ | Kartu "Kamar saat ini" + kartu "KAMAR TUJUAN" beraksi: baris kamar kosong (avatar kunci, centang) + catatan putus-putus. CTA "Pindahkan" berikon swap. |
| 3.10 | Perpanjang kos | `features/tenant/TenantExtendScreen.kt` | ✅ | Kartu tanggal keluar + dropdown Jumlah penghuni/Durasi + kartu aksen "TANGGAL KELUAR BARU" + Lunas/Belum lunas. CTA "Perpanjang" berikon repeat. |
| 3.11 | Pindah tanggal | `features/tenant/TenantDatesScreen.kt` | ✅ | Dua bidang tanggal + catatan putus-putus; tombol "Kosongkan" dihapus. |

## 3. Tab Lokasi

| # | Layar (mock) | Berkas | Status | Catatan |
|---|---|---|---|---|
| 4.1 | Daftar lokasi | `features/location/LocationScreens.kt` | ✅ | Logo + chip + Buka + ikon edit/hapus. |
| 4A | Detail — Dashboard | `features/location/LocationDashboardTab.kt` | ✅ | |
| 4B | Detail — Kamar | `features/location/LocationRoomsTab.kt` | ✅ | Kartu hanya Detail + Edit. |
| 4C | Detail — Pembayaran | `features/location/LocationPaymentsTab.kt` | ✅ | Ringkasan kartu dihapus (sesuai mock). |
| 4D | Detail — Pengaturan | `features/location/LocationSettingsScreen.kt` | ✅ | |
| 4.5 | Daftar harga | `features/roomtype/RoomTypePriceScreen.kt` | ✅ | "1 orang • 1 bulan" (huruf kecil). |
| 4.6 | Form tambah harga | `features/roomtype/RoomTypePriceScreen.kt` (`PriceDialog`) | ✅ | |
| 4.7 | Edit kamar | `features/room/RoomScreens.kt` (`RoomEditScreen`) | ✅ | STATUS (chip) + UTILITAS + FOTO KAMAR. |
| 4.8 | Tambah banyak kamar | `features/room/BatchRoomScreen.kt` | ✅ | Satu mode: Tipe + Awalan + Jumlah + kartu "Akan dibuat"; CTA "Generate kamar" menempel di bawah. |
| 4.9 | Form tambah lokasi | `features/location/LocationEditScreen.kt` | ✅ | |
| 4.10 | Dialog konfirmasi hapus | `ui/common/LocationDialogs.kt` | ✅ | "Hapus lokasi? / Lokasi "<nama>" beserta kamar…" + tombol Hapus **merah** (destruktif). |
| 4.11 | Kelola tipe kamar | `features/roomtype/RoomTypeScreens.kt` | ✅ | Avatar + tombol Edit + ikon hapus. |
| 4.12 | Dialog tambah tipe | `features/roomtype/RoomTypeScreens.kt` (`TypeNameDialog`) | ✅ | Judul "Tambah tipe kamar" + bidang "Nama tipe kamar" + Batal/Simpan. |
| 4.13 | Dialog tambah harga | `features/roomtype/RoomTypePriceScreen.kt` (`PriceDialog`) | ✅ | Sama dengan 4.6. |
| 4.14 | Nomor kamar | `features/location/RoomNumberScreen.kt` | ✅ | Avatar kunci + ikon pensil. |
| — | Detail nomor kamar | `features/location/RoomDetailScreen.kt` | ✅ | Hero ikon kunci + chip status, satu kartu kv Penghuni/token/PDAM, "Tidak tersedia" bila kosong. |
| 4.15 | Edit nomor kamar | `features/room/RoomScreens.kt` (`compact`) | ✅ | Varian ringkas (tanpa status/foto). |
| 4.16 | Edit lokasi + logo | `features/location/LocationEditScreen.kt` | ✅ | |
| 4.17 | Kwitansi pakai logo | `features/receipt/ReceiptScreen.kt` | ✅ | |
| 4.18 | Kwitansi tanpa logo | `features/receipt/ReceiptScreen.kt` | ✅ | Fallback ikon rumah. |

## 4. Tab Pembayaran

| # | Layar (mock) | Berkas | Status | Catatan |
|---|---|---|---|---|
| 5.1 | Pembayaran — Semua | `features/payment/PaymentsScreen.kt` + `PaymentLedgerTab.kt` | ✅ | Segmented pill. |
| 5.2 | Pembayaran — Pemasukan | `features/payment/PaymentIncomeTab.kt` | ✅ | Satu kartu statistik penuh. |
| 5.3 | Pembayaran — Pengeluaran | `features/payment/PaymentExpenseTab.kt` | ✅ | Satu kartu statistik penuh. |
| 5.4 | Dialog catat pemasukan | `features/payment/PaymentDialog.kt` | ✅ | Kategori + Metode 2 kolom. |
| 5.5 | Dialog catat pengeluaran | `features/payment/ExpenseDialog.kt` | ✅ | |
| 5.6 | Kwitansi | `features/receipt/ReceiptScreen.kt` | ✅ | |

## 5. Tab Pengaturan

| # | Layar (mock) | Berkas | Status | Catatan |
|---|---|---|---|---|
| 6.1 | Pengaturan | `ui/settings/SettingsScreen.kt` | ✅ | Baris Tema/Tentang/Bantuan/Keluar ada di bawah lipatan. |
| 6.2 | Kategori | `ui/settings/CategorySettingsScreen.kt` | ✅ | Segmented pill + ikon edit/hapus. |
| 6.3 | Riwayat penghuni keluar | `ui/settings/MoveOutHistoryScreen.kt` | ✅ | Navigasi bulan ‹ ›; baris "Kamar N • tanggal • Rp" + chip "Keluar". |
| 6.4 | Tema | `ui/settings/ThemeScreen.kt` | ✅ | Pil MODE (Terang/Gelap/Hitam) + grid 5 kolom AKSEN WARNA (swatch bulat + cincin). |
| 6.5 | Tentang / Info | `ui/settings/InfoScreen.kt` | ✅ | Hero ikon pin (kotak aksen) + Kostku/Versi 1.0 + kartu deskripsi + 3 baris (Syarat/Kebijakan/Bantuan) tanpa subjudul. |

## 6. Komponen & Keadaan

| # | Layar (mock) | Berkas | Status | Catatan |
|---|---|---|---|---|
| 7.1 | Banner error | `App.kt` (banner `error`/`actionError`) | ✅ | Persisten + tombol Tutup (+ Coba lagi). |
| 7.2 | Toast sukses | `App.kt` (`notify`) | ✅ | `Toast` sistem, tampil di semua layar. |
| 7.3 | Sheet sumber foto | `media/PhotoSource.kt` | ✅ | "Pilih sumber foto / Galeri / Kamera". |
| 7.4 | Memuat | `App.kt` (indikator muat) | ✅ | Spinner + "Memuat data…". |

## 7. Mode Terang (mock 8)

| # | Layar (mock) | Berkas | Status | Catatan |
|---|---|---|---|---|
| 8 | Terang — Penghuni | (tema `light`) | ✅ | Diverifikasi di emulator mode Terang (baris + chip + FAB + navbar). |
| 8 | Terang — Perpanjang kos | (tema `light`) | ✅ | Diverifikasi di emulator mode Terang. |
| 8 | Terang — Kwitansi | (tema `light`) | ✅ | Diverifikasi di emulator mode Terang (cap LUNAS + tombol Bagikan/PDF). |

---

## Riwayat perubahan status

Seluruh 50 frame kini **✅ Identik** (⚠️ = 0, ❔ = 0).

### Penyimpangan sadar dari mock (atas permintaan pemilik)

| Tanggal | Layar | Penyimpangan | Alasan |
|---|---|---|---|
| 2026-10-12 | Penghuni keluar (`features/tenant/TenantExitScreen.kt`) | Tombol "Arsipkan penghuni" **dihapus** — menandai keluar kini **otomatis mengarsipkan** penghuni ke riwayat Penghuni keluar; setelah itu muncul **halaman baru "Status kamar"** yang menanyakan status kamar pasca-keluar (chip **Tersedia / Kotor / Maintenance**, bawaan Tersedia). Mock tidak punya frame untuk layar ini. | Permintaan langsung pemilik: arsip otomatis + tanya status kamar saat penghuni keluar. |

### Gelombang 3 — audit ulang frame-per-frame (2026-10-12)

Audit ulang menemukan beberapa frame yang sebelumnya ditandai ✅ ternyata **belum
persis** mock. Semuanya kini diperbaiki:

| Frame | Beda yang ditemukan | Tindakan |
|---|---|---|
| 2 Masuk | Logo bukan kotak pin; tombol tanpa ikon orang | Logo → kotak aksen berisi ikon pin (76 dp); tombol "Masuk dengan Google" ditambah ikon orang. |
| 3.2 Keadaan kosong | Ikon bukan kotak 84 dp; tanpa tombol "Tambah penghuni" | Kotak ikon 84 dp (rounded, latar variant, ikon 40 dp muted) + tombol tonal "Tambah penghuni". |
| 3.3/3.4/3.5 + 4A–4D | Strip tab berikon di atas label + ber-inset | `AppTabRow` diganti: **teks saja**, tinggi 52 dp, garis bawah penuh + indikator aksen 3 dp, tanpa inset (mock `.tabs`). |
| 3.3 Detail — Informasi | Catatan kaki "Nomor utilitas opsional…" | Dihapus (tidak ada di mock). |
| 3.3 Detail | FAB "Kelola" (extended, ikon tune) | Diganti **FAB kecil ikon roda gigi** (`.fab.sm`). |
| 3.5 Log perubahan | Baris berkartu berikon + format "…, HH:MM WIB" | Diganti **kartu polos 2 baris**: judul + "tgl • jam — detail" (tanpa ikon, tanpa WIB, detail di baris yang sama). |
| 3.6 Sheet kelola | Baris berkartu + subjudul nama penghuni | Diganti **baris datar** batas-tipis (`.sheetrow`); subjudul nama dihapus; ikon "Penghuni keluar" **amber**. |
| 3.10 Perpanjang | Kartu kv label-atas-nilai + chip M3 + sechead bergaris | Kartu "Tanggal keluar saat ini" → baris kv kiri/kanan (`KeyValueRow`); chip status → **`SelectChip`** (hijau/netral); judul bagian → kapital tanpa garis. |
| 4.1 Daftar lokasi | Chip "N kamar" biru; "0 terisi" biru | Chip "terisi" **hijau bila > 0, abu bila 0**; "N kamar" selalu abu (`.chip.neutral`). |
| 4A Dashboard | Kartu stat ikon-atas; "Catat pembayaran" ikon dompet | Kartu stat → **ikon + label sebaris, angka besar** (`.stat`); badge % → chip aksen; tombol aksi → ikon +/tren & lebih ringkas. |
| 4B Kamar | Tombol Edit outline | Diganti **ikon edit** (`.iconbtn`). |
| 4C Pembayaran | Filter chip terpisah | Diganti **kapsul segmen** `.seg` (`.Seg` via `SegmentedPillCompact`). |
| 4.16 Edit lokasi | +field "Deskripsi (opsional)" | Dihapus (tidak ada di mock). |
| 4.17/5.6 Kwitansi | Struktur disederhanakan berlebihan | Dipulihkan ke **format struk 5.6**: No. kwitansi … Tanggal bayar → TOTAL → cap LUNAS + "Dibuat …" sejajar → garis putus → ucapan terima kasih (di dalam kartu). |
| 7.3 Sheet sumber foto | Baris ber-subjudul | Diganti baris **satu baris** (ikon + label) batas-tipis, seperti `.sheetrow`. |

### Gelombang 2 — 8 frame ❔ → ✅ (verifikasi + penyelarasan)

| Frame | Hasil verifikasi | Bila ada beda, tindakan |
|---|---|---|
| 3.8 Form edit penghuni | Cocok | Tata letak diubah dari kartu → **datar** (DATA PENGHUNI + FOTO PENGHUNI) seperti mock. |
| 4.10 Dialog hapus lokasi | Cocok | Tombol "Hapus" dibuat **merah** (destruktif), sama seperti mock. |
| 4.12 Dialog tambah tipe kamar | Cocok | — |
| 6.3 Riwayat penghuni keluar | Cocok | Subjudul disederhanakan ke "Kamar N • tanggal • Rp"; baris "N penghuni keluar" dihapus (tidak ada di mock). |
| 6.5 Tentang Aplikasi | Cocok | Hero diganti **ikon pin** dalam kotak aksen (bukan huruf "K"); deskripsi dipindah ke kartu; baris Syarat/Kebijakan/Bantuan **tanpa subjudul**. |
| Terang — Penghuni | Cocok | — |
| Terang — Perpanjang kos | Cocok | — |
| Terang — Kwitansi | Cocok | — |

### Gelombang 1 — 6 frame ⚠️ → ✅

Enam frame yang dahulu ⚠️ (aplikasi menambah fungsi di luar mock) sudah
diselaraskan agar persis mock:

| Frame | Beda lama | Tindakan |
|---|---|---|
| 3.9 Pindah kamar | dropdown kamar tujuan | Diganti daftar kartu kamar kosong (avatar kunci + centang) + catatan putus-putus; label "Kamar N"; CTA berikon swap. |
| 3.10 Perpanjang | +diskon/dibayar/metode/tanggal | Diringkas ke Jumlah penghuni + Durasi → kartu "TANGGAL KELUAR BARU"; Lunas/Belum lunas; metode/kategori diisi nilai bawaan diam-diam. CTA berikon repeat. |
| 3.11 Pindah tanggal | +tombol kosongkan | Tombol dihapus; dua bidang tanggal + catatan putus-putus. |
| Detail kamar | +Penghuni/Utilitas/foto | Satu kartu kv (Penghuni/token/PDAM); hero ikon kunci + chip status. |
| 4.8 Tambah banyak kamar | +2 mode/akhiran/mulai | Dikunci ke satu mode (Awalan + Jumlah) + kartu "Akan dibuat". |
| 6.4 Tema | +kartu Pratinjau + radio | Kartu Pratinjau dihapus; MODE pil + grid AKSEN WARNA. |

Semua perubahan ini dipertahankan fungsinya: data yang dahulu diisi lewat bidang
yang dihapus kini memakai nilai bawaan aman, sehingga pencatatan ke backend tetap
lengkap.

## Cara memperbarui dokumen ini

1. Buka frame di aplikasi (emulator) **dan** mock (`mock/kostku-mock.html`).
2. Bandingkan tata letak, label, urutan, dan warna.
3. Ubah status baris ini: ❔ → ✅ (jika cocok) atau → ⚠️ (jika ada beda, tulis di Catatan).
4. Perbarui **Ringkasan angka** di atas.
