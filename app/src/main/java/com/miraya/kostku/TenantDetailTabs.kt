package com.miraya.kostku

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.miraya.kostku.features.location.UtilityRow
import com.miraya.kostku.media.ClickableRemotePhoto
import com.miraya.kostku.ui.common.DayLeftChip
import com.miraya.kostku.ui.common.SectionTitle
import com.miraya.kostku.ui.common.StatusBadge
import com.miraya.kostku.ui.theme.Screen
import com.miraya.kostku.ui.theme.Spacing

@Composable
internal fun TenantInformation(tenant: RemoteTenant, room: RemoteRoom?, location: RemoteLocation?) {
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = Screen.edge, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        // Kartu profil
        Card(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(Spacing.lg), horizontalArrangement = Arrangement.spacedBy(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                ClickableRemotePhoto(tenant.photoUrl, Modifier.size(72.dp), "Foto penghuni")
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    Text(tenant.name, style = MaterialTheme.typography.titleLarge, maxLines = 1)
                    DayLeftChip(daysUntil(tenant.moveOut))
                    Text(
                        listOfNotNull(room?.name?.let { "Kamar $it" }, room?.roomTypeName).joinToString(" • ").ifBlank { "Kamar tidak diketahui" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Satu kartu memuat penempatan + utilitas kamar/lokasi (mock 3.3).
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                DetailRow("Nomor HP", tenant.phone.ifBlank { "Tidak diisi" })
                DetailRow("Kamar", room?.name?.let { "Kamar $it" } ?: "Tidak diketahui")
                DetailRow("Tipe kamar", room?.roomTypeName ?: "Tidak diketahui")
                DetailRow("Tanggal masuk", tenant.moveIn.ifBlank { "Tidak diketahui" }.let { formatDate(it) })

                InlineSection("UTILITAS KAMAR")
                UtilityRow("Nomor token listrik", utilityText(room?.tokenListrik))
                UtilityRow("Nomor PDAM", utilityText(room?.pdamNumber))

                InlineSection("UTILITAS LOKASI")
                UtilityRow("Nomor token listrik", utilityText(location?.tokenListrik))
                UtilityRow("Nomor PDAM", utilityText(location?.pdamNumber))
            }
        }
    }
}

/** Judul bagian di dalam kartu (mock memakai huruf besar, tanpa garis pembatas). */
@Composable
private fun InlineSection(title: String) {
    Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Text(value, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
    }
}

@Composable
internal fun TenantPaymentHistory(payments: List<RemotePayment>, onReceipt: (String) -> Unit) {
    if (payments.isEmpty()) {
        EmptyHistory("Belum ada riwayat pembayaran.", Icons.Default.Payments)
    } else {
        LazyColumn(Modifier.fillMaxWidth().padding(Screen.edge), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            items(payments, key = { it.id }) { payment ->
                Card({ onReceipt(payment.id) }, Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text(rupiah(payment.amount), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) },
                        supportingContent = {
                            Text(listOfNotNull("Pemasukan", payment.category, paymentStatusLabel(payment.status), formatDate(payment.period)).joinToString(" • "))
                        },
                        leadingContent = {
                            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape, modifier = Modifier.size(40.dp)) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.TrendingUp, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        },
                        trailingContent = { Icon(Icons.Default.Description, "Kwitansi", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                    )
                }
            }
            // Kartu petunjuk bergaris putus (mock 3.4) menuju kwitansi pembayaran terbaru.
            item {
                val target = payments.first()
                val outlineColor = MaterialTheme.colorScheme.outline
                val hint = buildAnnotatedString {
                    append("Ketuk ikut untuk melihat ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("kwitansi") }
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawRoundRect(
                                color = outlineColor,
                                cornerRadius = CornerRadius(16.dp.toPx()),
                                style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f))
                            )
                        }
                        .clickable { onReceipt(target.id) }
                        .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Icon(Icons.Default.Description, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(hint, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
internal fun TenantChangeLog(tenant: RemoteTenant, api: KostkuApi) {
    val entries by produceState<List<RemoteAuditEntry>?>(initialValue = null, tenant.id) {
        value = api.tenantAudit(tenant.id).getOrNull()
    }
    val list = entries
    when {
        list == null -> Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                CircularProgressIndicator()
                Text("Memuat riwayat perubahan…", style = MaterialTheme.typography.bodySmall)
            }
        }
        list.isEmpty() -> EmptyHistory("Belum ada perubahan tercatat.", Icons.Default.History)
        else -> LazyColumn(Modifier.fillMaxWidth().padding(Screen.edge), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            items(list, key = { it.id }) { entry ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        Text(auditActionLabel(entry.action), style = MaterialTheme.typography.titleSmall)
                        Text(
                            listOfNotNull(auditTimestamp(entry.createdAt), auditDetailText(entry))
                                .joinToString(" — "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHistory(message: String, icon: ImageVector) {
    Column(
        Modifier.fillMaxWidth().padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Icon(icon, null, Modifier.size(44.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
