package com.soulscop.mostusedwidget.ui.components

import android.content.Context
import com.soulscop.mostusedwidget.R

fun formatDuration(context: Context, ms: Long): String {
    val totalMinutes = ms / 60000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) {
        context.getString(R.string.duration_hours, hours, minutes)
    } else {
        context.getString(R.string.duration_minutes, minutes)
    }
}
