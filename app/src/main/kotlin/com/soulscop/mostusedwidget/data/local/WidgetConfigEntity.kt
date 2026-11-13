package com.soulscop.mostusedwidget.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.soulscop.mostusedwidget.util.SortMode

@Entity(tableName = "widget_config")
data class WidgetConfigEntity(
    @PrimaryKey val widgetId: Int,
    val sortMode: String = SortMode.LAUNCH_COUNT.name,
    val w1: Float = 2f,
    val w2: Float = 0f,
    val decayFactorDays: Float = 7f,
    val iconCount: Int = 4,
    val bgColor: Int = -14803426, // 0xFF1E1E1E
    val textColor: Int = -1,       // 0xFFFFFFFF
    val cornerRadiusDp: Int = 0,
    val iconSizeDp: Int = 44,
    val templateId: Int = 0
)
