package com.miraya.kostku.features.receipt

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.miraya.kostku.*

// Berbagi kwitansi sebagai teks atau PDF.

internal fun receiptText(payment: RemotePayment, tenant: RemoteTenant?, room: RemoteRoom?, location: RemoteLocation?): String = buildString {
    appendLine("*Kwitansi ${location?.name ?: "Kos"}*")
    location?.let { l -> listOfNotNull(l.branch, l.address).filter(String::isNotBlank).joinToString(" • ").takeIf { it.isNotBlank() }?.let { appendLine(it) } }
    appendLine()
    appendLine("No. kwitansi: ${receiptNumber(payment.id, payment.paidAt ?: payment.createdAt)}")
    appendLine("Penghuni: ${tenant?.name ?: "-"}")
    appendLine("Kamar: ${room?.name?.let { "Kamar $it" } ?: "-"}")
    appendLine("Kategori: ${payment.category?.takeIf { it.isNotBlank() } ?: "Sewa"}")
    appendLine("Periode: ${formatDate(payment.period)}")
    appendLine("Metode: ${paymentMethodLabel(payment.method)}")
    payment.paidAt?.take(10)?.takeIf { it.isNotBlank() }?.let { appendLine("Tanggal bayar: ${formatDate(it)}") }
    appendLine("Total dibayar: ${rupiah(payment.amount)}")
    appendLine("Status: ${receiptStatusLabel(payment.status)}")
    (payment.paidAt ?: payment.createdAt)?.take(10)?.takeIf { it.isNotBlank() }?.let { appendLine("Dibuat ${formatDate(it)}") }
    append("Terima kasih telah membayar tepat waktu.")
}

internal fun shareText(context: Context, payment: RemotePayment, tenant: RemoteTenant?, room: RemoteRoom?, location: RemoteLocation?) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Kwitansi ${tenant?.name ?: ""}".trim())
        putExtra(Intent.EXTRA_TEXT, receiptText(payment, tenant, room, location))
    }
    runCatching { context.startActivity(Intent.createChooser(intent, "Bagikan kwitansi")) }
}

internal fun sharePdf(context: Context, payment: RemotePayment, tenant: RemoteTenant?, room: RemoteRoom?, location: RemoteLocation?) {
    val file = runCatching { ReceiptPdf.write(context, payment, tenant, room, location) }.getOrNull()
    if (file == null) { shareText(context, payment, tenant, room, location); return }
    val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching { context.startActivity(Intent.createChooser(intent, "Bagikan kwitansi")) }
        .onFailure { shareText(context, payment, tenant, room, location) }
}
