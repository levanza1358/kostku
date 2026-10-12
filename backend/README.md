# Kostku backend

Node.js/TypeScript REST API with PostgreSQL. The API is served under `/api/v1`; HTTPS is terminated by Nginx.

## Local/deployment

1. Copy `.env.example` to `.env` and replace every `CHANGE_ME`/placeholder with deployment secrets. Never commit `.env`.
2. With Docker: `POSTGRES_PASSWORD='...' docker compose up -d --build`, then `docker compose exec api node dist/migrate.js` (or run `npm run migrate` from a configured Node environment).
3. Point `api.kostku.miraya.my.id` (or the chosen `kostku.miraya.my.id` API path) at the VPS, proxy to `127.0.0.1:3000`, and provision TLS with Let's Encrypt.
4. Restrict PostgreSQL to the internal Docker network, configure firewall/backup monitoring, and schedule `pg_dump` backups.

Google authentication requires a web client ID in `GOOGLE_CLIENT_ID`; `/auth/google` verifies ID tokens with Google. All other endpoints require a bearer access token. Amounts are PostgreSQL `numeric` values serialized as strings by `pg`.

Scripts: `npm ci`, `npm run build`, `npm run migrate`, `npm start`.
