package com.soulscop.mostusedwidget.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usage_cache")
data class UsageCacheEntity(
    @PrimaryKey val packageName: String,
    val appLabel: String,
    val launchCount7d: Long,
    val launchCount30d: Long,
    val foregroundTime7d: Long,
    val foregroundTime30d: Long,
    val lastUsedTime: Long,
    val score: Double,
    val lastUpdated: Long
)
