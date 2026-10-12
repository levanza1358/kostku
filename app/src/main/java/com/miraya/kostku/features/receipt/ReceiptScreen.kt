package com.miraya.kostku.features.receipt

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.miraya.kostku.*
import com.miraya.kostku.media.RemotePhotoPreview
import com.miraya.kostku.ui.common.AppTopBar
import com.miraya.kostku.ui.common.statusColor
import com.miraya.kostku.ui.theme.Dimens
import com.miraya.kostku.ui.theme.Screen
import com.miraya.kostku.ui.theme.Spacing

/**
 * Layar kwitansi: diturunkan dari satu catatan [payment] (tidak ada endpoint baru).
 * Header memakai logo lokasi bila ada, atau ikon rumah sebagai cadangan. Cap status hanya
 * "LUNAS" bila pembayaran benar-benar `paid`; selainnya "SEBAGIAN"/"BELUM LUNAS".
 */
@Composable
internal fun ReceiptScreen(
    payment: RemotePayment,
    tenant: RemoteTenant?,
    room: RemoteRoom?,
    location: RemoteLocation?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val statusLabel = receiptStatusLabel(payment.status)
    val paid = payment.status.lowercase() == "paid"
    val stampColor = statusColor(payment.status)
    val brand = location?.name ?: "Kos"
    val subtitle = listOfNotNull(location?.branch, location?.address).filter(String::isNotBlank).joinToString(" • ")
    val period = formatDate(payment.period)

    Scaffold(
        topBar = { AppTopBar("Kwitansi", onBack = onBack) },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(Modifier.fillMaxWidth().padding(horizontal = Screen.edge, vertical = Spacing.md), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    OutlinedButton(
                        onClick = { shareText(context, payment, tenant, room, location) },
                        modifier = Modifier.weight(1f).height(Dimens.ButtonHeight),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(Icons.Default.Share, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(Spacing.sm))
                        Text("Bagikan")
                    }
                    Button(
                        onClick = { sharePdf(context, payment, tenant, room, location) },
                        modifier = Modifier.weight(1f).height(Dimens.ButtonHeight),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Icon(Icons.Default.PictureAsPdf, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(Spacing.sm))
                        Text("PDF")
                    }
                }
            }
        }
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = Screen.edge, vertical = Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(Spacing.xl), horizontalAlignment = Alignment.CenterHorizontally) {
                    // Header merek: logo lokasi atau ikon rumah.
                    if (location?.logoUrl.isNullOrBlank()) {
                        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium, modifier = Modifier.size(52.dp)) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Default.Home, "Logo lokasi", tint = MaterialTheme.colorScheme.onPrimaryContainer) }
                        }
                    } else {
                        RemotePhotoPreview(location?.logoUrl, Modifier.size(52.dp), "Logo lokasi", shape = MaterialTheme.shapes.medium)
                    }
                    Spacer(Modifier.height(Spacing.sm))
                    Text(brand, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
                    if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)

                    DashedDivider(Modifier.padding(vertical = Spacing.lg))

                    ReceiptRow("No. kwitansi", receiptNumber(payment.id, payment.paidAt ?: payment.createdAt))
                    ReceiptRow("Penghuni", tenant?.name ?: "Penghuni")
                    ReceiptRow("Kamar", room?.name?.let { "Kamar $it" } ?: "Tidak diketahui")
                    ReceiptRow("Kategori", payment.category?.takeIf { it.isNotBlank() } ?: "Sewa")
                    ReceiptRow("Periode", period)
                    ReceiptRow("Metode", paymentMethodLabel(payment.method))
                    payment.paidAt?.take(10)?.takeIf { it.isNotBlank() }?.let { ReceiptRow("Tanggal bayar", formatDate(it)) }

                    DashedDivider(Modifier.padding(vertical = Spacing.lg))

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Text("TOTAL DIBAYAR", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(rupiah(payment.amount), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.height(Spacing.md))
                    // Cap status + tanggal pembuatan sejajar (mock 5.6).
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            statusLabel,
                            modifier = Modifier
                                .border(1.5.dp, stampColor, MaterialTheme.shapes.small)
                                .padding(horizontal = Spacing.md, vertical = Spacing.xs),
                            style = MaterialTheme.typography.titleSmall,
                            color = stampColor,
                            fontWeight = FontWeight.Bold
                        )
                        val createdRaw = (payment.paidAt ?: payment.createdAt)?.take(10)?.takeIf { it.isNotBlank() }
                        createdRaw?.let {
                            Text("Dibuat ${formatDate(it)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    DashedDivider(Modifier.padding(vertical = Spacing.lg))
                    Text(
                        "Terima kasih telah membayar tepat waktu.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = Spacing.xs), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(Spacing.md))
        Text(value, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.End)
    }
}

/** Garis putus-putus seperti sobekan kwitansi. */
@Composable
private fun DashedDivider(modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.outlineVariant
    androidx.compose.foundation.Canvas(modifier.fillMaxWidth().height(1.dp)) {
        drawLine(
            color = color,
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = size.height,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
        )
    }
}
