package com.soulscop.mostusedwidget.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.soulscop.mostusedwidget.data.local.PinnedAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PinnedAppDao {
    @Query("SELECT * FROM pinned_app ORDER BY sortOrder ASC")
    fun observeAll(): Flow<List<PinnedAppEntity>>

    @Query("SELECT * FROM pinned_app ORDER BY sortOrder ASC")
    suspend fun getAll(): List<PinnedAppEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: PinnedAppEntity)

    @Query("DELETE FROM pinned_app WHERE packageName = :packageName")
    suspend fun delete(packageName: String)
}
