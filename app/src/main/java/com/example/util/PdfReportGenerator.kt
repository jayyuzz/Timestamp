package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.model.BosActivity
import com.example.data.model.BosDocumentation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

object PdfReportGenerator {

  suspend fun generateActivityReport(
    context: Context,
    activity: BosActivity,
    documentations: List<BosDocumentation>,
    outputDir: File,
    schoolName: String = "SMK NEGERI 3 KOTAMOBAGU"
  ): File = withContext(Dispatchers.IO) {
    val pdfDocument = PdfDocument()
    val pageWidth = 595 // A4 standard width in points
    val pageHeight = 842 // A4 standard height in points

    var pageNumber = 1
    var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
    var currentPage = pdfDocument.startPage(pageInfo)
    var canvas = currentPage.canvas

    // Helper paints
    val textPaint = Paint().apply {
      color = Color.BLACK
      isAntiAlias = true
    }
    val linePaint = Paint().apply {
      color = Color.BLACK
      strokeWidth = 2f
      isAntiAlias = true
    }

    var currentY = 30f

    fun drawKopSurat() {
      // Draw Crest / Logo
      val logoSize = 50
      try {
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_smkn3_logo)
        drawable?.let {
          it.setBounds(30, currentY.toInt(), 30 + logoSize, (currentY + logoSize).toInt())
          it.draw(canvas)
        }
      } catch (_: Exception) {}

      // Header Text
      textPaint.textAlign = Paint.Align.CENTER
      textPaint.textSize = 10f
      textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
      canvas.drawText("PEMERINTAH PROVINSI SULAWESI UTARA", pageWidth / 2f + 15f, currentY + 10f, textPaint)

      textPaint.textSize = 10.5f
      canvas.drawText("DINAS PENDIDIKAN DAERAH - CABANG DINAS KOTAMOBAGU & BMR", pageWidth / 2f + 15f, currentY + 23f, textPaint)

      textPaint.textSize = 13f
      textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      val displaySchool = if (schoolName.isNotBlank()) schoolName.uppercase() else "SMK NEGERI 3 KOTAMOBAGU"
      canvas.drawText(displaySchool, pageWidth / 2f + 15f, currentY + 39f, textPaint)

      textPaint.textSize = 8.5f
      textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
      canvas.drawText("Jl. AKD, Mongkonai, Kec. Kotamobagu Barat, Kota Kotamobagu, Sulawesi Utara 95716", pageWidth / 2f + 15f, currentY + 52f, textPaint)

      currentY += 60f

      // Double divider line
      canvas.drawLine(30f, currentY, pageWidth - 30f, currentY, linePaint.apply { strokeWidth = 2.5f })
      currentY += 4f
      canvas.drawLine(30f, currentY, pageWidth - 30f, currentY, linePaint.apply { strokeWidth = 0.8f })
      currentY += 16f
    }

    // 1. Draw Kop on Page 1
    drawKopSurat()

    // 2. Document Title
    textPaint.textAlign = Paint.Align.CENTER
    textPaint.textSize = 12f
    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("LAPORAN DOKUMENTASI PERTANGGUNGJAWABAN DANA BOS", pageWidth / 2f, currentY, textPaint)
    currentY += 14f

    textPaint.textSize = 10f
    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("Tahun Anggaran: ${activity.tahunAnggaran} | Sistem Verifikasi Kriptografi BOS Cam", pageWidth / 2f, currentY, textPaint)
    currentY += 20f

    // 3. Activity Detail Card / Table
    val cardPaint = Paint().apply {
      color = Color.parseColor("#F1F5F9")
      style = Paint.Style.FILL
    }
    val cardRect = RectF(30f, currentY, pageWidth - 30f, currentY + 70f)
    canvas.drawRoundRect(cardRect, 6f, 6f, cardPaint)

    textPaint.textAlign = Paint.Align.LEFT
    textPaint.textSize = 9.5f
    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("Kode Kegiatan  : ", 42f, currentY + 18f, textPaint)
    canvas.drawText("Nama Kegiatan  : ", 42f, currentY + 34f, textPaint)
    canvas.drawText("Komponen BOS  : ", 42f, currentY + 50f, textPaint)
    canvas.drawText("Alokasi Dana   : ", 42f, currentY + 64f, textPaint)

    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText(activity.kodeKegiatan, 130f, currentY + 18f, textPaint)
    canvas.drawText(activity.namaKegiatan, 130f, currentY + 34f, textPaint)
    val shortKomp = if (activity.komponenBos.length > 60) activity.komponenBos.take(57) + "..." else activity.komponenBos
    canvas.drawText(shortKomp, 130f, currentY + 50f, textPaint)

    val rupiahFmt = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(activity.alokasiDana)
    canvas.drawText("$rupiahFmt (${activity.status})", 130f, currentY + 64f, textPaint)

    currentY += 86f

    // 4. Photos Grid
    textPaint.textSize = 11f
    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("MATRIKS BUKTI FOTO & KEABSAHAN KRIPTOGRAFI SHA-256", 30f, currentY, textPaint)
    currentY += 16f

    // Iterate through documentations (2 photos per page or 2 columns)
    val photosPerPage = 2
    for ((index, doc) in documentations.withIndex()) {
      if (index > 0 && index % photosPerPage == 0) {
        // Finish current page and start a new page
        pdfDocument.finishPage(currentPage)
        pageNumber++
        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        currentPage = pdfDocument.startPage(pageInfo)
        canvas = currentPage.canvas
        currentY = 30f
        drawKopSurat()

        textPaint.textAlign = Paint.Align.LEFT
        textPaint.textSize = 11f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("LANJUTAN BUKTI DOKUMENTASI (Halaman $pageNumber)", 30f, currentY, textPaint)
        currentY += 16f
      }

      // Draw Photo Card Container
      val itemHeight = 220f
      val itemRect = RectF(30f, currentY, pageWidth - 30f, currentY + itemHeight)
      val itemBorderPaint = Paint().apply {
        color = Color.parseColor("#CBD5E1")
        style = Paint.Style.STROKE
        strokeWidth = 1f
      }
      canvas.drawRoundRect(itemRect, 6f, 6f, itemBorderPaint)

      // Draw Photo Image if exists
      val photoFile = File(doc.photoFilePath)
      if (photoFile.exists()) {
        try {
          val bmp = BitmapFactory.decodeFile(photoFile.absolutePath)
          bmp?.let {
            val imgDestRect = RectF(38f, currentY + 8f, 218f, currentY + itemHeight - 8f)
            canvas.drawBitmap(it, null, imgDestRect, Paint(Paint.FILTER_BITMAP_FLAG))
            it.recycle()
          }
        } catch (_: Exception) {}
      } else {
        // Placeholder
        val placeholderPaint = Paint().apply {
          color = Color.parseColor("#E2E8F0")
          style = Paint.Style.FILL
        }
        val pRect = RectF(38f, currentY + 8f, 218f, currentY + itemHeight - 8f)
        canvas.drawRoundRect(pRect, 4f, 4f, placeholderPaint)
      }

      // Meta Info next to photo
      val metaX = 230f
      var metaY = currentY + 22f

      // Category badge
      val badgePaint = Paint().apply {
        color = Color.parseColor("#0F3A5D")
        style = Paint.Style.FILL
      }
      val bRect = RectF(metaX, currentY + 8f, metaX + 110f, currentY + 26f)
      canvas.drawRoundRect(bRect, 4f, 4f, badgePaint)
      textPaint.color = Color.WHITE
      textPaint.textSize = 8.5f
      textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      canvas.drawText(doc.category.uppercase(Locale("id", "ID")), metaX + 8f, currentY + 20f, textPaint)

      // Text items
      textPaint.color = Color.BLACK
      textPaint.textSize = 9f
      textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      canvas.drawText("No. Dokumen: ", metaX, metaY + 20f, textPaint)
      textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
      canvas.drawText(doc.nomorDokumentasi, metaX + 75f, metaY + 20f, textPaint)

      textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      canvas.drawText("Waktu WITA: ", metaX, metaY + 36f, textPaint)
      textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
      canvas.drawText(doc.witaFormattedDate, metaX + 75f, metaY + 36f, textPaint)

      textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      canvas.drawText("Petugas: ", metaX, metaY + 52f, textPaint)
      textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
      canvas.drawText("${doc.officerName} (${doc.officerRole})", metaX + 75f, metaY + 52f, textPaint)

      textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      canvas.drawText("Koordinat GPS: ", metaX, metaY + 68f, textPaint)
      textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
      val gpsText = String.format(Locale.US, "%.5f, %.5f", doc.latitude, doc.longitude)
      canvas.drawText(gpsText, metaX + 75f, metaY + 68f, textPaint)

      textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      canvas.drawText("Alamat: ", metaX, metaY + 84f, textPaint)
      textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
      val shortDocAddr = if (doc.addressText.length > 40) doc.addressText.take(37) + "..." else doc.addressText
      canvas.drawText(shortDocAddr, metaX + 75f, metaY + 84f, textPaint)

      // SHA-256 Checksum block
      textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      canvas.drawText("Keaslian SHA-256 Checksum (Anti-Fraud):", metaX, metaY + 104f, textPaint)
      textPaint.textSize = 7.5f
      textPaint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
      textPaint.color = Color.parseColor("#047857") // Dark green
      val hashLine1 = doc.sha256Hash.take(32)
      val hashLine2 = doc.sha256Hash.drop(32)
      canvas.drawText(hashLine1, metaX, metaY + 118f, textPaint)
      canvas.drawText(hashLine2, metaX, metaY + 130f, textPaint)

      textPaint.color = Color.BLACK
      currentY += itemHeight + 14f
    }

    // 5. Signature Section (Check if enough room, else create new page)
    val signatureHeight = 130f
    if (currentY + signatureHeight > pageHeight - 30f) {
      pdfDocument.finishPage(currentPage)
      pageNumber++
      pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
      currentPage = pdfDocument.startPage(pageInfo)
      canvas = currentPage.canvas
      currentY = 40f
      drawKopSurat()
    }

    currentY += 10f
    textPaint.textAlign = Paint.Align.CENTER
    textPaint.textSize = 9.5f
    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

    val dateStr = "Kotamobagu, ${WitaTimeUtil.formatDateOnly(System.currentTimeMillis())}"
    canvas.drawText(dateStr, pageWidth - 120f, currentY, textPaint)
    currentY += 16f

    // Columns: Bendahara (Left), Kepala Sekolah (Right)
    val colLeftX = 140f
    val colRightX = pageWidth - 140f

    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("Mengetahui,", colLeftX, currentY, textPaint)
    canvas.drawText("Menyetujui,", colRightX, currentY, textPaint)
    currentY += 14f

    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("Bendahara BOS SMKN 3 Kotamobagu", colLeftX, currentY, textPaint)
    canvas.drawText("Kepala SMK Negeri 3 Kotamobagu", colRightX, currentY, textPaint)

    currentY += 55f // Signature space

    canvas.drawLine(colLeftX - 70f, currentY, colLeftX + 70f, currentY, linePaint.apply { strokeWidth = 1f })
    canvas.drawLine(colRightX - 70f, currentY, colRightX + 70f, currentY, linePaint.apply { strokeWidth = 1f })
    currentY += 12f

    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    canvas.drawText("( ................................................... )", colLeftX, currentY, textPaint)
    canvas.drawText("( ................................................... )", colRightX, currentY, textPaint)
    currentY += 12f

    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    canvas.drawText("NIP. 19780512 200501 1 008", colLeftX, currentY, textPaint)
    canvas.drawText("NIP. 19690315 199403 1 004", colRightX, currentY, textPaint)

    pdfDocument.finishPage(currentPage)

    // Save PDF
    val cleanKode = activity.kodeKegiatan.replace("-", "_")
    val pdfFile = File(outputDir, "LPJ_BOS_${cleanKode}_SMKN3.pdf")
    FileOutputStream(pdfFile).use { out ->
      pdfDocument.writeTo(out)
    }
    pdfDocument.close()

    pdfFile
  }
}
