package com.soulscop.mostusedwidget.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.soulscop.mostusedwidget.data.local.UsageCacheEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageCacheDao {
    @Query("SELECT * FROM usage_cache ORDER BY score DESC")
    fun observeAll(): Flow<List<UsageCacheEntity>>

    @Query("SELECT * FROM usage_cache ORDER BY score DESC")
    suspend fun getAll(): List<UsageCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<UsageCacheEntity>)

    // Diff-delete: keep transaction atomic without clearing the whole table (#10)
    @Query("DELETE FROM usage_cache WHERE packageName NOT IN (:packages)")
    suspend fun deleteNotIn(packages: List<String>)

    @Query("DELETE FROM usage_cache")
    suspend fun clear()
}
