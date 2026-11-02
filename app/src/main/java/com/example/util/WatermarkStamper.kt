package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class WatermarkData(
  val nomorDokumentasi: String,
  val namaKegiatan: String,
  val komponenBos: String,
  val kategoriFoto: String,
  val namaPetugas: String,
  val peranPetugas: String,
  val witaTimestamp: String,
  val latitude: Double,
  val longitude: Double,
  val alamat: String,
  val customTimestampMillis: Long? = null,
  val namaSekolah: String = "SMK NEGERI 3 KOTAMOBAGU"
)

data class WatermarkResult(
  val stampedFile: File,
  val sha256Hash: String,
  val stampedBitmap: Bitmap
)

object WatermarkStamper {

  private val WITA_TZ = TimeZone.getTimeZone("Asia/Makassar")

  suspend fun stampWatermark(
    context: Context,
    originalBitmap: Bitmap,
    data: WatermarkData,
    outputDirectory: File
  ): WatermarkResult = withContext(Dispatchers.Default) {
    val width = originalBitmap.width
    val height = originalBitmap.height
    val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(mutableBitmap)

    // Scale factor reduced by 1/4 (25% reduction as requested, perfectly balanced)
    val scale = (width / 360f).coerceIn(1.8f, 6.0f)

    // Timestamps
    val now = if (data.customTimestampMillis != null) Date(data.customTimestampMillis) else Date()
    val sdfTime = SimpleDateFormat("HH:mm", Locale("id", "ID")).apply { timeZone = WITA_TZ }
    val timeOnlyText = sdfTime.format(now)
    val fullDateText = WitaTimeUtil.formatTimemarkDayDate(now.time)
    val coordinatesText = WitaTimeUtil.formatLatLongDecimal(data.latitude, data.longitude)
    val auditCode = WitaTimeUtil.generateTimemarkAuditCode(data.nomorDokumentasi + "_" + now.time)

    // =========================================================================
    // 1. TOP-LEFT: MINI MAP BOX (Google Maps with Blue Pin)
    // =========================================================================
    val mapCardSize = 100f * scale
    val mapCardLeft = 20f * scale
    val mapCardTop = 22f * scale
    val mapCardRight = mapCardLeft + mapCardSize
    val mapCardBottom = mapCardTop + mapCardSize
    val mapCornerRadius = 14f * scale

    // Card white background and shadow
    val cardRect = RectF(mapCardLeft, mapCardTop, mapCardRight, mapCardBottom)
    val cardPaint = Paint().apply {
      color = Color.WHITE
      style = Paint.Style.FILL
      isAntiAlias = true
      setShadowLayer(8f * scale, 0f, 3f * scale, Color.parseColor("#40000000"))
    }
    canvas.drawRoundRect(cardRect, mapCornerRadius, mapCornerRadius, cardPaint)

    // Clip canvas to rounded map box
    canvas.save()
    val clipPath = Path().apply {
      addRoundRect(cardRect, mapCornerRadius, mapCornerRadius, Path.Direction.CW)
    }
    canvas.clipPath(clipPath)

    // Map background terrain (#EBF2F7)
    val terrainPaint = Paint().apply {
      color = Color.parseColor("#EBF2F7")
      style = Paint.Style.FILL
    }
    canvas.drawRect(cardRect, terrainPaint)

    // Green zone / park area
    val greenPath = Path().apply {
      moveTo(mapCardLeft + (15f * scale), mapCardTop + (48f * scale))
      lineTo(mapCardLeft + (42f * scale), mapCardTop + (42f * scale))
      lineTo(mapCardLeft + (34f * scale), mapCardTop + (75f * scale))
      lineTo(mapCardLeft + (12f * scale), mapCardTop + (68f * scale))
      close()
    }
    val greenPaint = Paint().apply {
      color = Color.parseColor("#A7F3D0")
      style = Paint.Style.FILL
      isAntiAlias = true
    }
    canvas.drawPath(greenPath, greenPaint)

    // Water river lines (light blue #90CAF9)
    val waterPaint = Paint().apply {
      color = Color.parseColor("#93C5FD")
      strokeWidth = 3f * scale
      style = Paint.Style.STROKE
      isAntiAlias = true
    }
    val riverPath = Path().apply {
      moveTo(mapCardLeft + (20f * scale), mapCardTop)
      cubicTo(
        mapCardLeft + (32f * scale), mapCardTop + (25f * scale),
        mapCardLeft + (60f * scale), mapCardTop + (35f * scale),
        mapCardRight, mapCardTop + (45f * scale)
      )
    }
    canvas.drawPath(riverPath, waterPaint)

    // Road network lines (white with light outline)
    val roadOutlinePaint = Paint().apply {
      color = Color.parseColor("#CBD5E1")
      strokeWidth = 5f * scale
      style = Paint.Style.STROKE
      isAntiAlias = true
    }
    val roadMainPaint = Paint().apply {
      color = Color.WHITE
      strokeWidth = 3.5f * scale
      style = Paint.Style.STROKE
      isAntiAlias = true
    }

    // Diagonal main road
    val roadPath1 = Path().apply {
      moveTo(mapCardLeft, mapCardTop + (65f * scale))
      lineTo(mapCardRight, mapCardTop + (25f * scale))
    }
    canvas.drawPath(roadPath1, roadOutlinePaint)
    canvas.drawPath(roadPath1, roadMainPaint)

    // Secondary vertical & cross roads
    val roadPath2 = Path().apply {
      moveTo(mapCardLeft + (50f * scale), mapCardTop)
      lineTo(mapCardLeft + (60f * scale), mapCardBottom)
    }
    canvas.drawPath(roadPath2, roadOutlinePaint)
    canvas.drawPath(roadPath2, roadMainPaint)

    // Subtle area label
    val mapLabelPaint = Paint().apply {
      color = Color.parseColor("#64748B")
      textSize = 8f * scale
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      isAntiAlias = true
    }
    canvas.drawText("Kotamobagu", mapCardLeft + (5f * scale), mapCardTop + (55f * scale), mapLabelPaint)

    // Google text mark at bottom-left corner of the map
    val googlePaint = Paint().apply {
      color = Color.parseColor("#4285F4")
      textSize = 9f * scale
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      isAntiAlias = true
    }
    canvas.drawText("Google", mapCardLeft + (8f * scale), mapCardBottom - (7f * scale), googlePaint)

    // Blue Map Location Pin Marker (teardrop) in center
    val pinCenterX = mapCardLeft + (mapCardSize * 0.52f)
    val pinCenterY = mapCardTop + (mapCardSize * 0.42f)

    // Pin drop shadow
    val shadowPaint = Paint().apply {
      color = Color.parseColor("#33000000")
      style = Paint.Style.FILL
      isAntiAlias = true
    }
    canvas.drawOval(
      RectF(pinCenterX - (5f * scale), pinCenterY + (9f * scale), pinCenterX + (5f * scale), pinCenterY + (13f * scale)),
      shadowPaint
    )

    // Pin teardrop body (#1A73E8)
    val pinRadius = 8f * scale
    val pinPaint = Paint().apply {
      color = Color.parseColor("#1A73E8")
      style = Paint.Style.FILL
      isAntiAlias = true
    }
    canvas.drawCircle(pinCenterX, pinCenterY, pinRadius, pinPaint)

    val pinPointer = Path().apply {
      moveTo(pinCenterX - (pinRadius * 0.85f), pinCenterY + (pinRadius * 0.4f))
      lineTo(pinCenterX + (pinRadius * 0.85f), pinCenterY + (pinRadius * 0.4f))
      lineTo(pinCenterX, pinCenterY + (11f * scale))
      close()
    }
    canvas.drawPath(pinPointer, pinPaint)

    // Pin inner white dot
    val dotPaint = Paint().apply {
      color = Color.WHITE
      style = Paint.Style.FILL
      isAntiAlias = true
    }
    canvas.drawCircle(pinCenterX, pinCenterY, 3.5f * scale, dotPaint)

    canvas.restore() // Restore after map clip

    // =========================================================================
    // 2. TOP-RIGHT: TIMEMARK BRANDING & ACCURACY SEAL
    // =========================================================================
    val brandRight = width - (20f * scale)
    val timemarkPaint = Paint().apply {
      color = Color.parseColor("#FFA000") // Warm amber/orange
      textSize = 23f * scale
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      textAlign = Paint.Align.RIGHT
      isAntiAlias = true
      setShadowLayer(4f * scale, 0f, 1f * scale, Color.parseColor("#80000000"))
    }
    canvas.drawText("Timemark", brandRight, mapCardTop + (22f * scale), timemarkPaint)

    val subtitlePaint = Paint().apply {
      color = Color.parseColor("#E2E8F0")
      textSize = 12.5f * scale
      typeface = Typeface.DEFAULT
      textAlign = Paint.Align.RIGHT
      isAntiAlias = true
      setShadowLayer(3f * scale, 0f, 1f * scale, Color.parseColor("#80000000"))
    }
    canvas.drawText("Foto 100% akurat", brandRight, mapCardTop + (38f * scale), subtitlePaint)

    // =========================================================================
    // 3. RIGHT EDGE: VERTICAL ROTATED VERIFIED CODE
    // =========================================================================
    val vertPaint = Paint().apply {
      color = Color.parseColor("#A6FFFFFF")
      textSize = 10f * scale
      typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
      isAntiAlias = true
      setShadowLayer(2f * scale, 0f, 1f * scale, Color.BLACK)
    }
    val vertText = "© $auditCode  Timemark Verified"
    canvas.save()
    val vertX = width - (8f * scale)
    val vertY = height * 0.52f
    canvas.rotate(-90f, vertX, vertY)
    canvas.drawText(vertText, vertX, vertY, vertPaint)
    canvas.restore()

    // =========================================================================
    // 4. BOTTOM-LEFT: MAIN STAMP (TIME PILL, YELLOW BAR, DATE, ADDRESS, COORDS)
    // =========================================================================
    val bottomMargin = 28f * scale
    val leftMargin = 20f * scale

    // Measure texts to layout from bottom up
    val footerText = "🛡 Kode Foto: $auditCode"
    val footerPaint = Paint().apply {
      color = Color.parseColor("#CFD8DC")
      textSize = 12f * scale
      typeface = Typeface.DEFAULT
      isAntiAlias = true
      setShadowLayer(3f * scale, 0f, 1f * scale, Color.BLACK)
    }

    val coordPaint = Paint().apply {
      color = Color.WHITE
      textSize = 15f * scale
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      isAntiAlias = true
      setShadowLayer(4f * scale, 0f, 1f * scale, Color.BLACK)
    }

    val addrPaint = Paint().apply {
      color = Color.WHITE
      textSize = 14.5f * scale
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
      isAntiAlias = true
      setShadowLayer(4f * scale, 0f, 1f * scale, Color.BLACK)
    }

    val datePaint = Paint().apply {
      color = Color.WHITE
      textSize = 18f * scale
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      isAntiAlias = true
      setShadowLayer(4f * scale, 0f, 1f * scale, Color.BLACK)
    }

    // Split address into 2 clean lines if long
    val rawAddress = if (data.alamat.isNotBlank()) data.alamat else "Molinow, Kec. Kotamobagu Bar., Kota Kotamobagu, Sulawesi Utara"
    val addressLines = if (rawAddress.contains(",")) {
      val parts = rawAddress.split(",").map { it.trim() }
      if (parts.size >= 3) {
        val line1 = parts.take(parts.size / 2).joinToString(", ") + ","
        val line2 = parts.drop(parts.size / 2).joinToString(", ")
        listOf(line1, line2)
      } else {
        listOf(rawAddress)
      }
    } else {
      listOf(rawAddress)
    }

    // Calculate Y positions from bottom
    val footerY = height - bottomMargin
    val coordsY = footerY - (22f * scale)

    var currentAddrY = coordsY - (20f * scale)
    val addrPositions = mutableListOf<Float>()
    for (i in addressLines.indices.reversed()) {
      addrPositions.add(0, currentAddrY)
      currentAddrY -= (19f * scale)
    }

    val dateY = currentAddrY - (4f * scale)
    val barTop = dateY - (17f * scale)
    val barBottom = coordsY + (3f * scale)

    // Yellow Accent Vertical Bar on the Left
    val barWidth = 4.5f * scale
    val barX = leftMargin
    val yellowBarPaint = Paint().apply {
      color = Color.parseColor("#FFB300") // Vibrant golden yellow
      style = Paint.Style.FILL
      isAntiAlias = true
    }
    canvas.drawRoundRect(
      RectF(barX, barTop, barX + barWidth, barBottom),
      barWidth / 2f,
      barWidth / 2f,
      yellowBarPaint
    )

    // Draw Date, Address, Coordinates (indented after yellow bar)
    val textStartX = barX + barWidth + (10f * scale)
    canvas.drawText(fullDateText, textStartX, dateY, datePaint)

    for (i in addressLines.indices) {
      canvas.drawText(addressLines[i], textStartX, addrPositions[i], addrPaint)
    }

    canvas.drawText(coordinatesText, textStartX, coordsY, coordPaint)

    // Draw Footer Line
    canvas.drawText(footerText, barX, footerY, footerPaint)

    // =========================================================================
    // 5. TIME PILL BADGE (Directly above the Date/Yellow Bar)
    // =========================================================================
    val pillBottom = barTop - (14f * scale)
    val pillHeight = 44f * scale
    val pillTop = pillBottom - pillHeight
    val pillCorner = 10f * scale

    // Pill texts (supports Sebelum, Proses, Sesudah, Nota, Barang, Absensi, or custom categories)
    val tagText = WitaTimeUtil.formatCategoryTag(data.kategoriFoto)

    val tagTextPaint = Paint().apply {
      color = Color.BLACK
      textSize = 15f * scale
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      isAntiAlias = true
    }
    val tagTextWidth = tagTextPaint.measureText(tagText)

    val timeTextPaint = Paint().apply {
      color = Color.parseColor("#0F172A") // Deep slate navy
      textSize = 34f * scale
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      isAntiAlias = true
    }
    val timeTextWidth = timeTextPaint.measureText(timeOnlyText)

    val tagPaddingHoriz = 12f * scale
    val tagWidth = tagTextWidth + (tagPaddingHoriz * 2)
    val tagHeight = pillHeight - (8f * scale)

    val pillWidth = tagWidth + timeTextWidth + (22f * scale)
    val pillLeft = leftMargin
    val pillRight = pillLeft + pillWidth

    // Outer white container
    val pillBgPaint = Paint().apply {
      color = Color.WHITE
      style = Paint.Style.FILL
      isAntiAlias = true
      setShadowLayer(6f * scale, 0f, 2f * scale, Color.parseColor("#50000000"))
    }
    canvas.drawRoundRect(RectF(pillLeft, pillTop, pillRight, pillBottom), pillCorner, pillCorner, pillBgPaint)

    // Left inner tag (Golden yellow rounded rectangle)
    val tagLeft = pillLeft + (4f * scale)
    val tagTop = pillTop + (4f * scale)
    val tagRight = tagLeft + tagWidth
    val tagBottom = tagTop + tagHeight
    val tagBgPaint = Paint().apply {
      color = Color.parseColor("#FFB300")
      style = Paint.Style.FILL
      isAntiAlias = true
    }
    canvas.drawRoundRect(RectF(tagLeft, tagTop, tagRight, tagBottom), 7f * scale, 7f * scale, tagBgPaint)

    // Tag text centered
    val tagTextY = tagTop + (tagHeight / 2f) + (tagTextPaint.textSize * 0.35f)
    canvas.drawText(tagText, tagLeft + tagPaddingHoriz, tagTextY, tagTextPaint)

    // Big digital time text
    val timeTextX = tagRight + (8f * scale)
    val timeTextY = pillTop + (pillHeight / 2f) + (timeTextPaint.textSize * 0.34f)
    canvas.drawText(timeOnlyText, timeTextX, timeTextY, timeTextPaint)

    // =========================================================================
    // 6. SAVE TO DISK & COMPUTE SHA-256 HASH
    // =========================================================================
    val filename = "BOS_SMKN3_${WitaTimeUtil.formatFilenameTimestamp(System.currentTimeMillis())}.jpg"
    val stampedFile = File(outputDirectory, filename)

    FileOutputStream(stampedFile).use { out ->
      mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
    }

    val realSha256 = HashUtil.calculateFileSha256(stampedFile)

    WatermarkResult(
      stampedFile = stampedFile,
      sha256Hash = realSha256,
      stampedBitmap = mutableBitmap
    )
  }
}
