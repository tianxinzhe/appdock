package com.soulscop.mostusedwidget.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.soulscop.mostusedwidget.data.local.HiddenAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HiddenAppDao {
    @Query("SELECT * FROM hidden_app")
    fun observeAll(): Flow<List<HiddenAppEntity>>

    @Query("SELECT * FROM hidden_app")
    suspend fun getAll(): List<HiddenAppEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: HiddenAppEntity)

    @Query("DELETE FROM hidden_app WHERE packageName = :packageName")
    suspend fun delete(packageName: String)
}
