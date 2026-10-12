# Database — Kostku

**Status:** Production schema for native PHP + PDO and MySQL 8/MariaDB.

Android accesses the database only through the API. The authoritative schema is `backend-php/database/schema.sql`; use InnoDB, `utf8mb4`, UUID `CHAR(36)` identifiers, UTC `DATETIME`, and `DECIMAL(14,2)` money.

## Core tables

`users`, `properties`, `room_types`, `room_type_prices`, `rooms`, `tenants`, `payments`, `expenses`, `categories`, `media`, `tenant_audit_log`, and `tenant_move_out_history`. Properties own room types and rooms; tenants reference placements and prices; payments reference tenants; expenses reference a tenant (for refunds) and optionally a property; categories belong to an owner. Every query is ownership-scoped.

`tenant_audit_log` stores the tenant change history: one row per mutation (`created`, `updated`, `moved`, `moved_out`, `extended`) with `owner_id`, `tenant_id`, an `action`, optional JSON `details`, and `created_at`. It is written by the API inside the same transaction as the tenant mutation. Rows cascade-delete with their tenant.

`tenant_move_out_history` stores one snapshot per move-out (`POST /tenants/{id}/move-out`): `owner_id`, `tenant_id`, `full_name`, `phone`, `room_id`, `room_name`, `move_in_date`, `move_out_date`, `monthly_rent`, optional `notes`, and `created_at`. It is written in the same transaction as the move-out, so it is the durable "penghuni keluar" registry that survives later edits or archiving. Read by month via `GET /tenants/move-out-history`. Rows cascade-delete with their tenant.

`media` stores owner/entity type and ID, relative filesystem path, actual MIME, bytes, width, height, checksum, timestamps, and nullable `deleted_at`. `entity_type` is `tenant`, `room`, `payment`, `expense`, or `property` (the location **logo**). It stores metadata only: photo bytes live on VPS filesystem storage, not in database blobs. New uploads use `media/{owner_id}/{category}/{YYYY}/{MM}/{random-file}` where category is `penghuni`, `kamar`, `pembayaran`, `pengeluaran`, or `lokasi`; the active-media index supports one active photo per owner/entity.

## Business data

Room statuses are `vacant`, `occupied`, `maintenance`, and `dirty`. Tenant placement and room occupation are transactional. Move-out archives the tenant and sets the room to the requested status (`vacant` default, `dirty`, or `maintenance`) while retaining historical tenant/payment records. Tenant status is `active`, `moved_out`, or `archived`; `DELETE /tenants/{id}` never removes rows — it archives the tenant (status `archived`) so payment history and audit entries remain intact. Prices use occupancy and flexible day/week/month/year duration.

Creation order is enforced top-down: `properties` → `room_types` → `room_type_prices` → `rooms` → `tenants`. A room cannot be created for a room type that has no price: the API rejects it (409) while `STRICT_ROOM_PRICING` is `1` (the default). Set `STRICT_ROOM_PRICING=0` in the environment to relax this for legacy data backfills. Rooms reference a room type, and tenants reference a room and a price, so a tenant always sits on top of the whole chain.

`properties` may carry an optional **`token_listrik`** and **`pdam_number`** (location-level utilities) and each `rooms` row may carry its own optional **`token_listrik`** / **`pdam_number`**. Both are `VARCHAR(60) NULL` and stored **as text** (never numeric) so leading zeros are preserved. There is no inheritance: the tenant detail reads the room's row and the location's row separately and shows "Tidak tersedia" for a missing value.

Datetime columns are `move_in_at` and `move_out_at`; date-only compatibility fields may remain for older deployments. Placement also uses rent, due day, amount paid, payment status, and discount fields. The server derives move-out from normalized price duration.

There is a tenant audit table (`tenant_audit_log`) and a `GET /tenants/{id}/audit` endpoint; the Android `Log Perubahan Data` tab reads from it. A tenant **placement always writes one `payments` row** (full amount when Lunas, otherwise the entered partial/zero amount as `partial`/`pending`, with method and category), and **every lease extension** writes one `payments` row via `POST /tenants/{id}/extend`; `PATCH /tenants/{id}` never writes a payment.

`expenses` records money going out. `kind` is `operational` (electricity/water, repairs, salaries, etc., described by `category`) or `refund` (money returned to a tenant, linked via `tenant_id`). `spent_at` is UTC; the Android "Pengeluaran" tab and the combined "Semua" ledger read from `GET /expenses`. An expense is the single source for outflows — refunds are not mirrored into `payments`.

`categories` holds the owner's own income/expense category list (global per account). `kind` is `income` or `expense`; `name` is unique per `(owner_id, kind)` and ordered by `sort_order`. Records store the category **by name** in `payments.category` and `expenses.category`, so renaming or deleting a category never rewrites existing records. The API seeds a sensible default list whenever an owner has no categories at all (income: Sewa, Deposit, Denda, Lainnya; expense: Listrik/Air, Perbaikan, Kebersihan, Gaji, Internet, Perabot, Lainnya), so the pickers always offer options unless at least one category exists. Managed from Android **Pengaturan → Kategori** via `GET/POST /categories` and `PATCH/DELETE /categories/{id}`.

## Schema and media operations

`database/schema.sql` is the single source of truth and already includes the `media`, `tenant_audit_log`, and `tenant_move_out_history` tables, so import it into a fresh or empty database. Configure `MEDIA_STORAGE_ROOT`, `MEDIA_PUBLIC_BASE_URL`, `MEDIA_MAX_BYTES` (default 524288), and `MEDIA_MAX_SIDE` (default 1280) in the server environment. Create the storage directory with least-privilege ownership/writability and keep it outside the database. Deployment also requires the public `.htaccess` protections and the manually uploaded PHP entrypoint.

`database/migrations/` holds numbered, idempotent SQL files for production databases that already hold data. Currently: `001_tenant_audit_log.sql` (creates `tenant_audit_log`), `002_tenant_status_archived.sql` (allows the `archived` tenant status used by soft-delete), `003_expenses.sql` (creates `expenses`), `004_media_expense.sql` (allows `entity_type='expense'` in `media`), `005_categories.sql` (creates `categories` and adds `payments.category`), `006_tenant_move_out_history.sql` (creates `tenant_move_out_history` and backfills existing `moved_out` tenants), and `007_utilities_logo.sql` (adds `token_listrik`/`pdam_number` to `properties` and `rooms`, and allows `entity_type='property'` in `media` for the location logo). Each migration uses idempotent DDL (`CREATE TABLE IF NOT EXISTS`, `DROP CONSTRAINT IF EXISTS` + `MODIFY COLUMN`, `ADD COLUMN IF NOT EXISTS`) so it is safe to re-run; `006`'s backfill is guarded by `NOT EXISTS`.

Back up before schema changes and restore-test backups. Keep MySQL/MariaDB private.
