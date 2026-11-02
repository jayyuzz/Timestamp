package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bos_activities")
data class BosActivity(
  @PrimaryKey val id: String,
  val kodeKegiatan: String,
  val namaKegiatan: String,
  val komponenBos: String,
  val alokasiDana: Long,
  val tahunAnggaran: String = "2026",
  val deskripsi: String = "",
  val status: String = "Aktif",
  val createdAt: Long = System.currentTimeMillis()
)
