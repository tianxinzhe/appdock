package com.soulscop.mostusedwidget.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hidden_app")
data class HiddenAppEntity(
    @PrimaryKey val packageName: String
)
