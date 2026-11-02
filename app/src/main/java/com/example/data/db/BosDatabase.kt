package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BosActivity
import com.example.data.model.BosDocumentation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [BosActivity::class, BosDocumentation::class],
  version = 1,
  exportSchema = false
)
abstract class BosDatabase : RoomDatabase() {
  abstract fun bosDao(): BosDao

  companion object {
    @Volatile
    private var INSTANCE: BosDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): BosDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          BosDatabase::class.java,
          "bos_cam_smkn3.db"
        )
          .addCallback(DatabaseCallback(scope))
          .build()
        INSTANCE = instance
        instance
      }
    }

    private class DatabaseCallback(
      private val scope: CoroutineScope
    ) : Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          scope.launch(Dispatchers.IO) {
            populateInitialActivities(database.bosDao())
          }
        }
      }

      private suspend fun populateInitialActivities(dao: BosDao) {
        val initialList = listOf(
          BosActivity(
            id = "act-01",
            kodeKegiatan = "BOS-SMKN3-2026-001",
            namaKegiatan = "Pemeliharaan & Peningkatan Sarana Lab TKJ",
            komponenBos = "03. Pemeliharaan Sarana dan Prasarana Sekolah",
            alokasiDana = 24500000L,
            tahunAnggaran = "2026",
            deskripsi = "Perbaikan switch hub, kabel jaringan UTP Cat6, dan servis komputer client lab TKJ."
          ),
          BosActivity(
            id = "act-02",
            kodeKegiatan = "BOS-SMKN3-2026-002",
            namaKegiatan = "Pengadaan Bahan Ajar & Modul Kejuruan",
            komponenBos = "02. Pengembangan Perpustakaan dan Layanan Pojok Baca",
            alokasiDana = 12800000L,
            tahunAnggaran = "2026",
            deskripsi = "Penyediaan buku teks kejuruan dan modul ajar praktikum siswa SMK."
          ),
          BosActivity(
            id = "act-03",
            kodeKegiatan = "BOS-SMKN3-2026-003",
            namaKegiatan = "Langganan Internet Fiber Optik & Daya Listrik",
            komponenBos = "06. Langganan Daya dan Jasa Sekolah",
            alokasiDana = 18000000L,
            tahunAnggaran = "2026",
            deskripsi = "Pembayaran tagihan listrik PLN dan bandwidth internet 100 Mbps untuk kegiatan belajar."
          ),
          BosActivity(
            id = "act-04",
            kodeKegiatan = "BOS-SMKN3-2026-004",
            namaKegiatan = "Pelaksanaan Uji Kompetensi Keahlian (UKK)",
            komponenBos = "08. Penyelenggaraan Kegiatan Penilaian & Asesmen",
            alokasiDana = 15600000L,
            tahunAnggaran = "2026",
            deskripsi = "Honorarium penguji eksternal industri, konsumsi pengawas, dan sertifikasi BNSP."
          )
        )
        dao.insertActivities(initialList)
      }
    }
  }
}
