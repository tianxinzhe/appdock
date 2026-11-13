package com.soulscop.mostusedwidget.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.soulscop.mostusedwidget.data.local.dao.HiddenAppDao
import com.soulscop.mostusedwidget.data.local.dao.PinnedAppDao
import com.soulscop.mostusedwidget.data.local.dao.SettingsDao
import com.soulscop.mostusedwidget.data.local.dao.UsageCacheDao
import com.soulscop.mostusedwidget.data.local.dao.WidgetConfigDao

@Database(
    entities = [
        UsageCacheEntity::class,
        HiddenAppEntity::class,
        PinnedAppEntity::class,
        SettingsEntity::class,
        WidgetConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usageCacheDao(): UsageCacheDao
    abstract fun hiddenAppDao(): HiddenAppDao
    abstract fun pinnedAppDao(): PinnedAppDao
    abstract fun settingsDao(): SettingsDao
    abstract fun widgetConfigDao(): WidgetConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mostused.db"
                ).build().also { INSTANCE = it }
            }
    }
}
