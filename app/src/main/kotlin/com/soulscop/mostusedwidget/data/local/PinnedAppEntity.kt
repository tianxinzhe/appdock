package com.soulscop.mostusedwidget.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pinned_app")
data class PinnedAppEntity(
    @PrimaryKey val packageName: String,
    val sortOrder: Int
)
