# Kostku PHP backend

Deployable target for `https://kostku.miraya.my.id/v1`. PHP 8.2+ with PDO MySQL, Apache rewrite or PHP-FPM, and MySQL 8/MariaDB. Copy `.env.example` to `.env`, set the MySQL DSN/user/password, `JWT_SECRET`, Google web client ID, and CORS origin. Never commit `.env` or database credentials.

The API keeps the `/v1` base path and existing endpoint behavior, including JWT authentication, Google verification, ownership filtering, validation, transactions, and dashboard queries. The existing `backend/` Node API remains untouched.

## Snapshot and quota optimisation

- **`GET /snapshot`** returns the whole application dataset (properties, room types, prices, rooms, tenants, payments, expenses, categories) in **one** authenticated response, replacing the Android client's former fan-out of ~10+ requests. The response carries an **`ETag`** (SHA-256 of the body); the client stores the body locally and revalidates with `If-None-Match`, receiving **`304 Not Modified`** (empty body) when nothing changed. A successful save changes the data, so the next fetch returns a fresh `200` and a new `ETag` — cached data is never stale. The per-entity endpoints remain available and unchanged.
- **gzip** compression is enabled (`ob_gzhandler`) for all JSON responses when the client sends `Accept-Encoding: gzip`, cutting large snapshots by roughly 70–80%. `ETag` and `Accept-Encoding` are exposed through CORS.


## Adminer import

1. Create/select a MySQL 8 or MariaDB database named `kostku` with `utf8mb4` (do not paste credentials into the repository).
2. In Adminer, log in with **System: MySQL**, the server host, the database user, and its password.
3. Select the `kostku` database, choose **Import**, select `backend-php/database/schema.sql`, leave SQL as the format, and click **Execute**.
4. Confirm that `users`, `properties`, `rooms`, `tenants`, and `payments` were created. The script uses InnoDB, `CHAR(36)` UUIDs, `UUID()`-compatible UUID strings, foreign keys, checks, and indexes.

## Direct upload layout

Upload the entire contents of this folder into the document root of `kostku.miraya.my.id`:

```text
kostku.miraya.my.id/
├── index.html
├── .env
├── config/
├── database/
├── public/          # legacy/reference entry point
└── v1/
    ├── index.php
    └── .htaccess
```

The public website is `https://kostku.miraya.my.id/`. The API is `https://kostku.miraya.my.id/v1/`; test it at `/v1/health`. The `.env` file must be created from `.env.example`, must contain real server values, and must never be committed. The root and `v1` rules block sensitive files; keep `mod_rewrite` enabled.

For VPS, use Nginx/Apache TLS, PHP-FPM, a private MySQL/MariaDB service, firewall, backups, and log monitoring. PHP must have `pdo_mysql` enabled.
