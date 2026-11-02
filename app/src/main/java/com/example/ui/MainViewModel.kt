package com.example.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.BosDatabase
import com.example.data.model.BosActivity
import com.example.data.model.BosDocumentation
import com.example.data.model.PhotoCategory
import com.example.data.model.UserRole
import com.example.data.model.UserSession
import com.example.data.repository.BosRepository
import com.example.util.LocationHelper
import com.example.util.WatermarkData
import com.example.util.WatermarkStamper
import com.example.util.WitaTimeUtil
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

  private val database = BosDatabase.getDatabase(application, viewModelScope)
  val repository = BosRepository(database.bosDao())

  private val _userSession = MutableStateFlow(UserSession())
  val userSession: StateFlow<UserSession> = _userSession.asStateFlow()

  val schoolName: StateFlow<String> = _userSession
    .map { it.schoolName }
    .stateIn(viewModelScope, SharingStarted.Eagerly, "SMK NEGERI 3 KOTAMOBAGU")

  val activities: StateFlow<List<BosActivity>> = repository.activities
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val documentations: StateFlow<List<BosDocumentation>> = repository.documentations
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val queuedCount: StateFlow<Int> = repository.queuedCount
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  private val _isSyncing = MutableStateFlow(false)
  val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

  private val _isProcessingPhoto = MutableStateFlow(false)
  val isProcessingPhoto: StateFlow<Boolean> = _isProcessingPhoto.asStateFlow()

  private val _toastEvent = MutableSharedFlow<String>()
  val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

  // Selected state for camera capture
  val selectedActivity = MutableStateFlow<BosActivity?>(null)
  val selectedCategory = MutableStateFlow(PhotoCategory.SEBELUM)

  // Filters for Archive Gallery
  val filterActivityId = MutableStateFlow<String?>(null)
  val filterCategory = MutableStateFlow<String?>(null)
  val filterOfficer = MutableStateFlow<String?>(null)

  val filteredDocumentations: StateFlow<List<BosDocumentation>> = combine(
    documentations,
    filterActivityId,
    filterCategory,
    filterOfficer
  ) { list, actId, cat, officer ->
    list.filter { doc ->
      (actId == null || doc.activityId == actId) &&
      (cat == null || doc.category == cat) &&
      (officer.isNullOrBlank() || doc.officerName.contains(officer, ignoreCase = true))
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun setRole(role: UserRole) {
    _userSession.value = _userSession.value.copy(role = role)
  }

  fun setOfficerName(name: String) {
    if (name.isNotBlank()) {
      _userSession.value = _userSession.value.copy(displayName = name.trim())
    }
  }

  fun setSchoolName(name: String) {
    if (name.isNotBlank()) {
      _userSession.value = _userSession.value.copy(schoolName = name.trim())
    }
  }

  fun selectActivity(activity: BosActivity) {
    selectedActivity.value = activity
  }

  fun selectCategory(category: PhotoCategory) {
    selectedCategory.value = category
  }

  fun processAndSavePhoto(
    context: Context,
    bitmap: Bitmap,
    activity: BosActivity,
    category: PhotoCategory,
    notes: String,
    customTimestampMillis: Long? = null,
    customCategory: String? = null,
    onSuccess: (BosDocumentation) -> Unit
  ) {
    viewModelScope.launch {
      _isProcessingPhoto.value = true
      try {
        val locationInfo = LocationHelper.resolveLocationInfo(context)
        val timestampMillis = customTimestampMillis ?: System.currentTimeMillis()
        val witaTimestamp = if (customTimestampMillis != null) {
          WitaTimeUtil.formatCustomWitaFormatted(customTimestampMillis)
        } else {
          WitaTimeUtil.getCurrentWitaFormatted()
        }
        val docNumber = repository.generateDocNumber()
        val officer = _userSession.value

        val storageDir = File(context.filesDir, "bos_photos").apply {
          if (!exists()) mkdirs()
        }

        val effectiveCategory = customCategory?.takeIf { it.isNotBlank() } ?: category.label

        val watermarkData = WatermarkData(
          nomorDokumentasi = docNumber,
          namaKegiatan = activity.namaKegiatan,
          komponenBos = activity.komponenBos,
          kategoriFoto = effectiveCategory,
          namaPetugas = officer.displayName,
          peranPetugas = officer.role.label,
          witaTimestamp = witaTimestamp,
          latitude = locationInfo.latitude,
          longitude = locationInfo.longitude,
          alamat = locationInfo.addressText,
          customTimestampMillis = customTimestampMillis,
          namaSekolah = officer.schoolName
        )

        val result = WatermarkStamper.stampWatermark(
          context = context,
          originalBitmap = bitmap,
          data = watermarkData,
          outputDirectory = storageDir
        )

        val docEntity = BosDocumentation(
          id = UUID.randomUUID().toString(),
          nomorDokumentasi = docNumber,
          activityId = activity.id,
          activityName = activity.namaKegiatan,
          komponenBos = activity.komponenBos,
          category = effectiveCategory,
          officerName = officer.displayName,
          officerRole = officer.role.label,
          witaTimestampMillis = timestampMillis,
          witaFormattedDate = witaTimestamp,
          latitude = locationInfo.latitude,
          longitude = locationInfo.longitude,
          addressText = locationInfo.addressText,
          photoFilePath = result.stampedFile.absolutePath,
          sha256Hash = result.sha256Hash,
          syncStatus = "QUEUED",
          catatan = notes
        )

        repository.addDocumentation(docEntity)
        _toastEvent.emit("Foto tersimpan & terverifikasi (SHA-256 dibuat)")
        onSuccess(docEntity)
      } catch (e: Exception) {
        _toastEvent.emit("Gagal memproses foto: ${e.message}")
      } finally {
        _isProcessingPhoto.value = false
      }
    }
  }

  fun triggerSync() {
    viewModelScope.launch {
      _isSyncing.value = true
      try {
        val synced = repository.syncAllQueued()
        _toastEvent.emit("Berhasil sinkronisasi $synced bukti ke cloud")
      } catch (e: Exception) {
        _toastEvent.emit("Sinkronisasi gagal: ${e.message}")
      } finally {
        _isSyncing.value = false
      }
    }
  }

  fun addNewActivity(
    kode: String,
    nama: String,
    komponen: String,
    alokasi: Long,
    deskripsi: String
  ) {
    viewModelScope.launch {
      val newAct = BosActivity(
        id = UUID.randomUUID().toString(),
        kodeKegiatan = kode,
        namaKegiatan = nama,
        komponenBos = komponen,
        alokasiDana = alokasi,
        tahunAnggaran = "2026",
        deskripsi = deskripsi
      )
      repository.addActivity(newAct)
      _toastEvent.emit("Kegiatan BOS baru berhasil ditambahkan")
    }
  }

  fun deleteActivity(activity: BosActivity) {
    viewModelScope.launch {
      repository.deleteActivity(activity)
      _toastEvent.emit("Kegiatan berhasil dihapus")
    }
  }

  fun deleteDocumentation(doc: BosDocumentation) {
    viewModelScope.launch {
      repository.deleteDocumentation(doc)
      try {
        val file = File(doc.photoFilePath)
        if (file.exists()) file.delete()
      } catch (_: Exception) {}
      _toastEvent.emit("Bukti foto berhasil dihapus")
    }
  }
}
