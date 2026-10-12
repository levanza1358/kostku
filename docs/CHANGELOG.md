# Changelog — Kostku

## Unreleased — hapus backend legacy `backend/` (Node/PostgreSQL)

### Removed
- **Folder `backend/` (Node/PostgreSQL, legacy) dihapus** dari repo dan disk. Backend produksi satu-satunya adalah `backend-php/` (PHP native + PDO + MySQL/MariaDB).
- Referensi "Folder Node/PostgreSQL adalah legacy" di `README.md`, `readme.id.md`, `docs/PRD.md`, `docs/DATABASE.md`, dan `docs/RULES.md` dibersihkan; `.gitignore` disesuaikan (`backend/dist/` → `dist/`).
- Verifikasi: `./gradlew assembleDebug` **BUILD SUCCESSFUL** setelah penghapusan (Gradle hanya menyertakan modul `:app`, tidak ada dependensi ke `backend/`).

## Unreleased — Tandai keluar: arsip otomatis + pilihan status kamar

### Changed
- **"Tandai keluar" kini otomatis mengarsipkan penghuni ke riwayat "Penghuni keluar"** — tombol "Arsipkan penghuni" manual di halaman Penghuni keluar dihapus; teks pengganti: "Data penghuni otomatis dipindahkan ke riwayat Penghuni keluar; riwayat pembayaran tetap tersimpan."
- **Halaman baru "Status kamar"** muncul setelah menekan "Tandai keluar hari ini" (`TenantExitScreen.kt`): menanyakan status kamar pasca-keluar lewat chip **Tersedia / Kotor / Maintenance** (bawaan: Tersedia), dengan catatan bahwa status bisa diubah lagi lewat Edit kamar di tab Lokasi.
- **Backend** (`backend-php/public/index.php`): `POST /tenants/{id}/move-out` menerima **`room_status`** (`vacant`/`dirty`/`maintenance`, bawaan `vacant`) — status kamar diterapkan transaksional bersama arsip riwayat; nilai lain ditolak 400. Respons mengembalikan `room_status` yang diterapkan. Sudah di-deploy ke produksi (backup `index.php.bak.moveout` di server; health check 200).
- **App API**: `KostkuApi.moveOut(id, date, roomStatus)` mengirim `room_status` (bawaan `vacant` sehingga kompatibel mundur).
- **Panduan** (`InfoScreen.kt`): butir "Arsipkan penghuni" diganti dengan penjelasan alur tandai keluar baru.

## Unreleased — audit ulang frame-per-frame (Gelombang 3)

### Changed
- **Audit ulang 1-per-1 terhadap mock** menemukan ~17 ketidaksesuaian pada frame yang sebelumnya ditandai ✅; semuanya kini diselaraskan:
  - **2 Masuk / 3.2 Keadaan kosong**: logo dan teks tombol/empty-state disamakan persis dengan mock.
  - **Strip tab (`.tabs`)** (`ui/common/TabComponents.kt`): `AppTabRow` ditulis ulang — teks saja (tanpa ikon), tinggi 52dp, garis bawah 1px penuh, indikator aksen 3px; dipakai Detail penghuni (Informasi/Pembayaran/Log) dan Detail lokasi (Dashboard/Kamar/Pembayaran).
  - **3.3 Detail — Informasi** (`TenantDetailTabs.kt`): catatan kaki di bawah kartu UTILITAS dihapus; FAB "Kelola penghuni" kini ikon roda gigi.
  - **3.5 Log perubahan**: format kartu diratakan — judul aksi + "10 Okt 2026 • 10:32 — detail" (tanpa label WIB/ikon); `UiFormat.auditTimestamp` dan `TenantAuditFormat` disederhanakan.
  - **3.6 Sheet "Kelola penghuni"** (`TenantActionSheet.kt`): baris datar ala `.sheetrow` (ikon + label + pembatas tipis), "Penghuni keluar" ditandai warna peringatan; parameter `tenantName` dihapus.
  - **3.10 Perpanjang kos**: kartu tanggal memakai `KeyValueRow`; bagian "Pembayaran perpanjangan"; chip Lunas/Belum lunas memakai `SelectChip` berwarna status.
  - **4.1 Daftar lokasi** (`LocationScreens.kt`): chip "N terisi"/"N kamar" berwarna hijau/abu sesuai mock.
  - **4A Dashboard**: kartu stat memakai `SummaryCard` (ikon+label di atas, angka besar); chip persen bergaya container aksen; tombol aksi tonal + solid.
  - **4B Kamar**: aksi Edit menjadi ikon pensil; tombol Detail tonal ringkas.
  - **4C Pembayaran**: filter diganti `SegmentedPillCompact` (pil kecil, tidak selebar layar).
  - **4.16 Edit lokasi**: bidang "Deskripsi (opsional)" dihapus (tidak ada di mock); nilai deskripsi dikirim kosong.
  - **5.6 Kwitansi** (`ReceiptScreen.kt`): format struk penuh dipulihkan — baris No. kwitansi/Penghuni/Kamar/Kategori/Periode/Metode/Tanggal bayar → TOTAL DIBAYAR → cap LUNAS + "Dibuat …" sebaris → garis putus-putus → "Terima kasih telah membayar tepat waktu." di dalam kartu; footer luar kartu dihapus.
  - **7.3 Sheet sumber foto** (`media/PhotoSource.kt`): baris datar Galeri/Kamera dengan pembatas tipis (bukan `ListItem` bersubjudul).
- **Komponen baru**: `KeyValueRow` (`ui/common/FormLayout.kt`), `SelectChip` (`ui/common/StatusComponents.kt`), `SegmentedPillCompact` (`ui/common/ListComponents.kt`), `SummaryCard` memperoleh `iconColor` (`ui/common/EmptyStateComponents.kt`).
- Verifikasi: `./gradlew testDebugUnitTest assembleDebug` **BUILD SUCCESSFUL**; layar-layar di atas ditelusuri ulang di emulator (mode gelap & terang) — **tanpa crash** `com.miraya.kostku`.
- **Dokumentasi**: `docs/MOCK-COMPARISON.md` — ditambah bagian "Gelombang 3 — audit ulang frame-per-frame (2026-10-12)".

## Unreleased — verifikasi 8 frame "belum diverifikasi" hingga 100% ✅

### Changed
- **Delapan frame yang dahulu ❔ (belum diverifikasi) kini dicek di emulator dan diselaraskan** sampai identik mock; status di `docs/MOCK-COMPARISON.md` menjadi **50/50 ✅**:
  - **3.8 Form edit penghuni** (`TenantEditScreen.kt`): tata letak diubah dari kartu menjadi **datar** (bagian "DATA PENGHUNI" + "FOTO PENGHUNI" tanpa `FormCard`), selaras mock.
  - **4.10 Dialog konfirmasi hapus lokasi** (`ui/common/LocationDialogs.kt`): tombol **"Hapus" kini merah** — `ConfirmDialog`/`SaveDialogButton` menambah opsi `destructive`. Dialog hapus tipe kamar ikut memakai opsi yang sama.
  - **4.12 Dialog tambah tipe kamar** (`RoomTypeScreens.kt`): sudah cocok (judul + "Nama tipe kamar" + Batal/Simpan).
  - **6.3 Riwayat penghuni keluar** (`MoveOutHistoryScreen.kt`): subjudul baris disederhanakan menjadi "Kamar N • tanggal keluar • Rp" (hapus rentang masuk→keluar) dan baris "N penghuni keluar" dihapus (mock tidak menampilkannya).
  - **6.5 Tentang Aplikasi** (`InfoScreen.kt`): hero diganti **ikon pin** dalam kotak aksen (sebelumnya huruf "K" di kotak solid); deskripsi dipindah ke **kartu**; baris Syarat/Kebijakan/Bantuan tanpa subjudul (`SettingsSection` menerima `subtitle` opsional).
  - **Mode Terang (mock 8)** — Penghuni, Perpanjang kos, dan Kwitansi diverifikasi langsung di emulator (tema `light`, aksen Biru) dan cocok; setelah verifikasi tema dikembalikan ke Gelap.
- Verifikasi: `./gradlew testDebugUnitTest assembleDebug` **BUILD SUCCESSFUL**; seluruh layar di atas ditelusuri di emulator — **tanpa crash** `com.miraya.kostku`.
- **Dokumentasi**: `docs/MOCK-COMPARISON.md` — ringkasan angka menjadi **✅50 / ⚠️0 / ❔0**; ditambah bagian "Riwayat perubahan status" (gelombang 1 ⚠️ dan gelombang 2 ❔).

## Unreleased — penyelarasan 6 frame "sebagian" agar identik mock

### Changed
- **Enam frame yang dahulu hanya sebagian (⚠️) kini diselaraskan penuh dengan mock**; fungsi backend tetap utuh (data yang dahulu diisi lewat bidang yang dihapus kini memakai nilai bawaan aman):
  - **3.9 Pindah kamar** (`TenantMoveScreen.kt`): dropdown kamar tujuan diganti **daftar kartu kamar kosong** — kartu "Kamar saat ini" + kartu bertepi aksen "KAMAR TUJUAN" berisi baris kamar (avatar kunci bulat + centang untuk pilihan, baris lain diredupkan) + catatan "Hanya kamar kosong yang dapat dipilih."; label "Kamar N"; catatan putus-putus "Penghuni & sewa tetap; hanya kamar yang berpindah."; CTA "Pindahkan" diberi ikon swap.
  - **3.10 Perpanjang kos** (`TenantExtendScreen.kt`): diringkas persis mock — kartu "Tanggal keluar saat ini" + dropdown **Jumlah penghuni** & **Durasi perpanjangan** + kartu aksen "TANGGAL KELUAR BARU" ("Sisa N hari dari hari ini") + "Pembayaran perpanjangan" (Harga sewa baru, chip Lunas/Belum lunas). Diskon/jumlah dibayar/metode/kategori/tanggal yang tak ada di mock dihapus; metode `cash`, kategori "Sewa", status `paid`/`pending` dipakai diam-diam. CTA "Perpanjang" diberi ikon repeat.
  - **3.11 Pindah tanggal** (`TenantDatesScreen.kt`): tombol "Kosongkan tanggal keluar" dihapus; dua `DateField` berdiri sendiri + catatan putus-putus "Mengubah tanggal tidak menghitung ulang harga.".
  - **Detail kamar** (`RoomDetailScreen.kt`): hero memakai **ikon kunci** dalam kartu bertepi + `StatChip` status (bukan logo/foto); isi satu kartu kv "Penghuni / Nomor token listrik / Nomor PDAM" dengan "Tidak tersedia" bila kosong; CTA "Edit kamar" tanpa ikon.
  - **4.8 Tambah banyak kamar** (`BatchRoomScreen.kt`): dikunci ke satu mode — Tipe kamar* + Awalan nama + Jumlah (1–100)* + kartu "Akan dibuat"; CTA "Generate kamar" menempel di bar bawah (sticky).
  - **6.4 Tema** (`ThemeScreen.kt`): kartu **Pratinjau** dan baris radio dihapus; kini pil MODE (Terang/Gelap/Hitam) + grid 5 kolom AKSEN WARNA (swatch **bulat** + cincin aksen, centang saat terpilih).
- **Komponen**: `SaveButton` menerima `icon: ImageVector?` opsional; `TenantActionScaffold` meneruskan `ctaIcon`.
- Verifikasi: `./gradlew testDebugUnitTest assembleDebug` **BUILD SUCCESSFUL**; keenam layar ditelusuri di emulator (3.9, 3.10, 3.11, Detail kamar, 4.8, 6.4) dan dibandingkan dengan mock — **tanpa crash** `com.miraya.kostku`.
- **Dokumentasi**: `docs/MOCK-COMPARISON.md` diperbarui — keenam frame pindah dari ⚠️ ke ✅ (ringkasan: ✅42 / ⚠️0 / ❔8).

## Unreleased — audit layar 1-per-1 vs mock (Lokasi, Pembayaran, Pengaturan)

### Changed
- **Audit layar satu per satu terhadap mock** (`mock/kostku-mock.html`) dan penyelarasan detail tata letak/label berikut:
  - **Lokasi — Tipe kamar / Harga / Kamar (4.5, 4.7, 4.11, 4.14, 4.15)**
    - Harga: baris satuan durasi kini **huruf kecil** ("1 orang • 1 bulan"), bukan "Bulan"; aksi per baris memakai ikon pensil + tempat sampah (bukan tombol teks "Edit"/"Hapus").
    - Kelola tipe kamar (4.11): kartu memakai **avatar ikon** + "Kelola harga dan kamar" + tombol kecil "Edit" + ikon hapus merah (bukan baris ListItem).
    - Nomor kamar (4.14): kartu memakai avatar kunci berwarna sesuai ada/tidaknya utilitas + ikon **pensil** (bukan tombol "Edit").
    - Edit kamar (4.7): judul "Edit kamar", field "Nama kamar *"/"Tipe kamar *", bagian **STATUS** berupa chip pilihan [Tersedia/Terisi/Kotor/Maintenance], **UTILITAS (OPSIONAL)**, **FOTO KAMAR**; tombol **Hapus + Simpan** sejajar di bar bawah.
    - **Edit nomor kamar (4.15)** varian ringkas via parameter `compact` pada `RoomEditScreen`: hanya Nomor kamar + Tipe + Utilitas + satu tombol "Simpan" (dibuka dari daftar Nomor kamar). Sumber input dibedakan dengan flag `roomEditCompact` di `App.kt`.
    - Detail kamar (dok): satu kartu kv "Penghuni / Nomor token listrik / Nomor PDAM" + catatan "Nomor utilitas opsional. Data contoh, bukan nomor pelanggan asli."
  - **Lokasi — detail 3 tab (4A–4D)**: Pembayaran lokasi tanpa kartu ringkasan Pemasukan/Pengeluaran (sesuai mock 4C, hanya daftar + segmen).
  - **Pembayaran — daftar (5.1–5.3)**: sub-navigasi Semua/Pemasukan/Pengeluaran kini **segmented pill** (komponen baru `SegmentedPill`, mock `.seg`) menggantikan tab bergaris-bawah; kartu ringkasan "Pemasukan"/"Pengeluaran" **satu kartu penuh** di tiap sub-tab (mock 5.2/5.3) dengan angka besar; sub-tab tidak lagi menampilkan chip filter status/jenis.
  - **Pembayaran — dialog (5.4/5.5)**: Kategori + Metode berdampingan (2 kolom, mock "2 kolom rapi"); dialog dibuat lebih lebar (`usePlatformDefaultWidth=false`, 94% lebar) agar label tidak terpotong; chip status dibungkus `FlowRow` supaya tidak meluber.
  - **Pengaturan (6.2)**: pemilih tab Kategori memakai `SegmentedPill`; baris kategori memakai ikon pensil + tempat sampah (bukan tombol teks); baris petunjuk "Kategori dipakai di dialog…" dihapus (mock tidak menampilkannya).
- **Komponen baru**: `SegmentedPill(options, selected, onSelect)` dan `SelectableChip(label, selected, onClick)` (kartu pilihan tanpa centang untuk baris status kamar).
- Verifikasi: `./gradlew testDebugUnitTest assembleDebug` **BUILD SUCCESSFUL**; layar ditelusuri satu per satu di emulator (Lokasi → Kamar/Buka, Tipe kamar, Harga, Nomor kamar, Edit kamar (4.7) & Edit nomor kamar (4.15), Pengaturan lokasi, Pembayaran (pill + kartu stat + dialog pemasukan/pengeluaran), Kategori, Tema, Penghuni keluar, Tentang) — **tanpa crash** `com.miraya.kostku`.
- **Dokumentasi**: `docs/MOCK-COMPARISON.md` — peta perbandingan tiap frame mock vs aplikasi (✅ identik / ⚠️ sebagian / ❔ belum diverifikasi) beserta berkas implementasinya, untuk membuktikan kesesuaian 100%.

## Unreleased — penyelarasan tampilan dengan mock (100%)

### Changed
- **Seluruh layar diselaraskan agar identik dengan mock** (`mock/kostku-mock.html`), tanpa mengubah alur/backend:
  - **Masuk** — judul "Kostku", tagline "Kelola kos Anda lebih mudah", catatan "Dengan masuk, Anda menyetujui Ketentuan & Kebijakan Privasi." (2).
  - **Penghuni** — keadaan kosong "Tekan tombol + untuk menambah penghuni pertama." (3.2); detail Informasi kini menampilkan **Utilitas kamar + Utilitas lokasi** (3.3); tab Pembayaran: baris ringkas "Pemasukan • kategori • status • tanggal" + "Ketuk untuk melihat kwitansi" dan baris **bisa diketuk untuk membuka kwitansi** (3.4); sheet kelola "Tandai keluar & arsipkan" (3.6).
  - **Aksi penghuni** — Pindah kamar menampilkan aturan "Hanya kamar kosong yang dapat dipilih." dan "Penghuni & sewa tetap; hanya kamar yang berpindah." (3.9); Pindah tanggal menampilkan "Mengubah tanggal tidak menghitung ulang harga." (3.11).
  - **Lokasi** — daftar memakai chip "N terisi" / "N kamar" + tombol **Buka** (4.1); kartu Kamar menampilkan **nama penghuni** dan hanya Detail + Edit (4B); aksi lokasi (Pengaturan/Edit) pindah ke menu titik-tiga (dok); Pengaturan: "Nama, cabang, alamat, logo", "Tambah / ubah tipe kamar", "Tambah / ubah nomor kamar", "Hapus lokasi + seluruh kamar", bagian "Utilitas lokasi" dihapus dari sini karena sudah tampil di detail penghuni (4D); **Tambah lokasi** kini layar penuh dengan **logo + utilitas** (4.9); dialog hapus "Lokasi "<nama>" beserta kamar di dalamnya akan dihapus…" + tombol "Hapus" (4.10).
  - **Tipe kamar / Harga / Kamar** — "Kelola tipe kamar" tanpa garis miring; Harga judul "Harga — <tipe>", dialog "Jumlah orang*", "Satuan*", "Harga*", tombol "Simpan harga" (4.5/4.6); Edit kamar: judul "Edit kamar", urutan status Tersedia/Terisi/Kotor/Maintenance (4.7/4.15); Tambah banyak kamar: "Tipe kamar*", "Awalan nama", "Jumlah (1–100)*", "Akan dibuat:" (4.8); Nomor kamar: tanpa pencarian (4.14).
  - **Pembayaran** — dialog "Catat pemasukan" dengan chip status **Lunas/Sebagian/Belum**, label "Kategori", "Metode" (5.4); dialog "Catat pengeluaran" "Jenis pengeluaran*", "Jumlah (Rp)*", "Lokasi" (5.5); baris memakai **metode** ("Tunai") bukan jenis (5.1/5.3); filter pemasukan "Belum"; status `pending` → label "Belum".
  - **Kwitansi** — header **No. kwitansi "KW-YYYY-NNNN"** (turunan dari id, tanpa endpoint baru), baris Kategori/Periode/Metode/**Tanggal bayar**, penutup "Dibuat <tanggal>" + "Terima kasih telah membayar tepat waktu." (5.6) — juga pada PDF, dan tombol Bagikan teks diperbarui.
  - **Pengaturan** — Tentang Aplikasi kini menampilkan kartu aplikasi (Kostku / Versi 1.0) + baris **Syarat & Ketentuan**, **Kebijakan Privasi** (layar sebelumnya tak terjangkau kini terhubung), **Bantuan** (6.5); Riwayat penghuni keluar: judul "Penghuni keluar", bulan panjang "Oktober 2026", badge "Keluar" (6.3).
  - **Komponen** — sheet sumber foto "Pilih sumber foto / Galeri / Kamera" (7.3); layar pemuatan menampilkan "Memuat data…" (7.4).
- Data: `RemotePayment` menambah `paidAt`/`createdAt` (diambil dari snapshot; opsional).
- Verifikasi: unit test (`UiFormatTest` diperbarui untuk label "Belum") + `assembleDebug` **BUILD SUCCESSFUL**; aplikasi dipasang & ditelusuri di emulator (Masuk, Penghuni, detail + 3 tab, Kwitansi dari riwayat, aksi pindah/perpanjang/tanggal, Lokasi, Kamar, Pengaturan, Nomor kamar, dialog pemasukan/pengeluaran, Tentang) **tanpa crash** `com.miraya.kostku`.

## Unreleased — pemecahan berkas sumber per fitur

### Changed
- **Berkas `.kt` dipecah menjadi banyak berkas kecil sesuai fitur** (tanpa mengubah perilaku). Berkas besar yang semula menumpuk kini dipisah, dan jumlah berkas naik dari 40 → 86. Rincian pemecahan:
  - `features/location/LocationScreens.kt` → `LocationScreens` (tab daftar + logo), `LocationDetailScreen`, `LocationDashboardTab`, `LocationRoomsTab`, `LocationPaymentsTab`.
  - `features/location/LocationSettingsScreens.kt` → `LocationSettingsScreen`, `RoomDetailScreen`, `RoomNumberScreen`, `LocationEditScreen`.
  - `features/payment/PaymentScreens.kt` → `PaymentsScreen`, `PaymentLedgerTab`, `PaymentIncomeTab`, `PaymentExpenseTab`, `PaymentRowMenu`, `PaymentDialog`, `ExpenseDialog`.
  - `features/tenant/TenantActionScreens.kt` → `TenantActionSheet`, `TenantActionScaffold`, `TenantEditScreen`, `TenantExtendScreen`, `TenantMoveScreen`, `TenantDatesScreen`, `TenantExitScreen`, `TenantActionPage`.
  - `features/tenant/TenantScreens.kt` → `TenantListScreen`, `TenantFilterSheet`, `AddTenantScreen`.
  - `NetworkApi.kt` → `ApiModels`, `ApiSession`, `ApiHttp`, `KostkuApi` (+ mesin HTTP) dan grup metode ekstensi per fitur: `ApiAuth`, `ApiSnapshot`, `ApiProperty`, `ApiTenant`, `ApiPayment`, `ApiMedia`.
  - `media/PhotoField.kt` → `PhotoField`, `PhotoFrame` (`StoredPhotoState`+`PhotoFramePreview`), `LogoField`.
  - `media/ImageViewer.kt` → `ImageViewer` (zoom layar penuh) + `PhotoThumbnails` (`ClickableRemotePhoto`, `ListPhotoThumb`).
  - `ui/common/AppDialogs.kt` → `LoginScreen`, `MainDialogs` (penyalur) + `LocationDialogs`, `PaymentDialogs`, `ExpenseDialogs`, `LocationFormDialog`.
  - `ui/common/CommonComponents.kt` → `EmptyStateComponents`, `SaveButton`, `DialogButtons`, `FormLayout`.
  - `TenantDetailScreen.kt` → `TenantDetailScreen`, `TenantDetailTabs`, `TenantAuditFormat`.
  - `features/receipt/ReceiptScreen.kt` → `ReceiptScreen`, `ReceiptShare`, `ReceiptPdf`.
  - `features/roomtype/RoomTypeScreens.kt` → `RoomTypeScreens` (tipe kamar) + `RoomTypePriceScreen` (harga).
  - Grup metode API kini berupa fungsi ekstensi `KostkuApi.*`, sehingga pemanggil merapikan impor per metode yang dipakai.
- Verifikasi: unit test + `assembleDebug` **BUILD SUCCESSFUL**; aplikasi dipasang & diuji di emulator (Penghuni, Detail penghuni, Lokasi, Detail lokasi 3 tab, Pengaturan lokasi, Nomor kamar, Detail kamar, Pembayaran, Kwitansi, tombol kembali) — **tanpa crash** `com.miraya.kostku`.

## Unreleased — perbaikan navigasi: state layar & tombol kembali

### Fixed
- **Kembali dari halaman dalam kini mengembalikan tampilan asal, bukan reset ke tab pertama.** Sebelumnya tab/filter/pencarian disimpan dengan `remember` di dalam layar, sedangkan layar induk keluar dari komposisi saat halaman yang lebih dalam dibuka — sehingga `remember`-nya hilang dan kembali selalu melompat ke tab pertama (mis. buka **Detail kamar** lalu *Kembali* selalu mendarat di tab **Dashboard**, bukan **Kamar**). Kini:
  - **Tab utama** (Penghuni/Lokasi/Pembayaran) menyimpan sub-tab/pencarian/filter sebagai state di `App` (`rememberSaveable`).
  - **Layar dalam** (detail lokasi/kamar/penghuni, pengaturan, nomor kamar, dsb.) dibungkus `SaveableStateHolder` yang di-key per **id entitas**, sehingga sub-tab/pencarian/filter bertahan saat kembali — dan **tidak bocor antar-data** (tab "Kamar" lokasi A tidak terbawa ke lokasi B; tab detail penghuni tidak terbawa ke penghuni lain).
- **Tombol **kembali** fisik kini berfungsi di semua halaman dalam.** Ditambahkan `BackHandler` yang memetakan layar dalam → aksi kembali yang setara ikon **Kembali** (menutup detail/pengaturan/kwitansi/editor). Di tab utama, back tetap keluar dari aplikasi seperti biasa.

## Unreleased — utilitas kos, logo lokasi, kwitansi, rapikan detail lokasi

### Added
- **Detail lokasi: 3 tab + halaman Pengaturan.** Tab `Dashboard`/`Kamar`/`Pembayaran` dipoles (okupansi + bar progres, pemasukan/pengeluaran bulan ini, jatuh tempo terdekat, pintasan "Perlu tindakan"). Daftar **Kamar** kini hanya menawarkan **Detail** dan **Edit**; pengaturan **tipe kamar**, **nomor kamar** (tambah/ubah + utilitas), edit info+logo, dan **hapus lokasi** dipindah ke halaman **Pengaturan** lokasi (ikon gerigi pada app bar).
- **Layar Detail kamar** baru: identitas kamar, penghuni aktif, dan **utilitas kamar** + tombol ke Edit kamar.
- **Utilitas opsional** — **nomor token listrik** & **nomor PDAM** — per **kamar** dan per **lokasi kos**. Diisi dari *Edit lokasi* / *Edit nomor kamar*. Di **detail penghuni** hanya ditampilkan **Utilitas kamar** (utilitas lokasi ada di **Pengaturan** lokasi itu sendiri). Nilai kosong ditulis **"Tidak tersedia"**. Tidak ada pewarisan dari lokasi ke kamar.
- **Logo lokasi** (opsional) yang tampil di **kwitansi**; bila kosong memakai **ikon rumah**. Dipasang lewat *Edit lokasi* (Galeri/Kamera, tanpa pemotongan).
- **Kwitansi** yang diturunkan dari satu catatan pembayaran (tanpa endpoint/dokumen baru): header logo lokasi, identitas, penghuni/kamar/periode/metode, total, dan cap status. Tombol **Bagikan** (teks) dan **PDF** (dibuat di perangkat lewat `PdfDocument` bawaan Android, dibagikan via `FileProvider`) — tanpa pustaka tambahan. Dapat dibuka dari tab **Pembayaran** (Semua & Pemasukan) melalui menu **⋮ → Kwitansi**.
- **Chip sisa hari** di daftar penghuni (hijau ≥14 hari, kuning 7–13, merah <7 atau sudah lewat, abu-abu tanpa tanggal keluar), menggantikan label status keanggotaan "Aktif/Sudah keluar" yang lama.
- **Backend:** migrasi `007_utilities_logo.sql` (kolom utilitas pada `properties` & `rooms`, dan entitas media `property` untuk logo) + `docs/IMPLEMENTATION-PLAN.md`.

### Changed
- Daftar penghuni tidak lagi menampilkan "Sisa N hari" di subjudul (kini menjadi chip) dan subjudul hanya memuat HP • Kamar/Tipe.
- Endpoint `PATCH /properties/{id}` dan `PATCH /rooms/{id}` menerima `token_listrik`/`pdam_number`; `POST /properties`, `POST /properties/{id}/rooms` juga menerimanya. `DELETE /properties/{id}` ikut menghapus media logo.
- Snapshot `GET /snapshot` kini menyertakan `logo_url` untuk tiap properti.

### Notes
- Cap kwitansi **LUNAS** hanya muncul bila status pembayaran `paid`; selain itu **SEBAGIAN**/**BELUM LUNAS**/**DIBATALKAN**.
- Deploy backend produksi **selesai 2026-10-11**: migrasi `007` diterapkan ke DB produksi (MariaDB 11.4), file `config/bootstrap.php` + `public/index.php` + `database/schema.sql` + migrasi diunggah (md5 server == lokal), dan uji end-to-end **20/20 PASS** — termasuk utilitas lokasi/kamar, `POST /media/property/{id}` + penyajian file, serta `logo_url` di `/snapshot`. Properti lama (wasd, Miraya Kos) tidak tersentuh. Lihat `docs/IMPLEMENTATION-PLAN.md`.

## Unreleased — hemat kuota: satu endpoint snapshot + ETag/304 + gzip

### Changed
- **Data dimuat dalam SATU permintaan, bukan ~10+.** Sebelumnya `snapshot()` memanggil `GET /properties` lalu fan-out tiap properti → tipe kamar → harga → kamar, ditambah penghuni, pembayaran, pengeluaran, dan kategori (belasan permintaan tiap buka/segarkan). Kini klien memanggil **`GET /snapshot`** sekali yang mengembalikan seluruh data (`properties`, `room_types`, `room_type_prices`, `rooms`, `tenants`, `payments`, `expenses`, `categories`) — jauh lebih hemat, terutama untuk panggilan ulang setelah setiap penyimpanan.
- **Validasi lewat `ETag`/`If-None-Match` → `304 Not Modified`.** Server mengirim `ETag` (hash isi); klien menyimpan isi snapshot + ETag secara lokal dan di permintaan berikutnya mengirim `If-None-Match`. Bila data tidak berubah, server menjawab **304 tanpa isi** — pemuatan ulang nyaris nol kuota. Setiap penyimpanan yang sukses mengubah data, jadi unduhan berikutnya kembali `200` dengan ETag baru. Data tidak pernah basi tetapi tetap "fully sinkron".
- **Snapshot tampil dari cache lebih dulu (instan & tahan offline).** Profil dan snapshot terakhir ditampilkan langsung dari cache lokal saat aplikasi dibuka — tanpa menunggu jaringan — lalu divalidasi ke server di latar belakang. Saat offline, aplikasi tetap menampilkan data terakhir alih-alih layar kosong atau paksa-logout; hanya 401 yang mengakhiri sesi.
- **gzip di server.** Respons JSON dikompresi (`ob_gzhandler`) saat klien mengirim `Accept-Encoding: gzip`, memangkas isi snapshot sekitar **70–80%** (contoh nyata: 9.149 → 1.899 byte). `ETag`/`Accept-Encoding` diekspos lewat CORS.
- **Kompatibilitas.** Bila backend belum diperbarui (tanpa `/snapshot`), klien otomatis kembali ke jalur lama sehingga aplikasi tetap berfungsi.

## Unreleased — hemat kuota: cache gambar (Coil)

### Changed
- **Foto kini di-cache (memori + disk), bukan diunduh ulang tiap render.** Sebelumnya setiap tampilan foto memakai `URL.openConnection()` mentah tanpa cache HTTP, jadi foto yang sama diunduh berulang setiap layar digambar ulang — pemborosan kuota terbesar, terutama di jaringan seluler. Kini seluruh pemuatan foto jarak jauh lewat **Coil 3** dengan satu `ImageLoader` aplikasi (`KostkuApp`) yang menyediakan **cache memori (20% RAM)** + **cache disk 128 MB** (`cache/image_cache`) dan memakai klien **OkHttp bersama**. Hasilnya setiap foto **hanya diunduh sekali** lalu dilayani dari cache; unduhan berikutnya tidak lagi memakai kuota. Klien OkHttp bersama juga menghormati header `Cache-Control`/`ETag` dari media CDN (cache 1 bulan).
- **Tiga pemuat gambar bersama ditulis ulang ke Coil:** `RemotePhotoPreview` (pratinjau bundar), `RemoteViewerImage` (penampil layar penuh, `SubcomposeAsyncImage` dengan status memuat/gagal), dan `PhotoField` (bingkai edit foto tersimpan; status `StoredPhotoState` kini diturunkan dari `AsyncImagePainter.State` lewat `collectAsState()`). Tidak ada perubahan pada alur unggah/edit foto maupun bentuk tampilan (foto tetap bundar di luar pratinjau penuh).

## Unreleased — perapian tampilan: satu sistem desain Material 3

### Changed
- **Ruang atas berlebih diperbaiki (inset ganda).** Scaffold tab utama dulu menerapkan inset status bar lalu `AppTopBar` tiap tab menambahkannya lagi, sehingga judul semua tab tampak terlalu turun dengan celah besar di bawah status bar. Kini `AppTopBar` yang menangani inset atas dan Scaffold tab hanya menyisakan inset navigasi bawah (`contentWindowInsets = WindowInsets(0,0,0,0)`) — judul kembali rapi di ketinggian app bar normal.
- **Semua foto tampil bundar (lingkaran) kecuali pratinjau layar penuh.** Komponen bersama `ClickableRemotePhoto`/`ListPhotoThumb` kini meng-clip gambar dengan `CircleShape` + `ContentScale.Crop` secara default, sehingga avatar **penghuni** (daftar & detail), **daftar kamar**, **riwayat penghuni keluar**, dan baris **bukti pembayaran/pengeluaran** seragam berbentuk lingkaran. Hanya bingkai edit `PhotoField` dan pratinjau layar penuh yang tetap bukan lingkaran (dengan parameter `shape` untuk kustomisasi bila perlu).
- **Satu sistem desain.** Token jarak baru (`ui/theme/Dimens.kt`: `Spacing`, `Screen`, `Dimens`, `FieldShape`) menjadi sumber tunggal untuk inset, jarak antar blok, clearance FAB, dan tinggi tombol — menggantikan puluhan nilai `.dp` lepas yang sebelumnya bercampur 16/20/14/96/24. Padding tepi layar kini seragam 16dp di semua halaman.
- **App bar seragam.** Komponen baru `AppTopBar` dipakai **semua** layar (4 tab utama + seluruh halaman dalam): tinggi, warna permukaan, judul satu baris, dan ikon kembali yang sama. Tab utama kini ber-judul di app bar; judul besar di dalam body dan **judul ganda** di layar Tentang/Bantuan/Privasi dihapus.
- **Bidang isian seragam.** Komponen baru `AppTextField` menggantikan semua `OutlinedTextField` lepas: sudut 14dp (`FieldShape`), label, jenis papan ketik (angka untuk nominal/diskon/jumlah), dan `prefix` "Rp " yang konsisten. `ChoiceDropdown`/`DateField` ikut memakai bentuk yang sama; komponen mati `SearchField` dihapus.
- **Tombol seragam.** `SaveDialogButton` kini ber-`shapes.large`; tinggi tombol aksi utama disamakan (`Dimens.ButtonHeight` = 52dp); baris tombol Hapus/Simpan di Edit kamar seragam; label "GENERATE KAMAR" → "Generate kamar".
- **Kartu, daftar, tab, sheet, dialog, login.** Padding dalam kartu seragam 16dp, jarak item daftar 8dp, clearance FAB memakai token; strip tab (`AppTabRow`) ber-inset 16dp di semua layar; sheet/dialog memakai token yang sama; layar Masuk diselaraskan; bobot judul (`headlineSmall`/`titleLarge`/`titleMedium`) dibuat SemiBold agar lebih tegas.

## Unreleased — riwayat penghuni keluar (per bulan)

### Added
- **Tabel `tenant_move_out_history`.** Setiap penghuni yang ditandai **keluar** kini dicatat sebagai snapshot (nama, HP, kamar, tanggal masuk/keluar, sewa, catatan) di dalam transaksi `POST /tenants/{id}/move-out` — riwayat tetap ada walau penghuni kemudian diarsipkan atau datanya berubah. Migrasi `006_tenant_move_out_history.sql` (idempoten) juga **mengisi riwayat dari penghuni yang sudah `moved_out`** agar langsung ada isi.
- **Endpoint `GET /tenants/move-out-history?month=YYYY-MM`** mengembalikan penghuni keluar pada bulan tersebut (berdasarkan `move_out_date`), terbaru lebih dulu, dengan `photo_url` bila masih ada foto.
- **Layar `Pengaturan → Riwayat → Penghuni keluar`** menampilkan daftar per bulan dengan **navigasi bulan (‹ bulan ›)**; tombol bulan berikutnya nonaktif di bulan berjalan. Tiap baris: foto (ketuk untuk pratinjau layar penuh), nama, kamar, rentang tanggal, sewa, dan badge **Sudah keluar**.

### Changed
- **`POST /tenants/{id}/move-out` hanya untuk penghuni `active`.** Menandai keluar ulang penghuni yang sudah `moved_out`/`archived` kini menjawab **409** (mencegah baris riwayat ganda).

## Unreleased — perbaikan tema & aksen (kontras, lebih banyak aksen, pratinjau)

### Fixed
- **Kontras aksen diperbaiki.** Sebelumnya satu warna aksen dipakai mentah untuk `primary` di **semua** mode, dan `onPrimary`/container tidak diatur — sehingga pada mode gelap/hitam aksen bernada gelap menghasilkan teks aksen gelap di latar gelap yang nyaris tak terbaca. Kini setiap aksen punya **nada per-mode** (lebih gelap di mode terang, lebih terang di mode gelap/hitam) dan seluruh peran M3 (`onPrimary`, `primaryContainer`/`secondaryContainer`, `onPrimaryContainer`) diturunkan dari aksen, jadi teks di atas aksen selalu kontras (teks putih di tombol terang-mode, teks hampir-hitam di tombol gelap-mode).

### Added
- **10 pilihan aksen.** Dari 4 (Teal, Blue, Purple, Orange) menjadi **10**: Teal, Biru, Indigo, Ungu, Merah Muda, Merah, Oranye, Kuning, Hijau, Cyan. Semua berasal dari **satu sumber** (`ui/theme/Accents.kt`, `AccentList`) sehingga tidak ada daftar nama yang terduplikasi.
- **Pratinjau tema langsung.** Layar **Tema** kini menampilkan pratinjau yang merender komponen nyata dari skema tema: judul bagian, tombol primer & tonal (beserta teks di atasnya), badge status, dan teks biasa — jadi kontras tiap aksen langsung terlihat. Grid aksen menampilkan swatch sesuai nada mode aktif dengan ring penanda pilihan; centang memakai warna teks yang otomatis kontras.

### Changed
- **Sumber warna disatukan.** `ThemeScreen` tidak lagi menduplikasi perhitungan warna; pratinjau dan pemilih aksen memakai `kostkuColorScheme(mode, accent)` yang sama dengan tema aplikasi. Ringkasan di pengaturan menampilkan label aksen (mis. "Aksen Biru"), dan nilai `accent` lama tetap kompatibel (id stabil).

## Unreleased — pratinjau foto layar penuh (zoom & geser)

### Added
- **Ketuk foto → pratinjau layar penuh.** Komponen baru `ImagePreviewDialog` menampilkan foto di atas latar hitam, pas layar, dengan **cubit/ketuk-dua-kali untuk zoom (1×–6×)**, **geser untuk pan** (dibatasi agar foto tidak hilang dari layar), dan tombol **`Tutup` (✕)** + tombol kembali untuk menutup. Dipasang di semua foto yang ditampilkan: avatar penghuni (daftar & detail), thumbnail kamar di daftar kamar, dan thumbnail bukti **pembayaran/pengeluaran** di tab **Semua**, **Pemasukan**, dan **Pengeluaran** (baris tanpa foto tetap memakai ikon). Lihat `ClickableRemotePhoto` dan `ListPhotoThumb`.
- **Bukti pembayaran & pengeluaran kini tampil.** Sebelumnya foto bukti hanya bisa diunggah tetapi tidak pernah ditampilkan; sekarang muncul sebagai thumbnail di baris daftar dan dapat diketuk untuk dipratinjau.

### Changed
- **Layar edit foto (`PhotoField`).** Ketuk pada foto kini membuka **pratinjau layar penuh**; aksi **Ganti foto**/**Hapus foto** pindah ke tombol **`. . .`** di sudut kanan-atas bingkai foto. Tombol `. . .` hanya muncul bila sudah ada foto.

## Unreleased — indikator loading & error server saat menyimpan

### Added
- **Tombol Simpan menampilkan progres.** Komponen bersama **`SaveButton`** (form lebar penuh) dan **`SaveDialogButton`** (dialog) menukar labelnya menjadi **spinner + "Menyimpan…"** dengan transisi animasi (`AnimatedContent` fade/scale) dan menonaktifkan tombol selama permintaan berjalan; tombol lebar penuh juga menampilkan **progress bar indeterminate** di tepi bawah tombol. Dipakai di form **Tambah penghuni**, **Perpanjang kos**, **Edit kamar** (tombol Hapus ikut nonaktif saat menyimpan), dialog **lokasi**, **Kategori**, **Tipe kamar/Harga**, serta semua dialog **Pembayaran**/**Pengeluaran** — jadi setiap simpan jelas sedang berjalan.
- **Error server tampil menetap.** Mutasi yang gagal memunculkan **banner merah** di bagian atas (ikon + pesan Bahasa Indonesia + tombol **`Tutup`**) yang bertahan sampai ditutup atau tergantikan aksi sukses berikutnya; pesan yang sama juga muncul sebagai snackbar. Nilai form tetap dipertahankan saat gagal.

### Notes
- Pesan konflik server (HTTP 409), mis. saat nama kamar duplikat, tampil sebagai "Data bertentangan dengan data yang sudah ada. Permintaan ke server gagal. Silakan coba lagi." Kode HTTP, SQL, nama exception, token, dan detail backend tetap tidak pernah ditampilkan.

## Unreleased — hapus entitas selalu membersihkan fotonya

### Fixed
- **Hapus data kini menghapus fotonya juga.** Sebelumnya `DELETE /payments/{id}`, `DELETE /expenses/{id}`, `DELETE /rooms/{id}`, dan arsip penghuni (`DELETE /tenants/{id}`) hanya menghapus sebagian baris `media` dan **meninggalkan file foto di disk** (pembayaran juga meninggalkan baris `media`). Kini semuanya memanggil `mediaPurge(type, id, ownerId)` yang menghapus **file fisik + baris metadata**; `mediaDelete` (hapus foto tanpa hapus entitas) juga diperkuat dengan guard `is_file()`.
- Catatan: file media disajikan lewat CDN dengan cache 1 bulan, jadi URL file yang baru dihapus bisa sesaat masih menjawab 200 (HIT cache) walau file di origin sudah hilang.

### Changed
- `DELETE /payments/{id}` menyertakan penghapusan file fisik melalui helper `mediaPurge`, bukan sekadar baris media.

## Unreleased — aksi baris Pembayaran lewat menu titik-tiga (Edit / Hapus)

### Changed
- **Baris Pembayaran & Pengeluaran memakai menu titik-tiga.** Tombol **Edit**/**Batalkan** (dan **Hapus** pada pengeluaran) diganti satu tombol overflow (**⋮**) yang membuka pop-up berisi **Edit** dan **Hapus** (Hapus berwarna merah). Berlaku di tab **Semua**, **Pemasukan**, dan **Pengeluaran**.
- **"Batalkan" diganti "Hapus".** Pembayaran kini dihapus permanen (dengan konfirmasi), bukan ditandai `cancelled`.

### Added
- **`DELETE /payments/{id}`** pada backend: menghapus baris pembayaran beserta metadata medianya secara transaksional.
- `NetworkApi.deletePayment(id)`; dialog konfirmasi **"Hapus pembayaran?"** (tombol `Hapus`).

### Notes
- Kolom `payments.status` tetap mendukung `cancelled` untuk data lama, tetapi UI tidak lagi menawarkan pembatalan.

## Unreleased — pemasukan lengkap saat tambah penghuni & perpanjang

### Added
- **Perpanjang kos kini mencatat pemasukan**: layar **Perpanjang kos** punya bagian **Pembayaran perpanjangan** (diskon, Lunas/Belum lunas & jumlah bayar, **metode**, **kategori pemasukan**). Menyimpan memperbarui `move_out_date` + `monthly_rent` penghuni **dan** menulis satu baris `payments` secara transaksional lewat endpoint baru `POST /tenants/{id}/extend`, sehingga setiap perpanjangan langsung masuk ke tab **Pemasukan** dan buku besar **Semua**. Aksi ini juga tercatat di **Log Perubahan Data** sebagai **"Diperpanjang"**.
- **Metode & kategori pada tambah penghuni**: form **Tambah penghuni** kini punya dropdown **Metode pembayaran** (Tunai/Transfer/Lainnya) dan **Kategori** (kategori pemasukan dari Pengaturan).
- **Audit action `extended`**: `tenant_audit_log` mencatat aksi perpanjang; UI menampilkan label, ikon, dan ringkasan (tanggal keluar baru + nominal).

### Changed
- **Tambah penghuni selalu menulis baris pembayaran**: sebelumnya baris hanya dibuat bila dibayar > 0. Sekarang penempatan **selalu** membuat satu baris `payments` — nominal penuh saat Lunas; nominal sebagian/nol (status `partial`/`pending`) bila belum lunas — lengkap dengan `method` dan `category`. Penempatan, pembayaran awal, dan okupansi kamar tetap transaksional.
- `PATCH /tenants/{id}` **tidak** menulis pembayaran (hanya `POST /tenants/{id}/extend` yang melakukannya).

### Notes
- Tidak ada migrasi database baru: kolom `payments.{method,status,category}` dan `tenant_audit_log.action` sudah mencukupi.

## Unreleased — kategori pemasukan & pengeluaran bisa diatur sendiri

### Added
- **Pengaturan → Kategori**: pemilik bisa membuat, mengganti nama, dan menghapus **kategori pemasukan** dan **kategori pengeluaran** sendiri (global per akun, berlaku di semua lokasi). Layar bertab Pemasukan/Pengeluaran, tiap kategori bisa Edit/Hapus (hapus dengan konfirmasi), dan FAB **+** membuka dialog nama kategori.
- **Kategori dipakai di dialog**: dropdown **Kategori** pada *Catat/Edit pengeluaran* kini menampilkan kategori dari Pengaturan (bukan daftar tetap), dan *Catat/Edit pembayaran* kini punya dropdown **Kategori (opsional)** dari kategori pemasukan. Kategori juga tampil di baris Pemasukan dan di buku besar **Semua**.
- **Backend**: tabel `categories` (migrasi idempoten `005_categories.sql`), kolom `payments.category`, dan endpoint `GET/POST /categories`, `PATCH/DELETE /categories/{id}`. Server mengisi daftar default saat pemilik belum punya kategori sama sekali (pemasukan: Sewa, Deposit, Denda, Lainnya; pengeluaran: Listrik/Air, Perbaikan, Kebersihan, Gaji, Internet, Perabot, Lainnya). `snapshot()` Android juga memuat `categories` (toleran bila endpoint belum ter-deploy).

### Notes
- Catatan menyimpan **nama** kategori, jadi mengganti nama/menghapus kategori **tidak** mengubah pemasukan/pengeluaran yang sudah tercatat (trade-off yang disengaja).

## Unreleased — tab Pembayaran (Semua/Pemasukan/Pengeluaran) & fitur pengeluaran

### Added
- **Layar Pembayaran kini bertab: `Semua`, `Pemasukan`, `Pengeluaran`** (sebelumnya hanya daftar pemasukan dengan chip filter status).
  - **Semua** — buku besar gabungan pemasukan + pengeluaran (terbaru lebih dulu) dengan kartu ringkasan **Pemasukan** dan **Pengeluaran**. Tiap baris bisa di-Edit; pembayaran juga bisa di-**Batalkan**.
  - **Pemasukan** — daftar pembayaran penghuni dengan chip filter status (Semua/Lunas/Menunggu/Sebagian/Terlambat/Dibatalkan) + total pemasukan.
  - **Pengeluaran** — daftar pengeluaran dengan filter jenis (Semua/Operasional/Refund) + total; tiap baris bisa di-Edit/**Hapus** (dengan konfirmasi).
- **Pengeluaran (outflow) baru**, mendukung **biaya operasional** (kategori: Listrik/Air, Perbaikan, Kebersihan, Gaji, Internet, Perabot, Lainnya) **dan refund/pengembalian ke penghuni** (terhubung ke penghuni). Dialog "Catat pengeluaran" punya jenis, kategori/penghuni, tanggal, jumlah, metode, lokasi opsional, catatan, dan bukti foto opsional. FAB pop-up di layar Pembayaran (menu → Pengeluaran) membuka dialog ini.
- **Backend:** tabel `expenses` (skema + migrasi idempoten `003_expenses.sql`), endpoint `GET/POST /expenses` dan `GET/PATCH/DELETE /expenses/{id}` (ownership-scoped), serta bukti foto via `media` (`entity_type='expense'`, migrasi `004_media_expense.sql`). `snapshot()` Android kini juga memuat `expenses` (tanpa menggagalkan snapshot bila endpoint belum ter-deploy).

### Changed
- **Tombol tambah di Pembayaran jadi FAB pop-up (bukan bottom sheet).** Di layar **Pembayaran** ada satu FAB **+**; menekannya memunculkan menu mengambang (bukan bottom sheet) berisi **Pemasukan** dan **Pengeluaran**, yang masing-masing membuka dialog *Catat pembayaran* / *Catat pengeluaran*. FAB berubah jadi **X** saat menu terbuka, dan menu juga menutup bila area lain diketuk. Tombol **"Catat"/"Catat pengeluaran"** di header dihapus supaya tidak ganda.
- **Tombol tambah ganda dihilangkan.** Empty-state kini hanya mengandalkan satu tombol (FAB), konsisten dengan tab Penghuni: tab **Pengeluaran** dan **Pemasukan** tidak lagi menampilkan tombol "Tambah sekarang", dan **Lokasi** tidak lagi menampilkan tombol "Tambah sekarang" (cukup FAB **+**).

## Unreleased — urutan wajib pembuatan (lokasi → tipe → harga → kamar) & gating penghuni

### Added
- **Urutan pembuatan ditegakkan:** lokasi → tipe kamar → **harga** → **nomor kamar**. Form "Tambah Banyak Kamar" kini **hanya menampilkan tipe kamar yang sudah punya harga**; bila belum ada, muncul petunjuk "Belum ada tipe kamar yang punya harga di lokasi ini. Buat tipe kamar lalu tambahkan harganya dulu (alur: tipe kamar → harga → nomor kamar)." dan pemilih tipe dinonaktifkan. Backend menolak `POST .../rooms` dan `.../rooms/batch` untuk tipe kamar tanpa harga (409) selama `STRICT_ROOM_PRICING=1` (default; setel `0` di environment untuk melonggarkan pada data lama).
- **Tambah penghuni dibatasi sampai rantai lengkap.** `Penghuni` menampilkan empty-state pengarah ("Lengkapi dulu: buat lokasi, tipe kamar, harga, lalu nomor kamar…") dan tidak membuka form tambah selagi belum ada **tipe kamar yang lengkap**. Pada dropdown "Tipe kamar" form penempatan, **hanya tipe yang sudah punya harga DAN sudah punya nomor kamar** yang muncul; tipe belum lengkap (belum ada harga, atau sudah ada harga tapi belum ada nomor) **disembunyikan**, dengan pesan "Belum ada tipe kamar yang lengkap di lokasi ini…" bila lokasi belum punya tipe lengkap.

### Changed
- `NetworkApi.snapshot()` kini juga memuat **harga tipe kamar** (`RemoteSnapshot.prices`), sehingga layar harga dan gating tipe-berharga memakai data snapshot yang sama tanpa permintaan tambahan; state `prices`/`dataVersion` khusus di `App.kt` dihapus.

### Fixed
- Chip mati **"Kelola tipe kamar"** pada tab Lokasi (memanggil rute `"roomtype"` tanpa handler) dihapus; kelola tipe kamar tetap lewat detail lokasi → **Aksi lokasi → Atur Tipe Kamar**.

## Unreleased — konfirmasi sebelum hapus di semua tempat

### Changed
- **Semua aksi hapus kini wajib lewat dialog konfirmasi.** Sebelumnya **tipe kamar**, **harga tipe kamar**, dan **kamar** terhapus langsung begitu tombol "Hapus" ditekan. Kini ketiganya menampilkan `ConfirmDialog` (tombol **Konfirmasi**/**Batal**) dengan judul dan pesan yang menyebut nama/harga objeknya, mis. *"Hapus kamar? Kamar \"Kamar 1\" akan dihapus permanen…"*. Lokasi, pembatalan pembayaran, penghuni keluar, dan arsip penghuni memang sudah berkonfirmasi. Terverifikasi di emulator: dialog muncul untuk hapus kamar, tipe kamar, dan harga; menekan **Batal** tidak menghapus apa pun.

## Unreleased — halaman langsung menyegar setelah simpan/hapus

### Fixed
- **Data baru tidak muncul sampai halaman dibuka ulang ("harus buka tutup").** Daftar **harga tipe kamar** tak ikut menyegar setelah tambah/ubah/hapus. Mulanya diperbaiki dengan penghitung `dataVersion` di `App.kt`; sejak perubahan snapshot berikutnya, daftar harga dibaca langsung dari `snapshot.prices` yang dimuat ulang setiap `reload()`, sehingga **setiap simpan/hapus** memperbarui tipe kamar, harga, dan nomor kamar **seketika**. Terverifikasi di emulator: tambah harga, tambah kamar, dan hapus kamar semuanya tampil/hilang tanpa buka tutup.

## Unreleased — notifikasi Toast di semua halaman

### Fixed
- **Notifikasi/snackbar selalu "menggantung" lalu muncul di halaman depan.** Sebabnya `notify()` memakai `SnackbarHostState`, sedangkan `SnackbarHost` hanya terpasang di Scaffold halaman depan; di halaman dalam (detail penghuni/lokasi, halaman aksi, editor) tak ada host sehingga pesan baru tampil setelah kembali. Kini `notify()` dan pesan validasi di `RoomEditScreen` memakai **Toast** (overlay sistem, dibungkus `scope.launch`), sehingga **setiap aksi berhasil/gagal menampilkan pesan di halaman mana pun saat itu juga** — termasuk "Perubahan kamar berhasil disimpan." di halaman dalam. `SnackbarHost`/`SnackbarHostState` yang tak lagi terpakai dihapus.

## Unreleased — sesi tahan lama & pengambilan data tahan gangguan

### Fixed
- **"Nyangkut" ~20 detik saat memuat data (mis. "Memuat durasi & harga…").** Penyebab: server punya record **AAAA (IPv6)**; sebagian jaringan mengiklankan IPv6 tetapi tidak bisa merutekannya sehingga koneksi IPv6 menggantung sampai timeout sebelum jatuh ke IPv4. Klien kini memakai **DNS yang mengutamakan IPv4** (`Dns.SYSTEM` disaring ke `Inet4Address`) dan connect timeout 10 dtk, sehingga permintaan selesai dalam ratusan milidetik (terukur: 20.500 ms → 123 ms).

### Added
- **Sesi tahan lama (login permanen).** Refresh token kini berlaku **1 tahun** (naik dari 90 hari) dan **diperbarui tiap kali dipakai** (sliding), sehingga sesi aktif tidak pernah kedaluwarsa selama dipakai. Profil pengguna di-cache di perangkat; saat server sementara tidak terjangkau, aplikasi **tetap masuk** memakai cache (bukan dilempar ke layar login).
- **Data diperkuat terhadap gangguan sesaat.** `KostkuApi` mencoba ulang otomatis (3× dengan jeda bertambah) untuk permintaan tanpa efek samping (GET/HEAD/OPTIONS/DELETE) saat koneksi gagal atau proxy mengembalikan 502/503/504/408/429. POST/PATCH **tidak** diulang otomatis agar data (mis. pembayaran) tidak tergandakan.
- **Banner error global.** Pesan gagal + tombol **Coba lagi** kini tampil di **semua layar** (detail penghuni, lokasi, halaman aksi), bukan hanya tab utama; ada tombol **Tutup**. Muncul indikator tipis saat menyegarkan data.
- Pesan kegagalan jaringan diterjemahkan ramah ("Tidak dapat terhubung ke server…", "Koneksi terlalu lama…") di samping pesan teknis di log.

### Changed
- Saat menyegarkan data, layar **tidak lagi dikosongkan** (data lama tetap tampil + indikator tipis); spinner penuh hanya untuk muat pertama. Ini menghilangkan kesan "susah/hang" saat reload.
- Kegagalan jaringan sesaat **tidak lagi membuat sesi terhapus**; sesi hanya berakhir bila server benar-benar menolak (401) atau refresh token ditolak.

## Unreleased — FAB "Kelola" & halaman aksi penghuni

### Changed
- Layar **Tambah penghuni**: kolom **Lokasi kos** kini selalu berupa **dropdown** (sebelumnya hanya teks bila lokasi cuma satu). Bila hanya ada satu lokasi, dropdown tampil dalam keadaan nonaktif agar konsisten; `ChoiceDropdown` mendapat parameter `enabled`.
- Layar Tambah penghuni tidak lagi keliru menampilkan "Tidak ada kamar tersedia" saat data kamar masih dimuat — kini ada status "Memuat nomor kamar…".

### Added
- FAB **Kelola** di detail penghuni membuka bottom sheet berisi lima aksi, masing-masing punya halaman penuh sendiri (`features/tenant/TenantActionScreens.kt`):
  - **Edit data** — nama, nomor HP, dan foto (unggah/ganti/hapus).
  - **Perpanjang kos** — pilih **jumlah penghuni** (dropdown "Jumlah penghuni", mis. "1 orang"/"2 orang", diambil dari tarif tipe kamar) **lebih dulu**; di bawahnya muncul **durasi perpanjangan** yang menyesuaikan orang terpilih (dropdown, auto-fetch dari server). Begitu durasi dipilih, **tanggal keluar baru langsung dihitung & ditampilkan** (menumpuk dari tanggal keluar saat ini bila sewa masih berjalan, atau dari hari ini bila sudah lewat) beserta harga sewa dan sisa hari. Bila satu kombinasi punya beberapa tarif, muncul dropdown "Harga sewa". Tanggal keluar baru juga bisa **di-custom** lewat pemilih tanggal ("Ubah tanggal keluar (opsional)"), dengan tautan "Pakai hasil perhitungan durasi" untuk kembali ke hasil hitung. Menyimpan memperbarui tanggal keluar & harga sewa.
  - **Pindah kamar** — pilih kamar kosong, nama kamar ditampilkan bersama lokasinya (bisa lintas lokasi).
  - **Pindah tanggal** — ubah tanggal masuk & keluar (boleh dikosongkan).
  - **Penghuni keluar** — tandai keluar (kamar jadi tersedia lagi) atau arsipkan, dengan konfirmasi.
- Komponen bersama `FormCard`/`FormRow` (`ui/common/CommonComponents.kt`).

### Changed
- Menu top bar (Edit + ⋮) di detail penghuni dihapus, diganti FAB. Dialog lama `DetailDialogs`/`TenantEditDialog`/`MoveTenantDialog` dihapus.
- `KostkuApi.updateTenant` kini opsional menerima `monthlyRent`, `dueDay`, `moveInDate`, `moveOutDate` (non-destruktif); `PATCH /tenants/{id}` menerima `move_in_date` juga.
- Klien OkHttp memakai timeout eksplisit (connect 20 dtk, read/write 60 dtk) + `retryOnConnectionFailure`, agar operasi tulis sesekali lambat tidak gagal.
- Semua **tanggal ditampilkan dengan format `dd mmm yyyy`** (mis. "10 Okt 2026") via helper `formatDate` (`UiFormat.kt`); `DateField` dan detail penghuni (tanggal masuk/keluar) serta nilai log perubahan tanggal ikut memakainya.

## Unreleased — tab detail (ikon + pil) & log penghuni

### Changed
- Detail penghuni dan detail lokasi kini memakai tab **ikon di atas label** dengan indikator pil aksen (`PrimaryTabRow`), plus transisi konten fade+geser via komponen bersama `AppTabRow`/`TabContent` (`ui/common/TabComponents.kt`). Label tab penghuni: **Informasi · Pembayaran · Log**.
- Tab Log menampilkan waktu lokal WIB (`GET /tenants/{id}/audit` mengembalikan `created_at_wib` via `CONVERT_TZ`), format "10 Okt 2026, 10:32 WIB", ringkasan perubahan ditulis `Field: lama → baru`.

### Fixed
- Parser JSON klien: nilai `null` dari server (mis. `move_out_at`, `photo_url`, `room_type_id`) tak lagi tampil sebagai teks "null"; ditangani helper `optText`. Ini menghilangkan bug "Tanggal keluar: null" dan "Foto tidak dapat dimuat." pada penghuni/kamar yang fotonya kosong.
- `PATCH /tenants/{id}` kini menerima `monthly_rent`/`due_day`/`move_out_date` (opsional, non-destruktif) dan mencatat nilai lama→baru ke audit log.

### Added
- Unit test untuk pelabelan aksi/field log perubahan dan format waktu WIB (`auditTimestamp`).

## Unreleased — tenant soft-delete & production sync

### Fixed
- Photo upload returned 500 on production because the site's PHP (8.3 FPM) lacks the `fileinfo` extension while `mediaUpload` used `new finfo(...)`. The server now detects the image type with `getimagesize()` (GD, always present), so `POST /media/{tenant|room|payment}/{id}` uploads and `DELETE` cleanup work.
- `DELETE /tenants/{id}` no longer fails with 500 when the tenant has payment rows. It is now a soft-delete that sets status `archived` (migration `002_tenant_status_archived.sql`); archived tenants are hidden from `GET /tenants`. Payment history and audit entries are preserved. Active tenants still return 409.
- Tenant audit-log writes and the `tenant_audit_log` table deployed to production; backend files synced (repo ↔ production md5-identical).

### Changed
- Android tenant delete action relabeled to "Arsipkan penghuni" (archive) with matching confirmation copy.
- `AddTenantScreen` fetches room type prices (duration/price) and rooms directly from the server, so those dropdowns auto-fetch per selection.
- Removed unused `allRoomTypePrices`.

### Added
- `backend-php/deploy.sh` — sync local backend to production with backup + md5 verification (credentials via env).

## Unreleased — UI cleanup, tests, and tenant audit log

### Added
- Tenant audit log: `tenant_audit_log` table, `GET /tenants/{id}/audit` endpoint, transactional writes on tenant create/update/move/move-out, and a real `Log Perubahan Data` Android tab. Migration `backend-php/database/migrations/001_tenant_audit_log.sql`.
- Location detail `Riwayat Pembayaran` tab and an occupancy/income `Dashboard` tab.
- Payment edit (UI) using the existing `PATCH /payments/{id}` endpoint, plus a status filter on the payment list.
- Search and active/moved-out filter on the tenant list.
- Unit tests for Rupiah formatting, status labels, and move-out duration math; `docs/TESTING.md` with build/test/run and QA checklist.

### Changed
- Room status label `Perbaikan` unified to `Maintenance` in the UI.
- Move-out calculation now honors the price `duration_unit` (day/week/month/year) instead of assuming months.
- Client photo resize target aligned to 1280 px (was 1080) to match server docs/env.

### Removed
- Dead code: Gemini template leftovers (`BakingScreen`, `BakingViewModel`, `UiState`), duplicate root `RoomTypeScreens.kt`, unused `models/Models.kt` store/models, unused common components (`Components.kt`, `FormDialog`, `NumberField`, `ActionButton`, `PhotoPreview`, picker/camera helpers), `TenantDialogs.kt`, and unused resources (`baked_goods_*.jpg`, `lucide_license.xml`, Gemini strings).

## Unreleased — current implementation documentation

### Current

- Audited and hardened CRUD contracts for properties, room types, rooms, tenants, and payments.
- Added server-backed tenant profile PATCH and dependency-safe tenant DELETE/archival checks.
- Added server-backed payment PATCH validation and explicit cancellation protection.
- Added Indonesian error mapping without exposing HTTP codes or backend exception text.
- Ensured paid tenant placement creates an initial payment even when the discounted total is zero.
- Documented Kotlin Compose Material 3 architecture, four bottom destinations, tenant detail navigation, and location-to-room detail navigation.
- Documented Galeri/Kamera photo source choice, deterministic crop preview/continue/retake, EXIF orientation, longest-side ≤1280 px resize, and ≤500 KB client compression target.
- Documented tenant photos, room photos, payment proof photos, one-active-photo replacement/delete semantics, remote preview loading/error states, and upload-after-entity-create behavior without duplicate entity creation.
- Documented server ownership, MIME, dimension, and size validation; VPS filesystem media storage; metadata-only database storage; migration, environment, writable storage, `.htaccess`, and manual PHP deployment requirements.
- Clarified that the tenant audit log is placeholder UI and the backend audit endpoint/events are not implemented.

### Historical

Earlier local-first, five-tab, Beranda/Profile, PostgreSQL, and planned-backend descriptions are retained only as historical context in repository history; they are not current behavior.
