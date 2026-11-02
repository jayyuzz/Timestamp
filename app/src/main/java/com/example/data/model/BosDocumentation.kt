package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PhotoCategory(val label: String, val badgeColorHex: Long) {
  SEBELUM("Sebelum (0%)", 0xFF64748B),
  PROSES("Proses (50%)", 0xFF0284C7),
  SESUDAH("Sesudah (100%)", 0xFF10B981),
  BARANG("Fisik Barang", 0xFF8B5CF6),
  NOTA("Nota / Faktur", 0xFFF59E0B),
  SERAH_TERIMA("Berita Acara", 0xFF0D9488)
}

enum class SyncStatus(val label: String) {
  QUEUED("Menunggu Sinkron"),
  SYNCED("Tersinkronisasi"),
  FAILED("Gagal Sinkron")
}

@Entity(tableName = "bos_documentations")
data class BosDocumentation(
  @PrimaryKey val id: String,
  val nomorDokumentasi: String,
  val activityId: String,
  val activityName: String,
  val komponenBos: String,
  val category: String,
  val officerName: String,
  val officerRole: String,
  val witaTimestampMillis: Long,
  val witaFormattedDate: String,
  val latitude: Double,
  val longitude: Double,
  val addressText: String,
  val photoFilePath: String,
  val sha256Hash: String,
  val syncStatus: String = SyncStatus.QUEUED.name,
  val catatan: String = "",
  val createdAt: Long = System.currentTimeMillis()
)
