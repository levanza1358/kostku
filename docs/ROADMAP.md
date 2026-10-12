# Roadmap — Kostku

> **Rencana implementasi berjalan** (mock disetujui, dokumentasi & fitur) ada di [`docs/IMPLEMENTATION-PLAN.md`](IMPLEMENTATION-PLAN.md): rapikan detail lokasi, chip sisa hari, utilitas token/PDAM, logo lokasi, dan kwitansi.

## Current baseline

The Android client and PHP/MySQL/MariaDB API are integrated for authenticated locations, room types/prices, rooms, tenants, payments, dashboard data, session refresh, settings, and tenant/room/payment media. Media uses VPS filesystem storage with database metadata, one active photo per entity, client crop/resize/compression, and server validation. The tenant change log is now backed by `tenant_audit_log` and `GET /tenants/{id}/audit`.

## Next priorities

1. ~~Implement a real tenant audit-log backend endpoint and persistence, then replace the placeholder `Log Perubahan Data` UI.~~ **Done.**
2. Add automated unit, Compose UI, API integration, and real-device session/refresh/media tests (unit tests for format/date logic and `docs/TESTING.md` are in place).
3. Improve production deployment automation for migrations, environment values, VPS media storage, permissions, and `.htaccess`, while retaining the documented manual `index.php` upload fallback.
4. Add operational monitoring, backup/restore drills, and migration checks for MySQL/MariaDB and media files.
5. Review payment status/discount validation and date/time behavior against production PHP responses.

## Later / out of current scope

Payment gateway, tenant-facing app, booking, internal chat, full accounting/tax, automatic WhatsApp notifications, and multi-role management remain future or out of scope unless requirements change.
