# Architecture — Kostku

**Status:** Current online implementation

## Runtime architecture

```text
MainActivity / Compose UI
  -> App.kt screen state and navigation
  -> media pipeline + NetworkApi.kt (OkHttp, JSON, tokens, refresh)
  -> HTTPS https://kostku.miraya.my.id/v1/
  -> native PHP + PDO
  -> MySQL 8 / MariaDB and VPS filesystem media storage
```

Android never connects directly to the database. The server is the source of truth. Successful mutations reload the remote snapshot; failures preserve form state.

## Android source split

- `MainActivity.kt`: edge-to-edge activity, mandatory Google Sign-In, theme preferences.
- `App.kt`: authenticated state, four bottom destinations, settings, forms, entity creation, and post-create media upload.
- `models/Models.kt`: app models/state.
- `media/`: Galeri/Kamera source choice, deterministic crop preview/continue, EXIF-aware resize/compression, and remote preview loading/error.
- `TenantDetailScreen.kt`: full-page tenant detail with three tabs, including a real change-log tab backed by `GET /tenants/{id}/audit`.
- `NetworkApi.kt`: API client, token storage/refresh, CRUD, and media endpoints.

## Authentication and navigation

Google Login is mandatory. The ID token is exchanged at `/auth/google`; `/me` restores the profile and a 401 attempts refresh once. Root navigation is exactly `Penghuni`, `Lokasi`, `Pembayaran`, `Pengaturan`. Tenant detail hides bottom navigation and uses `Informasi`, `Riwayat Pembayaran`, and `Log Perubahan Data`; location detail leads to room management. The `Pembayaran` screen uses tabs `Semua` (combined income+expense ledger), `Pemasukan`, and `Pengeluaran`.

## Setup order enforcement

Data is created top-down: lokasi (`properties`) → tipe kamar (`room_types`) → harga (`room_type_prices`) → nomor kamar (`rooms`) → penghuni (`tenants`). Both layers hold the line: the PHP API rejects room creation when the room type has no price (409, `STRICT_ROOM_PRICING=1` by default; set `0` to relax for legacy backfills), and the Android batch-room form lists only priced types. Because a placement needs an available room with a price, the `Penghuni` add flow is gated until at least one **complete** room type exists, and the placement form offers **only complete types** — those that have both a price and at least one room number. Incomplete types (missing a price, or a price but no room) are hidden from the placement dropdown, with an explicit message when the location has no complete type.

`NetworkApi.snapshot()` loads properties, room types, each type's prices, rooms, tenants, payments, expenses, and categories in one pass; `RemoteSnapshot.prices` powers the type-price screen and the priced-type gate, `RemoteSnapshot.expenses` powers the Pembayaran `Pengeluaran`/`Semua` tabs, and `RemoteSnapshot.categories` feeds the Pengaturan → Kategori screen and the category dropdowns in the payment/expense dialogs.

## Media lifecycle

The UI chooses Galeri or Kamera, center-crops deterministically, previews the result, and continues only after confirmation. The client limits the longest side to 1280 px and targets ≤500 KB. The entity is created first; tenant/room/payment media is uploaded separately. Failure reports the upload problem without retrying entity creation. Replacement and deletion are explicit, with one active photo per entity. Remote preview has loading and non-fatal error states.

The PHP server validates ownership, actual MIME, dimensions, and size, stores bytes on VPS filesystem, and stores metadata/path in MySQL. Deleting an entity purges both the media metadata rows and the physical files through `mediaPurge` (hard deletes of payments, expenses, and rooms; tenant archive), so no orphan files or stale rows are left behind. Files are served through a CDN, so a just-deleted URL may briefly still resolve from cache even though the origin file is gone. Deployment needs the media migration, environment variables, writable storage, and `.htaccess` protections.

## Tenant change history

The API writes a `tenant_audit_log` row on tenant creation, profile update, room move, move-out, and lease extension (perpanjang), inside the same transaction as the mutation. Move-out additionally applies the requested room status (`vacant`/`dirty`/`maintenance`, default `vacant`) and archives the tenant into `tenant_move_out_history` in that same transaction — the follow-up **Status kamar** screen in the app supplies the choice. The Android `Log Perubahan Data` tab loads these entries from `GET /tenants/{id}/audit` and shows time, action, and changed fields. Tenant placement and lease extension each also write a `payments` row in the same transaction (`POST /tenants`, `POST /tenants/{id}/extend`); profile updates never touch payments.
