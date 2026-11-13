package com.soulscop.mostusedwidget.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.soulscop.mostusedwidget.util.SortMode

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 0,
    val isPremium: Boolean = false,
    val w1: Float = 2f,
    val w2: Float = 0f,
    val decayFactorDays: Float = 7f,
    val sortMode: String = SortMode.LAUNCH_COUNT.name,
    val excludeSystem: Boolean = false,
    val excludeGames: Boolean = false,
    val autoRefreshEnabled: Boolean = false
)
