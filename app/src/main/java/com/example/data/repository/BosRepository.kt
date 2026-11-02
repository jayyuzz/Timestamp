package com.example.data.repository

import com.example.data.db.BosDao
import com.example.data.model.BosActivity
import com.example.data.model.BosDocumentation
import com.example.data.model.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class BosRepository(
  private val bosDao: BosDao
) {

  val activities: Flow<List<BosActivity>> = bosDao.getAllActivities()
  val documentations: Flow<List<BosDocumentation>> = bosDao.getAllDocumentations()
  val queuedCount: Flow<Int> = bosDao.getQueuedCount()

  fun getDocumentationsByActivity(activityId: String): Flow<List<BosDocumentation>> {
    return bosDao.getDocumentationsByActivity(activityId)
  }

  suspend fun addActivity(activity: BosActivity) = withContext(Dispatchers.IO) {
    bosDao.insertActivity(activity)
  }

  suspend fun updateActivity(activity: BosActivity) = withContext(Dispatchers.IO) {
    bosDao.updateActivity(activity)
  }

  suspend fun deleteActivity(activity: BosActivity) = withContext(Dispatchers.IO) {
    bosDao.deleteActivity(activity)
  }

  suspend fun addDocumentation(doc: BosDocumentation) = withContext(Dispatchers.IO) {
    bosDao.insertDocumentation(doc)
  }

  suspend fun deleteDocumentation(doc: BosDocumentation) = withContext(Dispatchers.IO) {
    bosDao.deleteDocumentation(doc)
  }

  suspend fun syncAllQueued(): Int = withContext(Dispatchers.IO) {
    val queued = bosDao.getDocumentationsBySyncStatus(SyncStatus.QUEUED.name).first()
    var count = 0
    for (item in queued) {
      // Simulate network sync latency per item
      delay(200)
      bosDao.updateSyncStatus(item.id, SyncStatus.SYNCED.name)
      count++
    }
    count
  }

  suspend fun generateDocNumber(): String = withContext(Dispatchers.IO) {
    val currentDocs = bosDao.getAllDocumentations().first()
    val sdf = SimpleDateFormat("yyyy/MM", Locale.US).apply {
      timeZone = TimeZone.getTimeZone("Asia/Makassar")
    }
    val datePrefix = sdf.format(Date())
    val nextIndex = currentDocs.size + 1
    val indexStr = String.format(Locale.US, "%04d", nextIndex)
    "SMKN3-BOS/$datePrefix/$indexStr"
  }
}
