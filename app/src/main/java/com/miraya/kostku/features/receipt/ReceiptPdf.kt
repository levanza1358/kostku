package com.miraya.kostku.features.receipt

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.miraya.kostku.*
import java.io.File

/**
 * Membuat berkas PDF kwitansi memakai `android.graphics.pdf.PdfDocument` bawaan Android
 * (tanpa pustaka tambahan). Ukuran A4 (595×842 pt). Berkas disimpan di cache sehingga
 * dapat dibagikan lewat [FileProvider].
 */
internal object ReceiptPdf {
    private const val W = 595
    private const val H = 842

    fun write(context: Context, payment: RemotePayment, tenant: RemoteTenant?, room: RemoteRoom?, location: RemoteLocation?): File {
        val doc = PdfDocument()
        val page = doc.startPage(PdfDocument.PageInfo.Builder(W, H, 1).create())
        val canvas: Canvas = page.canvas
        canvas.drawColor(Color.WHITE)

        val title = Paint().apply { isAntiAlias = true; color = Color.BLACK; textSize = 22f; isFakeBoldText = true }
        val sub = Paint().apply { isAntiAlias = true; color = Color.DKGRAY; textSize = 12f }
        val label = Paint().apply { isAntiAlias = true; color = Color.DKGRAY; textSize = 13f }
        val value = Paint().apply { isAntiAlias = true; color = Color.BLACK; textSize = 13f }
        val total = Paint().apply { isAntiAlias = true; color = Color.BLACK; textSize = 20f; isFakeBoldText = true }
        val stamp = Paint().apply { isAntiAlias = true; color = Color.BLACK; textSize = 16f; isFakeBoldText = true }
        val dash = Paint().apply { isAntiAlias = true; color = Color.LTGRAY; strokeWidth = 1.5f }

        var y = 90f
        val left = 60f
        val right = W - 60f
        val center = W / 2f

        canvas.drawText(location?.name ?: "Kos", center - title.measureText(location?.name ?: "Kos") / 2f, y, title); y += 20f
        val subtitle = listOfNotNull(location?.branch, location?.address).filter(String::isNotBlank).joinToString(" • ")
        if (subtitle.isNotBlank()) { canvas.drawText(subtitle, center - sub.measureText(subtitle) / 2f, y, sub) }
        y += 30f

        fun dashed() {
            dash.pathEffect = android.graphics.DashPathEffect(floatArrayOf(8f, 6f), 0f)
            canvas.drawLine(left, y, right, y, dash); y += 28f
        }
        fun kv(k: String, v: String) {
            canvas.drawText(k, left, y, label)
            canvas.drawText(v, right - value.measureText(v), y, value)
            y += 22f
        }
        dashed()
        kv("No. kwitansi", receiptNumber(payment.id, payment.paidAt ?: payment.createdAt))
        kv("Penghuni", tenant?.name ?: "-")
        kv("Kamar", room?.name?.let { "Kamar $it" } ?: "-")
        kv("Kategori", payment.category?.takeIf { it.isNotBlank() } ?: "Sewa")
        kv("Periode", formatDate(payment.period))
        kv("Metode", paymentMethodLabel(payment.method))
        payment.paidAt?.take(10)?.takeIf { it.isNotBlank() }?.let { kv("Tanggal bayar", formatDate(it)) }
        dashed()
        canvas.drawText("TOTAL DIBAYAR", left, y, label)
        val amountText = rupiah(payment.amount)
        canvas.drawText(amountText, right - total.measureText(amountText), y + 4f, total)
        y += 46f
        canvas.drawText(receiptStatusLabel(payment.status), left, y, stamp)
        y += 40f
        (payment.paidAt ?: payment.createdAt)?.take(10)?.takeIf { it.isNotBlank() }?.let { canvas.drawText("Dibuat ${formatDate(it)}", left, y, sub); y += 20f }
        canvas.drawText("Terima kasih telah membayar tepat waktu.", left, y, sub)

        doc.finishPage(page)
        val file = File(context.cacheDir, "kwitansi_${payment.id}.pdf")
        runCatching { file.outputStream().use { doc.writeTo(it) } }
        doc.close()
        require(file.length() > 0) { "PDF kosong" }
        return file
    }
}
