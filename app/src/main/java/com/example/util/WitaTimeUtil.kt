package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

object WitaTimeUtil {
  private val WITA_TIME_ZONE = TimeZone.getTimeZone("Asia/Makassar")

  fun getCurrentWitaFormatted(): String {
    val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss 'WITA'", Locale("id", "ID")).apply {
      timeZone = WITA_TIME_ZONE
    }
    return sdf.format(Date())
  }

  fun getCurrentWitaTimeOnly(): String {
    val sdf = SimpleDateFormat("HH:mm:ss 'WITA'", Locale("id", "ID")).apply {
      timeZone = WITA_TIME_ZONE
    }
    return sdf.format(Date())
  }

  fun getCurrentWitaDayAndDate(): String {
    val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).apply {
      timeZone = WITA_TIME_ZONE
    }
    return sdf.format(Date())
  }

  fun formatCustomWitaTimeOnly(timeMillis: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss 'WITA'", Locale("id", "ID")).apply {
      timeZone = WITA_TIME_ZONE
    }
    return sdf.format(Date(timeMillis))
  }

  fun formatCustomWitaDayAndDate(timeMillis: Long): String {
    val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).apply {
      timeZone = WITA_TIME_ZONE
    }
    return sdf.format(Date(timeMillis))
  }

  fun formatCustomWitaFormatted(timeMillis: Long): String {
    val sdf = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss 'WITA'", Locale("id", "ID")).apply {
      timeZone = WITA_TIME_ZONE
    }
    return sdf.format(Date(timeMillis))
  }

  fun formatWita(timeMillis: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm 'WITA'", Locale("id", "ID")).apply {
      timeZone = WITA_TIME_ZONE
    }
    return sdf.format(Date(timeMillis))
  }

  fun formatDateOnly(timeMillis: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID")).apply {
      timeZone = WITA_TIME_ZONE
    }
    return sdf.format(Date(timeMillis))
  }

  fun formatFilenameTimestamp(timeMillis: Long): String {
    val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).apply {
      timeZone = WITA_TIME_ZONE
    }
    return sdf.format(Date(timeMillis))
  }

  fun toDmsFormat(latitude: Double, longitude: Double): String {
    fun convert(coord: Double, isLat: Boolean): String {
      val absVal = abs(coord)
      val degrees = absVal.toInt()
      val minutesNotTruncated = (absVal - degrees) * 60.0
      val minutes = minutesNotTruncated.toInt()
      val seconds = ((minutesNotTruncated - minutes) * 60.0).toInt()
      val direction = if (isLat) {
        if (coord >= 0) "N" else "S"
      } else {
        if (coord >= 0) "E" else "W"
      }
      return "%d°%02d'%02d\"%s".format(Locale.US, degrees, minutes, seconds, direction)
    }

    return "${convert(latitude, true)} ${convert(longitude, false)}"
  }

  fun formatWitaHourMinute(timeMillis: Long? = null): String {
    val sdf = SimpleDateFormat("HH:mm", Locale("id", "ID")).apply {
      timeZone = WITA_TIME_ZONE
    }
    return if (timeMillis != null) sdf.format(Date(timeMillis)) else sdf.format(Date())
  }

  fun formatLatLongDecimal(latitude: Double, longitude: Double): String {
    val latDir = if (latitude >= 0) "N" else "S"
    val lngDir = if (longitude >= 0) "E" else "W"
    val latStr = String.format(Locale.US, "%.6f°%s", kotlin.math.abs(latitude), latDir)
    val lngStr = String.format(Locale.US, "%.6f°%s", kotlin.math.abs(longitude), lngDir)
    return "$latStr, $lngStr"
  }

  fun formatTimemarkDayDate(timeMillis: Long? = null): String {
    val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")).apply {
      timeZone = WITA_TIME_ZONE
    }
    val raw = if (timeMillis != null) sdf.format(Date(timeMillis)) else sdf.format(Date())
    // Capitalize each word properly (e.g. Senin, 02 November 2026)
    return raw.split(" ").joinToString(" ") { word ->
      word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("id", "ID")) else it.toString() }
    }
  }

  fun generateTimemarkAuditCode(seed: String): String {
    val hash = seed.hashCode().toUInt().toString(36).uppercase().padStart(8, '0')
    return "URY" + hash.take(8) + "YLXT"
  }

  fun formatCategoryTag(kategori: String): String {
    val clean = kategori.trim()
    if (clean.isBlank()) return "Absensi"
    val mapped = when {
      clean.startsWith("Sebelum", ignoreCase = true) -> "Sebelum"
      clean.startsWith("Proses", ignoreCase = true) -> "Proses"
      clean.startsWith("Sesudah", ignoreCase = true) -> "Sesudah"
      clean.startsWith("Nota", ignoreCase = true) -> "Nota"
      clean.startsWith("Fisik Barang", ignoreCase = true) -> "Barang"
      clean.startsWith("Berita Acara", ignoreCase = true) -> "Serah Terima"
      clean.startsWith("Absensi", ignoreCase = true) -> "Absensi"
      else -> clean
    }
    return if (mapped.length > 15) mapped.take(13) + ".." else mapped
  }
}
