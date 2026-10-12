# User Flows — Kostku

## Login and session

Open app → Google Login wajib → ID token exchanged at `/v1/auth/google` → access/refresh tokens stored → `/me` restores profile → app opens `Penghuni`. A 401 refreshes once; failure clears tokens and returns to login with Indonesian feedback.

## Main navigation

Bottom navigation has exactly four destinations: `Penghuni`, `Lokasi`, `Pembayaran`, and `Pengaturan`. Opening a tenant from `Penghuni` opens full detail without bottom navigation. Back returns to the list. Opening a location from `Lokasi` opens location detail and room-management navigation.

## Photo flow

In a tenant, room, or payment form: tap the photo field → choose `Galeri` or `Kamera` → select/capture → deterministic center crop → preview → `Lanjutkan` to accept or `Retake` to discard. The client applies orientation, resizes the longest side to ≤1280 px, and compresses to a target ≤500 KB. Remote previews show loading and a non-fatal load error.

## Tenant detail and add tenant

Tap a tenant → `Informasi` / `Riwayat Pembayaran` / `Log Perubahan Data`. The log shows real audit entries (created, updated, moved, moved out) loaded from `GET /tenants/{id}/audit`. Information shows placement, room, rent, due day, status, contact, and tenant photo when available.

`Penghuni` → add (only once the setup chain is complete — see below) → identity first (required name, optional phone) → location → room type → available room → normalized price → move-in date → calculated move-out → initial payment (status default Lunas; partial/pending take an amount) → payment method → income category → optional tenant photo → save. The form marks required fields, keeps the save button active, and shows inline errors that scroll to the first invalid field. The server validates and creates the entity transactionally and **always writes one income payment row for the placement** (the full amount when Lunas; the entered partial or zero amount otherwise, as `partial`/`pending`), tagged with the chosen method and category; after the tenant exists, the photo upload is attempted separately. Upload failure leaves the tenant exactly once and requires retry/edit, not duplicate creation.

## Locations, types, prices, and rooms

The setup order is mandatory and enforced step by step: **lokasi → tipe kamar → harga → nomor kamar**.

`Lokasi` → add location (name/address required) → open location detail → **create room type** → **add normalized prices by occupancy and day/week/month/year duration** → **add one or many rooms**. Room numbers can only be created for a room type that already has a price: the "Tambah Banyak Kamar" form lists only priced types (with a hint when none exist), and the server rejects room creation for an unpriced type. Batch creation supports 1–100 names and transactionally rejects duplicates. Room forms may attach one room photo; replacement and delete follow the media rules.

## Adding a tenant (requires a complete chain)

Because a placement needs an available room with a price, `Penghuni` → add is gated on the whole chain above being complete, and the placement form offers **only "complete" room types — those that already have both a price and at least one room number**. Incomplete types (missing a price, or a price but no room) are hidden from the "Tipe kamar" dropdown; if the selected location has no complete type, the form shows "Belum ada tipe kamar yang lengkap di lokasi ini. Tipe kamar wajib sudah punya harga dan nomor kamar sebelum bisa dipakai menambah penghuni." Until at least one complete type exists, the tenant list shows a guidance empty state instead of the add form. An empty room set for a complete type still shows "Belum ada nomor kamar tersedia untuk tipe ini…" (all rooms occupied).

## Payments and move-out

`Pembayaran` has three tabs: **Semua** (a combined ledger of income and expenses, newest first, with total pemasukan and total pengeluaran), **Pemasukan** (server payments with status filters), and **Pengeluaran** (expenses with a type filter). A single **+** FAB opens a floating pop-up menu (not a bottom sheet) with **Pemasukan** and **Pengeluaran**; the FAB turns into an **X** while open. Choose a manual payment with tenant, period, amount, date, method, status, and optional payment proof photo. Proof upload is separate from payment creation; failure does not duplicate the payment. Each income/expense row has an overflow (three-dot) menu with **Edit** and **Hapus**; deleting asks for confirmation and removes the record permanently. Choose `Pengeluaran` to record either an operational cost (category: listrik/air, perbaikan, kebersihan, gaji, internet, perabot, lainnya; optional location; optional receipt photo) or a refund to a tenant (tenant required); expenses use the same three-dot Edit/Hapus menu. Move-out closes the placement, frees the room (applying the chosen Tersedia/Kotor/Maintenance status), and archives the tenant into the move-out history in one atomic step.

## Kelola penghuni (extend, move, dates, exit)

The tenant detail screen's FAB opens the "Kelola penghuni" actions. **Perpanjang kos** chooses occupancy → duration → price, computes the new lease end (stacking on the current end while the lease is still running, otherwise from today), and takes the extension payment (discount, Lunas/Belum lunas, amount, method, income category). Saving updates the tenant's `move_out_date` and `monthly_rent` **and records one income payment** (see API-SPEC `POST /tenants/{id}/extend`) so every extension lands in the income/payment data, with an **Diperpanjang** entry in the tenant's change log. **Pindah kamar** moves the placement to a vacant room, **Ubah tanggal** edits move-in/move-out, and **Penghuni keluar** closes the placement. Marking a tenant **keluar** automatically archives the tenant into the **riwayat penghuni keluar** and then asks for the room's new status (**Tersedia / Kotor / Maintenance**, default Tersedia) on a follow-up screen, viewable under **Pengaturan → Riwayat → Penghuni keluar**, filtered by month with ‹ › navigation (the next-month arrow is disabled at the current month). The history is a snapshot taken at move-out, so it remains even after the tenant is archived.

## Settings and network behavior

`Pengaturan` → `Ringkasan`, `Profile`, `Tema`, `Tentang Aplikasi`, `Open Source`, `Bantuan`, `Privasi`, or `Keluar`. Reads may retry. Mutations show loading, prevent duplicate submission, preserve forms on failure, and refresh only after success. Technical errors are translated to Indonesian.
