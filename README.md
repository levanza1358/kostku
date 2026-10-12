# Kostku

Kostku is an Android app for owners and managers of boarding houses (`kos`). It manages server-backed locations, rooms, tenants, room-type pricing, manual payments, expenses, and photos.

## Current truth

- **Android:** Kotlin, Jetpack Compose, Material 3; min/target/compile SDK 34/37/37 and Java 11.
- **Backend:** native PHP 8.2+ with PDO and MySQL 8/MariaDB at `https://kostku.miraya.my.id/v1/`. The app never connects directly to the database.
- **Authentication:** Google Login is mandatory. The app exchanges the Google ID token for access/refresh tokens, restores `/me`, and keeps the session permanent: the refresh token lasts 1 year and slides on every use. Network hiccups never log you out — safe requests retry automatically and the session is kept; only a real 401 ends it, with Indonesian session-expired handling.
- **Navigation:** four bottom destinations: `Penghuni`, `Lokasi`, `Pembayaran`, and `Pengaturan`. Tenant detail is a separate full page without bottom navigation, with `Informasi`, `Riwayat Pembayaran`, and `Log Perubahan Data` tabs. Location detail is opened from `Lokasi` and room management is opened from that detail flow.
- **Data:** remote server data is authoritative. Successful writes reload the affected snapshot; failed writes keep the form open. CRUD mutations are ownership-checked and dependency-safe. There are no demo records or local CRUD storage.
- **Language:** all user-visible UI and feedback is Bahasa Indonesia.

## Implemented product flows

Locations support name, branch, address, and description. Rooms use statuses `vacant` (Tersedia), `occupied` (Terisi), `maintenance` (Maintenance), and `dirty` (Kotor). Setup order is mandatory: **lokasi → tipe kamar → harga → nomor kamar**. Room types are created before rooms, each type has normalized prices by occupancy and flexible duration (day/week/month/year), and **a room number can only be created for a room type that already has a price** — the server rejects room creation without a price and the batch form offers only priced types. Batch rooms support numeric sequences or a custom list, 1–100 rooms, duplicate validation, and transactional server creation.

Adding a tenant requires a name; phone is optional. The user selects a location, room type, available room, and price. Because a placement needs an available room with a price, **tenant creation is gated on the whole chain above being complete** (at least one room). Move-in defaults to device date/time. Move-out is calculated from the selected duration and can be edited by tapping the date; there is no custom move-out toggle and no independent move-out time. Payment defaults to `Lunas`, with a payment method and income category; the placement **always writes one income payment** (full amount when Lunas, otherwise the entered partial/zero amount). **Perpanjang kos** (extend) updates the lease and **records an income payment** in the same transaction, so every extension lands in the income/payment data and the tenant change log.

The `Pembayaran` screen has three tabs: **Semua** (a combined ledger of income and expenses, newest first, with total pemasukan and total pengeluaran), **Pemasukan** (tenant payments with status filters), and **Pengeluaran**. A single `+` FAB pops up a menu (Pemasukan/Pengeluaran, not a bottom sheet). Expenses cover **operational costs** and **refunds to a tenant**, each with date, amount, method, optional location, notes, and an optional receipt photo; expenses can be edited and deleted (with confirmation). Income and expense **categories are user-managed** from **Pengaturan → Kategori** (global per account); records reference a category by name, so editing the list does not change existing records.

### Photos

Photo fields explicitly offer `Galeri` or `Kamera`. The selected/captured image is center-cropped deterministically, shown in a preview, and must be confirmed with `Lanjutkan`; `Retake` discards the pending file. The client applies EXIF orientation, resizes the longest side to at most 1280 px, and JPEG-compresses to a target of at most 500 KB. The original/high-resolution input is not uploaded or retained.

Photos are used for tenant photos, room photos, and payment proof photos. Each entity has at most one active photo. Replacing a photo creates the new media metadata before removing the old stored file; deleting a photo soft-deletes its metadata and removes the stored file. Remote previews expose loading and `Foto tidak dapat dimuat.` error states.

The entity is created first, then the photo upload is attempted. If upload fails, the created entity is not created again automatically; the user must retry the photo operation or edit the existing entity. The upload failure does not roll back the already-created entity.

The server checks ownership, actual MIME (JPEG/PNG/WebP), image dimensions, and size. Media bytes are stored on VPS filesystem storage, not as database blobs; the database stores media metadata and relative paths.

Tenant detail shows information and payment history. The `Log Perubahan Data` tab is backed by a real audit log: the API records create, update, room move, and move-out events in `tenant_audit_log` and the app reads them from `GET /tenants/{id}/audit`.

Settings contains `Ringkasan`, `Profile`, `Tema`, `Tentang Aplikasi`, `Open Source`, `Bantuan`, `Privasi`, and `Keluar`. Theme mode and accent are persisted locally; business data and session tokens use their respective stores.

## Media deployment

Import `backend-php/database/schema.sql` (single source of truth for the schema; it includes the `media` table) into a fresh or empty database. Configure `MEDIA_STORAGE_ROOT`, `MEDIA_PUBLIC_BASE_URL`, `MEDIA_MAX_BYTES`, and `MEDIA_MAX_SIDE` in the server environment, create writable VPS storage, and deploy the PHP entrypoint plus the repository `.htaccess` rules. The media directory must be served at the configured public URL while configuration, database, and `.env` files remain blocked. Upload `backend-php/public/index.php` manually; local changes do not deploy automatically.

## Source map

- `app/src/main/java/com/miraya/kostku/MainActivity.kt` — activity, Google sign-in launch, theme preferences.
- `App.kt` — authenticated app state, four-destination navigation, settings and screens.
- `models/Models.kt` — UI/domain models.
- `ui/common/Components.kt` — shared Compose components.
- `media/` — photo source, crop/resize/compression, and remote preview.
- `TenantDetailScreen.kt` — tenant detail tabs and the real change-log tab (tenant audit endpoint).
- `NetworkApi.kt` — PHP API client, token storage, automatic refresh, CRUD, and media upload/delete.

See [User flows](docs/USER-FLOWS.md), [API specification](docs/API-SPEC.md), [Database](docs/DATABASE.md), and [deployment README](backend-php/README.md).

The `backend/` Node/PostgreSQL tree is legacy and is not the production service.
