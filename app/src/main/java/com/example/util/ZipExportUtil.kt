package com.example.util

import com.example.data.model.BosActivity
import com.example.data.model.BosDocumentation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ZipExportUtil {

  suspend fun exportActivityToZip(
    activity: BosActivity,
    documentations: List<BosDocumentation>,
    outputDir: File
  ): File = withContext(Dispatchers.IO) {
    val cleanKode = activity.kodeKegiatan.replace("-", "_")
    val zipFile = File(outputDir, "Arsip_Foto_BOS_${cleanKode}.zip")

    ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
      // 1. Write Manifest Info Text File
      val manifestSb = StringBuilder()
      manifestSb.append("ARSIP DOKUMENTASI DANA BOS SMK NEGERI 3 KOTAMOBAGU\n")
      manifestSb.append("====================================================\n")
      manifestSb.append("Kode Kegiatan  : ${activity.kodeKegiatan}\n")
      manifestSb.append("Nama Kegiatan  : ${activity.namaKegiatan}\n")
      manifestSb.append("Komponen BOS   : ${activity.komponenBos}\n")
      manifestSb.append("Tahun Anggaran : ${activity.tahunAnggaran}\n")
      manifestSb.append("Total Foto     : ${documentations.size} berkas\n")
      manifestSb.append("Waktu Ekspor   : ${WitaTimeUtil.getCurrentWitaFormatted()}\n\n")
      manifestSb.append("DAFTAR BERKAS & CHECKSUM KEABSAHAN SHA-256:\n")
      manifestSb.append("----------------------------------------------------\n")

      for ((index, doc) in documentations.withIndex()) {
        val photoFile = File(doc.photoFilePath)
        val entryName = "foto_${index + 1}_${doc.category}_${photoFile.name}"
        manifestSb.append("${index + 1}. [${doc.category}] ${doc.nomorDokumentasi}\n")
        manifestSb.append("   Nama Berkas : $entryName\n")
        manifestSb.append("   Waktu WITA  : ${doc.witaFormattedDate}\n")
        manifestSb.append("   Petugas     : ${doc.officerName} (${doc.officerRole})\n")
        manifestSb.append("   Lokasi GPS  : ${doc.latitude}, ${doc.longitude}\n")
        manifestSb.append("   SHA-256     : ${doc.sha256Hash}\n\n")

        if (photoFile.exists()) {
          val zipEntry = ZipEntry(entryName)
          zos.putNextEntry(zipEntry)
          FileInputStream(photoFile).use { fis ->
            fis.copyTo(zos)
          }
          zos.closeEntry()
        }
      }

      // Add manifest entry
      val manifestEntry = ZipEntry("MANIFEST_VERIFIKASI_BOS.txt")
      zos.putNextEntry(manifestEntry)
      zos.write(manifestSb.toString().toByteArray(Charsets.UTF_8))
      zos.closeEntry()
    }

    zipFile
  }
}
