package com.soulscop.mostusedwidget.domain

import com.soulscop.mostusedwidget.data.local.SettingsEntity
import com.soulscop.mostusedwidget.util.SortMode
import kotlin.math.exp

object ScoreCalculator {
    fun calculate(
        launch7d: Long,
        launch30d: Long,
        foregroundTime7d: Long,
        lastUsedTime: Long,
        now: Long,
        settings: SettingsEntity
    ): Double {
        if (!settings.isPremium) return launch7d * 2.0

        return when (runCatching { SortMode.valueOf(settings.sortMode) }.getOrDefault(SortMode.LAUNCH_COUNT)) {
            SortMode.LAUNCH_COUNT -> {
                val ageDays = (now - lastUsedTime).coerceAtLeast(0L).toDouble() / (24.0 * 60 * 60 * 1000)
                val decay = 100.0 * exp(-ageDays / settings.decayFactorDays.coerceAtLeast(0.1f))
                settings.w1 * launch7d + settings.w2 * launch30d + decay
            }
            SortMode.FOREGROUND_TIME -> foregroundTime7d.toDouble()
            SortMode.LAST_USED -> lastUsedTime.toDouble()
        }
    }
}
