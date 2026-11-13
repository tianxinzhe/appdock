package com.soulscop.mostusedwidget.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.soulscop.mostusedwidget.data.local.WidgetConfigEntity

@Dao
interface WidgetConfigDao {
    @Query("SELECT * FROM widget_config WHERE widgetId = :widgetId")
    suspend fun get(widgetId: Int): WidgetConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(config: WidgetConfigEntity)

    @Query("DELETE FROM widget_config WHERE widgetId = :widgetId")
    suspend fun delete(widgetId: Int)
}
