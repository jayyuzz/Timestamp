package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BosActivity
import com.example.data.model.BosDocumentation
import kotlinx.coroutines.flow.Flow

@Dao
interface BosDao {

  @Query("SELECT * FROM bos_activities ORDER BY createdAt DESC")
  fun getAllActivities(): Flow<List<BosActivity>>

  @Query("SELECT * FROM bos_activities WHERE id = :id LIMIT 1")
  fun getActivityById(id: String): Flow<BosActivity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertActivity(activity: BosActivity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertActivities(activities: List<BosActivity>)

  @Update
  suspend fun updateActivity(activity: BosActivity)

  @Delete
  suspend fun deleteActivity(activity: BosActivity)

  @Query("SELECT * FROM bos_documentations ORDER BY witaTimestampMillis DESC")
  fun getAllDocumentations(): Flow<List<BosDocumentation>>

  @Query("SELECT * FROM bos_documentations WHERE activityId = :activityId ORDER BY witaTimestampMillis ASC")
  fun getDocumentationsByActivity(activityId: String): Flow<List<BosDocumentation>>

  @Query("SELECT * FROM bos_documentations WHERE syncStatus = :status ORDER BY witaTimestampMillis ASC")
  fun getDocumentationsBySyncStatus(status: String): Flow<List<BosDocumentation>>

  @Query("SELECT COUNT(*) FROM bos_documentations WHERE syncStatus = 'QUEUED'")
  fun getQueuedCount(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDocumentation(doc: BosDocumentation)

  @Update
  suspend fun updateDocumentation(doc: BosDocumentation)

  @Delete
  suspend fun deleteDocumentation(doc: BosDocumentation)

  @Query("UPDATE bos_documentations SET syncStatus = :newStatus WHERE id = :id")
  suspend fun updateSyncStatus(id: String, newStatus: String)
}
