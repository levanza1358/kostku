package com.miraya.kostku.features.tenant

import androidx.compose.runtime.Composable
import com.miraya.kostku.*
import com.miraya.kostku.media.MediaPipeline

/**
 * Router halaman aksi penghuni: memilih halaman sesuai id aksi dan menjalankan
 * panggilan API-nya lewat [mutate] (yang menampilkan pesan & memuat ulang data).
 */
@Composable
internal fun TenantActionPage(
    page: String,
    tenant: RemoteTenant,
    snapshot: RemoteSnapshot,
    saving: Boolean,
    api: KostkuApi,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    mutate: (String, suspend () -> Unit) -> Unit
) {
    val room = snapshot.rooms.firstOrNull { it.id == tenant.roomId }
    val roomTypeId = room?.roomTypeId
    when (page) {
        "edit" -> TenantEditScreen(tenant, saving, onBack) { name, phone, photo, remove ->
            mutate("Data penghuni berhasil disimpan.") {
                api.updateTenant(tenant.id, name, phone, null, null, null)
                if (remove) runCatching { api.deletePhoto("tenant", tenant.id).getOrThrow() }
                if (photo != null) {
                    runCatching { api.uploadPhoto("tenant", tenant.id, photo) }
                    MediaPipeline.cleanup(photo)
                }
                onSaved()
            }
        }
        "extend" -> TenantExtendScreen(tenant, roomTypeId, snapshot.categories.filter { it.kind == "income" }.map { it.name }, api, saving, onBack) { moveOut, rent, paid, status, method, category, dt, dv ->
            mutate("Masa sewa berhasil diperpanjang.") {
                api.extendTenant(tenant.id, moveOut.toString(), rent, paid, status, method, category, dt, dv).getOrThrow()
                onSaved()
            }
        }
        "move" -> TenantMoveScreen(tenant, snapshot, saving, onBack) { roomId ->
            mutate("Penghuni berhasil dipindahkan.") {
                api.moveTenant(tenant.id, roomId).getOrThrow()
                onSaved()
            }
        }
        "dates" -> TenantDatesScreen(tenant, saving, onBack) { moveIn, moveOut ->
            mutate("Tanggal sewa berhasil diperbarui.") {
                api.updateTenant(
                    tenant.id, tenant.name, tenant.phone, null, null, null,
                    moveInDate = moveIn.toString(),
                    moveOutDate = moveOut?.toString() ?: ""
                )
                onSaved()
            }
        }
        "exit" -> TenantExitScreen(tenant, room?.name, saving, onBack) { date, roomStatus ->
            mutate("Penghuni ditandai keluar dan diarsipkan.") {
                api.moveOut(tenant.id, date.toString(), roomStatus).getOrThrow()
                onSaved()
            }
        }
    }
}
