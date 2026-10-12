# API Specification — Kostku

**Status:** Current contract for native PHP + PDO at `https://kostku.miraya.my.id/v1/` (MySQL 8/MariaDB).

## Conventions

All non-auth endpoints use `Authorization: Bearer <access_token>`. JSON is exchanged over HTTPS and IDs are UUID strings. The Android client translates technical errors to Indonesian. A 401 triggers one refresh attempt; if the refresh is rejected the session ends, but a network failure during refresh keeps the session and retries. The refresh token lives 1 year and slides on every use.

## Authentication and main endpoints

- `POST /auth/google` — `{id_token}` → user, access token, refresh token.
- `POST /auth/refresh` — `{refresh_token}` → new access token and possibly rotated refresh token.
- `POST /auth/logout`; `GET /me`.
- `GET/POST /properties`; `GET/PATCH/DELETE /properties/{id}`. A property accepts optional `token_listrik` and `pdam_number` (location utilities, stored as text) and exposes a `logo_url` derived from `media` (`entity_type='property'`).
- `GET/POST /properties/{id}/room-types`; `PATCH/DELETE /room-types/{id}`.
- `GET/POST /properties/{id}/rooms`; `POST /properties/{id}/rooms/batch`; `PATCH/DELETE /rooms/{id}`. A room accepts optional `token_listrik` and `pdam_number` (room utilities, stored as text). `DELETE /rooms/{id}` purges the room's media (metadata + file).
- `GET/POST /room-types/{id}/prices`; `PATCH/DELETE /room-type-prices/{id}`.
- `GET/POST /tenants`; `GET/PATCH/DELETE /tenants/{id}`; `POST /tenants/{id}/move`; `POST /tenants/{id}/move-out`; `POST /tenants/{id}/extend`; `GET /tenants/{id}/audit`; `GET /tenants/move-out-history`.

`DELETE /tenants/{id}` is a soft-delete: it sets the tenant status to `archived` (the tenant disappears from `GET /tenants` and from tenant detail), keeps payment history and audit entries, and **purges the tenant's photo** (metadata rows + file). An `active` tenant cannot be archived; move it out first (409 otherwise). `GET /tenants` excludes archived tenants.
- `GET/POST /payments`; `GET/PATCH/DELETE /payments/{id}`. Payments accept an optional `category` (free-text name from the owner's income categories). `DELETE /payments/{id}` permanently removes the payment and **purges its media (metadata + file)**; the list actions are exposed as a row overflow menu (Edit / Hapus).
- `GET/POST /expenses`; `GET/PATCH/DELETE /expenses/{id}`. `DELETE /expenses/{id}` purges the expense's media (metadata + file).
- `GET/POST /categories`; `PATCH/DELETE /categories/{id}`.
- `GET /dashboard/summary`.

All business resources are ownership-scoped. Room batch creation accepts 1–100 names and is atomic. Room statuses are `vacant`, `occupied`, `maintenance`, and `dirty`. Prices use `occupancy`, `duration_value`, `duration_unit` (`day`, `week`, `month`, `year`), and `amount`.

## Setup order (lokasi → tipe kamar → harga → nomor kamar)

Creation is enforced top-down. `POST /properties/{id}/rooms` and `POST /properties/{id}/rooms/batch` return **409 `CONFLICT`** when the target room type has no price row, so a room number cannot be created before its price exists. This check is controlled by the `STRICT_ROOM_PRICING` environment variable (`1` = enforced, the default; `0` = relaxed for legacy data backfills). Adding a tenant consequently requires the whole chain to exist, because `POST /tenants` requires an available room and a matching price.

## Tenant placement

`POST /tenants` accepts the name, optional phone, property/room/type/price IDs, move-in/out datetime and compatibility date fields, rent, due day, amount paid, payment status, discount fields, and optional payment `method` (`cash`/`transfer`/`other`, default `other`) and `category`. The server validates ownership, availability, matching relationships, and dates, then creates the tenant, the initial income payment, and the room occupation transactionally. **Every placement writes one payment row** — the server-calculated initial payment (full amount, or the entered partial/zero amount as `partial`/`pending`), tagged with the given method and category. The `created` audit entry records the amount and method. Android reloads its snapshot after success.

`POST /tenants/{id}/extend` (perpanjang) updates the lease and records money in. Body: `move_out_date` (required, the new lease end), optional `monthly_rent` (defaults to the current rent), `amount_paid`, `payment_status` (`paid`/`partial`/`pending`), `method`, `category`, and optional `discount_type`/`discount_value`. The server recomputes the total from rent minus discount, clamps the paid amount, updates the tenant's `move_out_date` and `monthly_rent`, and inserts one income payment (`room_id` from the tenant, `period_month` at the start of the paid period, `paid_at` now) — all transactionally. It writes an `extended` audit entry and returns `{id, move_out_date, monthly_rent, payment_status}`. This is how an extension always lands in the income/payment data.

## Categories (kategori pemasukan & pengeluaran)

`categories` are an owner's own income/expense labels, global per account. `GET /categories` lists them ordered by `kind, sort_order, name`; the call seeds a default list whenever the owner has none at all, so a fresh account always starts with sensible options. `POST /categories` takes `kind` (`income` or `expense`) and `name` (1–100 chars); a duplicate `(owner, kind, name)` returns **409**. `PATCH /categories/{id}` renames (same uniqueness rule), `DELETE /categories/{id}` removes it. Records reference categories **by name** in `payments.category` / `expenses.category`, so renaming or deleting a category does not alter existing records. All endpoints are ownership-scoped.

## Expenses (pengeluaran)

`POST /expenses` records money going out. Body: `kind` (`operational` or `refund`, default `operational`), `amount` (required, ≥ 0), `spent_at` (UTC datetime, default now), `method` (`cash`/`transfer`/`other`), optional `category` (used for operational), optional `tenant_id` (required for refund; ownership-checked), optional `property_id`, `reference_number`, and `notes`. `GET /expenses` lists the owner's expenses (newest first) including a `photo_url` from `media`. `PATCH /expenses/{id}` updates the same fields (omitted fields keep their values). `DELETE /expenses/{id}` removes the row. All endpoints are ownership-scoped. An expense is the single source for outflows; refunds are not mirrored into `payments`.

## Media

- `POST /media/{tenant|room|payment|expense|property}/{id}` — authenticated multipart field `file`; ownership is checked before storage. `property` stores the **location logo** (category folder `lokasi`).
- `DELETE /media/{tenant|room|payment|expense|property}/{id}` — soft-delete active metadata and remove the stored file.
- Deleting the owning entity purges its media: the `media` rows are removed **and** the files are unlinked (`mediaPurge`). This applies to `DELETE /payments/{id}`, `DELETE /expenses/{id}`, `DELETE /rooms/{id}`, and tenant archive (`DELETE /tenants/{id}`).
- Media files are served through the CDN/proxy with a long cache (about 1 month). After a delete the origin file is gone immediately, but a cached URL may keep returning the old bytes until that cache expires.
- One active photo is allowed per entity. Replacement commits the new metadata before removing the old file.
- Client input is center-cropped deterministically, oriented using EXIF, resized to longest side ≤1280 px, and JPEG-compressed to a target ≤500 KB. The server validates ownership, image type from the file header (`getimagesize`, so it works without the `fileinfo` extension), dimensions (each side ≤4096 px), and actual upload size (default 524288 bytes).
- Media is stored on VPS filesystem storage; the database stores metadata, dimensions, checksum, and relative path, not blobs. New paths use `media/{owner_id}/{category}/{YYYY}/{MM}/{random-file}`, where `category` is `penghuni`, `kamar`, `pembayaran`, `pengeluaran`, or `lokasi`; the client never chooses `owner_id`.
- Photos support tenant photos, room photos, payment proof photos, expense proof photos, and the **location logo** (one active photo per entity). Upload is separate from entity creation: an upload failure does not create a duplicate entity and does not roll back an already-created entity.

## Utilities and receipt

Location utilities (`token_listrik`, `pdam_number`) travel on the property payload; room utilities travel on the room payload. Both are optional free text and are surfaced in the tenant detail (grouped as room utilities and location utilities). There is no inheritance between levels.

A **receipt (kwitansi)** is not a server resource — it is rendered on the client from a single payment record (amount, status, period, method, category) plus its tenant and location. No `/receipt` endpoint exists; the location logo comes from the property's `logo_url`. The client must stamp **LUNAS** only for `status == paid`.

## Tenant audit log

`GET /tenants/{id}/audit` returns the change history for one tenant (ownership-scoped), newest first. Each entry has `id`, `action` (`created`, `updated`, `moved`, `moved_out`, `extended`, `archived`), `details` (JSON text with the changed fields, move source/target room, move-out date, or extension end/amount), and two timestamps: `created_at` (UTC, stored) and `created_at_wib` (`CONVERT_TZ(created_at,'+00:00','+07:00')`, for display). On an `updated` entry, `details` carries `fields` (the changed column names) and `changes` (each field's `from`/`to` values). The API writes one entry transactionally on tenant creation, profile update, room move, move-out, lease extension, and archive. If the `tenant_audit_log` table has not been migrated yet, the write is skipped silently so tenant flows keep working.

`PATCH /tenants/{id}` accepts optional `monthly_rent`, `due_day`, and `move_out_date` in addition to the profile fields; omitted fields keep their current values, and each changed field is recorded in the audit `changes` map. `PATCH` never writes a payment: use `POST /tenants/{id}/extend` for a lease extension that must be recorded as income.

`POST /tenants/{id}/move-out` marks an **active** tenant as `moved_out`, frees the room, writes a `moved_out` audit entry, and **records a snapshot into `tenant_move_out_history`** (name, phone, room, move-in/move-out dates, rent, notes) — all transactionally. The body accepts `move_out_date` (default: today) and **`room_status`** — the room's new status after move-out: `vacant` (Tersedia, default), `dirty` (Kotor), or `maintenance`; anything else returns 400 `VALIDATION_ERROR`. The response echoes the applied `room_status`. Calling it again on an already-`moved_out` or `archived` tenant returns **409 `CONFLICT`**.

`GET /tenants/move-out-history?month=YYYY-MM` returns the moved-out tenants recorded for that month (the month is the `move_out_date`'s month; when omitted or malformed it defaults to the current month). Each row is the snapshot (`full_name`, `phone`, `room_name`, `move_in_date`, `move_out_date`, `monthly_rent`, `notes`) plus `id`, `tenant_id`, `room_id`, `created_at`, and a `photo_url` when the tenant still has an active photo. Ownership-scoped, newest move-out first. The history persists after the tenant is archived (the row cascades only if the tenant row is hard-deleted).

## Errors and deployment

Use 400 validation, 401 unauthenticated/expired, 403 forbidden, 404 hidden/not found, 409 conflict, 413 too large, and 5xx server errors as applicable. Production `index.php` is uploaded manually. Media deployment requires the migration/table, server environment values (`MEDIA_STORAGE_ROOT`, `MEDIA_PUBLIC_BASE_URL`, `MEDIA_MAX_BYTES`, `MEDIA_MAX_SIDE`), writable VPS storage, and deployed `.htaccess` protections. Room-creation gating uses `STRICT_ROOM_PRICING` (default `1`); set `0` to allow room creation without a price during legacy backfills.
